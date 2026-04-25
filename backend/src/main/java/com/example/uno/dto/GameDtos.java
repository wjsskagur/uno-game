package com.example.uno.dto;

import com.example.uno.domain.*;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DTO 설계 원칙:
 * - 클라이언트에게 손패(hand)는 본인 것만 전달 → 치트 방지
 * - 다른 플레이어 정보는 handSize만 노출
 */
public class GameDtos {

    // ─── Inbound (클라이언트 → 서버) ───────────────────────────────────

    public record JoinRequest(String nickname) {}

    public record PlayCardRequest(
            String roomId,
            String playerId,
            Card card,
            CardColor declaredColor  // WILD 카드 낼 때 선언할 색상
    ) {}

    public record DrawCardRequest(String roomId, String playerId) {}

    public record UnoCallRequest(String roomId, String playerId) {}

    public record CreateRoomRequest(String nickname) {}

    // ─── Outbound (서버 → 클라이언트) ──────────────────────────────────

    @Data
    @Builder
    public static class RoomCreatedResponse {
        private String roomId;
        private String playerId;
    }

    @Data
    @Builder
    public static class JoinResponse {
        private String playerId;
        private String roomId;
    }

    /**
     * 게임 전체 상태 - 브로드캐스트용
     * 손패는 포함하지 않음 (개인 정보는 별도 PlayerHandUpdate로)
     */
    @Data
    @Builder
    public static class GameStateResponse {
        private String roomId;
        private String currentPlayerId;
        private String currentPlayerNickname;
        private CardColor currentColor;
        private GameDirection direction;
        private Card topCard;
        private int deckSize;
        private List<PlayerInfo> players;
        private GameState.Phase phase;
        private String winnerId;
        private String winnerNickname;
        private String lastAction; // 마지막 액션 설명 (ex: "홍길동이 RED_SKIP을 냈습니다")

        public static GameStateResponse from(GameState state, String lastAction) {
            List<PlayerInfo> playerInfos = state.getPlayers().stream()
                    .map(p -> PlayerInfo.builder()
                            .id(p.getId())
                            .nickname(p.getNickname())
                            .handSize(p.getHandSize())
                            .calledUno(p.isCalledUno())
                            .build())
                    .collect(Collectors.toList());

            String winnerNickname = state.getWinnerId() == null ? null :
                    state.getPlayers().stream()
                            .filter(p -> p.getId().equals(state.getWinnerId()))
                            .map(Player::getNickname)
                            .findFirst().orElse(null);

            return GameStateResponse.builder()
                    .roomId(state.getRoomId())
                    .currentPlayerId(state.currentPlayer().getId())
                    .currentPlayerNickname(state.currentPlayer().getNickname())
                    .currentColor(state.getCurrentColor())
                    .direction(state.getDirection())
                    .topCard(state.topCard())
                    .deckSize(state.getDeck().size())
                    .players(playerInfos)
                    .phase(state.getPhase())
                    .winnerId(state.getWinnerId())
                    .winnerNickname(winnerNickname)
                    .lastAction(lastAction)
                    .build();
        }
    }

    @Data
    @Builder
    public static class PlayerInfo {
        private String id;
        private String nickname;
        private int handSize;
        private boolean calledUno;
    }

    /**
     * 손패 업데이트 - 본인에게만 전송
     */
    @Data
    @Builder
    public static class PlayerHandUpdate {
        private String playerId;
        private List<Card> hand;
    }

    @Data
    @Builder
    public static class ErrorResponse {
        private String message;
    }
}
