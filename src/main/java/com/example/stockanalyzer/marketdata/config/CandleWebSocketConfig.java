package com.example.stockanalyzer.marketdata.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class CandleWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final String[] allowedOriginPatterns;

    public CandleWebSocketConfig(
            @Value("${app.websocket.allowed-origin-patterns:http://localhost:8080}") String allowedOriginPatterns) {
        this.allowedOriginPatterns = java.util.Arrays.stream(allowedOriginPatterns.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/candles").setAllowedOriginPatterns(allowedOriginPatterns);
    }
}
