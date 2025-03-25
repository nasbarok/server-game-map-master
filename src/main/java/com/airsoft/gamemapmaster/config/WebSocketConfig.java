package com.airsoft.gamemapmaster.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${websocket.endpoint}")
    private String endpoint;

    @Value("${websocket.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Préfixe pour les destinations qui mappent les méthodes @MessageMapping
        registry.setApplicationDestinationPrefixes("/app");
        
        // Activer un broker simple en mémoire pour envoyer des messages aux clients
        // Les clients s'abonnent à ces destinations pour recevoir les messages
        registry.enableSimpleBroker("/topic", "/queue");
        
        // Préfixe pour les messages destinés à un utilisateur spécifique
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Enregistrer le point de terminaison WebSocket
        registry.addEndpoint(endpoint)
                .setAllowedOrigins(allowedOrigins.split(","));
    }
}
