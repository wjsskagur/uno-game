package com.example.uno.controller;

import com.example.uno.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * WebSocket 연결 해제 감지
 * 플레이어가 브라우저를 닫거나 네트워크 끊길 때 게임 상태 정리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final GameService gameService;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        log.info("WebSocket disconnected: {}", sessionId);
        // 실제 구현에서는 sessionId를 playerId로 매핑해서 처리
    }
}
