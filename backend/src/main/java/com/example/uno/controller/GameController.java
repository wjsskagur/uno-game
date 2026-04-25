package com.example.uno.controller;

import com.example.uno.dto.GameDtos;
import com.example.uno.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 방 생성/입장: REST API (1회성 요청)
 * 게임 진행 (카드 내기, 드로우): WebSocket STOMP (실시간)
 *
 * 구분 이유:
 * - 방 생성/입장은 HTTP 응답(playerId, roomId)이 필요
 * - 게임 액션은 서버 → 전체 플레이어 브로드캐스트가 필요해 WebSocket이 적합
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    // ─── REST: 방 관리 ──────────────────────────────────────────────────

    @PostMapping("/api/rooms")
    @ResponseBody
    public ResponseEntity<GameDtos.RoomCreatedResponse> createRoom(
            @RequestBody GameDtos.CreateRoomRequest request) {
        return ResponseEntity.ok(gameService.createRoom(request.nickname()));
    }

    @PostMapping("/api/rooms/{roomId}/join")
    @ResponseBody
    public ResponseEntity<GameDtos.JoinResponse> joinRoom(
            @PathVariable String roomId,
            @RequestBody GameDtos.JoinRequest request) {
        return ResponseEntity.ok(gameService.joinRoom(roomId, request.nickname()));
    }

    @PostMapping("/api/rooms/{roomId}/start")
    @ResponseBody
    public ResponseEntity<Void> startGame(@PathVariable String roomId) {
        gameService.startGame(roomId);
        return ResponseEntity.ok().build();
    }

    // ─── WebSocket STOMP: 게임 액션 ─────────────────────────────────────

    @MessageMapping("/game.play")
    public void playCard(@Payload GameDtos.PlayCardRequest request) {
        try {
            gameService.playCard(request);
        } catch (Exception e) {
            log.error("Error playing card", e);
        }
    }

    @MessageMapping("/game.draw")
    public void drawCard(@Payload GameDtos.DrawCardRequest request) {
        try {
            gameService.drawCard(request);
        } catch (Exception e) {
            log.error("Error drawing card", e);
        }
    }

    @MessageMapping("/game.uno")
    public void callUno(@Payload GameDtos.UnoCallRequest request) {
        try {
            gameService.callUno(request);
        } catch (Exception e) {
            log.error("Error calling UNO", e);
        }
    }
}
