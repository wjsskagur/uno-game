package com.example.uno.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP over WebSocket 설정
 *
 * STOMP 사용 이유:
 * - 순수 WebSocket은 메시지 형식이 없어 직접 파싱 필요
 * - STOMP는 pub/sub 모델을 제공해 /topic, /queue 구독이 직관적
 * - Spring의 @MessageMapping과 자연스럽게 통합
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트가 구독할 prefix
        config.enableSimpleBroker("/topic");
        // 클라이언트가 서버로 메시지 보낼 때 prefix
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // 개발환경: React dev server 허용
                .withSockJS();                  // SockJS fallback (WebSocket 미지원 환경)
    }
}
