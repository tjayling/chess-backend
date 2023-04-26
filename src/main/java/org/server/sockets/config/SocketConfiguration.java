package org.server.sockets.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.server.sockets.handler.LobbyHandler;
import org.server.sockets.handler.SocketHandler;

@Configuration
@EnableWebSocket
public class SocketConfiguration implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new SocketHandler(), "/socket").setAllowedOrigins("*");
        registry.addHandler(new LobbyHandler(), "/lobby").setAllowedOrigins("*");
    }
}