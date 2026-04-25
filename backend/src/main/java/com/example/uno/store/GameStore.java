package com.example.uno.store;

import com.example.uno.domain.GameState;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * InMemory 게임 저장소
 * ConcurrentHashMap 사용 이유: WebSocket 멀티스레드 환경에서 동시 접근 안전
 * Redis로 전환 시 이 클래스만 교체하면 됨 (인터페이스 추출 권장)
 */
@Component
public class GameStore {

    private final Map<String, GameState> games = new ConcurrentHashMap<>();

    // roomId → playerId 역방향 조회용 (disconnect 처리)
    private final Map<String, String> playerRoomMap = new ConcurrentHashMap<>();

    public void save(GameState state) {
        games.put(state.getRoomId(), state);
    }

    public Optional<GameState> find(String roomId) {
        return Optional.ofNullable(games.get(roomId));
    }

    public GameState findOrThrow(String roomId) {
        return find(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));
    }

    public void delete(String roomId) {
        games.remove(roomId);
    }

    public void registerPlayerRoom(String playerId, String roomId) {
        playerRoomMap.put(playerId, roomId);
    }

    public Optional<String> findRoomByPlayer(String playerId) {
        return Optional.ofNullable(playerRoomMap.get(playerId));
    }

    public void removePlayerRoom(String playerId) {
        playerRoomMap.remove(playerId);
    }

    public int activeRoomCount() {
        return games.size();
    }
}
