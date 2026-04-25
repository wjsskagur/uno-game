package com.example.uno.service;

import com.example.uno.domain.*;
import com.example.uno.dto.GameDtos;
import com.example.uno.engine.UnoRuleEngine;
import com.example.uno.store.GameStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final GameStore gameStore;
    private final UnoRuleEngine ruleEngine;
    private final SimpMessagingTemplate messagingTemplate;

    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 4;

    // ─── 방 관리 ────────────────────────────────────────────────────────

    public GameDtos.RoomCreatedResponse createRoom(String nickname) {
        String roomId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String playerId = UUID.randomUUID().toString();

        GameState state = new GameState(roomId);
        Player host = new Player(playerId, nickname);
        state.addPlayer(host);
        gameStore.save(state);
        gameStore.registerPlayerRoom(playerId, roomId);

        log.info("Room created: {} by {}", roomId, nickname);
        return GameDtos.RoomCreatedResponse.builder()
                .roomId(roomId)
                .playerId(playerId)
                .build();
    }

    public GameDtos.JoinResponse joinRoom(String roomId, String nickname) {
        GameState state = gameStore.findOrThrow(roomId);

        if (state.getPhase() != GameState.Phase.WAITING) {
            throw new IllegalStateException("Game already started");
        }
        if (state.getPlayers().size() >= MAX_PLAYERS) {
            throw new IllegalStateException("Room is full");
        }

        String playerId = UUID.randomUUID().toString();
        Player player = new Player(playerId, nickname);
        state.addPlayer(player);
        gameStore.registerPlayerRoom(playerId, roomId);

        log.info("Player {} joined room {}", nickname, roomId);
        broadcastGameState(state, nickname + "님이 입장했습니다.");

        return GameDtos.JoinResponse.builder()
                .playerId(playerId)
                .roomId(roomId)
                .build();
    }

    // ─── 게임 시작 ──────────────────────────────────────────────────────

    public void startGame(String roomId) {
        GameState state = gameStore.findOrThrow(roomId);

        if (state.getPhase() != GameState.Phase.WAITING) {
            throw new IllegalStateException("Game already started");
        }
        if (state.getPlayers().size() < MIN_PLAYERS) {
            throw new IllegalStateException("Need at least " + MIN_PLAYERS + " players");
        }

        ruleEngine.initializeGame(state);
        log.info("Game started in room {}", roomId);

        broadcastGameState(state, "게임이 시작되었습니다!");
        broadcastAllHands(state);
    }

    // ─── 카드 내기 ──────────────────────────────────────────────────────

    public void playCard(GameDtos.PlayCardRequest request) {
        GameState state = gameStore.findOrThrow(request.roomId());
        validateTurn(state, request.playerId());

        Player player = state.getPlayerById(request.playerId());
        Card card = request.card();

        // 룰 검증
        if (!ruleEngine.canPlay(card, state.topCard(), state.getCurrentColor())) {
            sendError(request.playerId(), "낼 수 없는 카드입니다.");
            return;
        }

        // WILD 카드인데 색상 선언 안 함
        if (card.isWild() && request.declaredColor() == null) {
            sendError(request.playerId(), "색상을 선언해야 합니다.");
            return;
        }

        // 손패에서 카드 제거
        if (!player.removeCard(card)) {
            sendError(request.playerId(), "해당 카드가 손패에 없습니다.");
            return;
        }

        // 버린 카드 더미에 추가
        state.getDiscardPile().push(card);

        // 현재 색상 업데이트 (숫자/액션 카드는 해당 카드 색상으로)
        if (!card.isWild()) {
            state.setCurrentColor(card.color());
        }

        // 효과 적용
        CardColor declared = card.isWild() ? request.declaredColor() : card.color();
        ruleEngine.applyEffect(state, card, declared);

        // 승리 체크
        if (player.hasWon()) {
            state.setPhase(GameState.Phase.FINISHED);
            state.setWinnerId(player.getId());
            broadcastGameState(state, player.getNickname() + "님이 우승했습니다! 🎉");
            return;
        }

        // 다음 턴으로
        state.advanceTurn();

        String action = player.getNickname() + "이(가) " + card + " 을(를) 냈습니다.";
        broadcastGameState(state, action);
        broadcastAllHands(state);
    }

    // ─── 카드 드로우 ────────────────────────────────────────────────────

    public void drawCard(GameDtos.DrawCardRequest request) {
        GameState state = gameStore.findOrThrow(request.roomId());
        validateTurn(state, request.playerId());

        Player player = state.getPlayerById(request.playerId());
        int playerIndex = state.getPlayers().indexOf(player);
        ruleEngine.giveCards(state, playerIndex, 1);

        // 드로우 후 낼 수 있는 카드가 없으면 턴 넘김
        Card drawn = player.getHand().get(player.getHand().size() - 1);
        if (!ruleEngine.canPlay(drawn, state.topCard(), state.getCurrentColor())) {
            state.advanceTurn();
        }

        String action = player.getNickname() + "이(가) 카드를 뽑았습니다.";
        broadcastGameState(state, action);
        broadcastAllHands(state);
    }

    // ─── UNO 선언 ───────────────────────────────────────────────────────

    public void callUno(GameDtos.UnoCallRequest request) {
        GameState state = gameStore.findOrThrow(request.roomId());
        Player player = state.getPlayerById(request.playerId());

        if (player.getHandSize() == 1) {
            player.callUno();
            broadcastGameState(state, player.getNickname() + "이(가) UNO를 선언했습니다!");
        } else {
            sendError(request.playerId(), "손패가 1장일 때만 UNO를 선언할 수 있습니다.");
        }
    }

    // ─── 연결 해제 처리 ─────────────────────────────────────────────────

    public void handleDisconnect(String playerId) {
        gameStore.findRoomByPlayer(playerId).ifPresent(roomId -> {
            gameStore.find(roomId).ifPresent(state -> {
                if (state.getPhase() == GameState.Phase.PLAYING) {
                    Player player = state.getPlayerById(playerId);
                    state.removePlayer(playerId);

                    if (state.getPlayers().size() < MIN_PLAYERS) {
                        state.setPhase(GameState.Phase.FINISHED);
                        broadcastGameState(state, player.getNickname() + "님이 나가서 게임이 종료되었습니다.");
                        gameStore.delete(roomId);
                    } else {
                        broadcastGameState(state, player.getNickname() + "님이 나갔습니다.");
                    }
                }
            });
            gameStore.removePlayerRoom(playerId);
        });
    }

    // ─── 내부 헬퍼 ─────────────────────────────────────────────────────

    private void validateTurn(GameState state, String playerId) {
        if (state.getPhase() != GameState.Phase.PLAYING) {
            throw new IllegalStateException("Game is not in progress");
        }
        if (!state.isCurrentPlayer(playerId)) {
            throw new IllegalStateException("It's not your turn");
        }
    }

    /**
     * 전체 플레이어에게 게임 상태 브로드캐스트
     * 손패(hand)는 포함하지 않음
     */
    private void broadcastGameState(GameState state, String lastAction) {
        GameDtos.GameStateResponse response = GameDtos.GameStateResponse.from(state, lastAction);
        messagingTemplate.convertAndSend("/topic/room/" + state.getRoomId(), response);
    }

    /**
     * 각 플레이어에게 본인 손패만 개별 전송
     * /user/{playerId}/queue/hand 로 전송 → 본인만 수신
     */
    private void broadcastAllHands(GameState state) {
        for (Player player : state.getPlayers()) {
            GameDtos.PlayerHandUpdate handUpdate = GameDtos.PlayerHandUpdate.builder()
                    .playerId(player.getId())
                    .hand(player.getHand())
                    .build();
            messagingTemplate.convertAndSend("/topic/hand/" + player.getId(), handUpdate);
        }
    }

    private void sendError(String playerId, String message) {
        messagingTemplate.convertAndSend(
                "/topic/error/" + playerId,
                GameDtos.ErrorResponse.builder().message(message).build()
        );
    }
}
