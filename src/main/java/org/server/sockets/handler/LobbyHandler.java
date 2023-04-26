package org.server.sockets.handler;

import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LobbyHandler extends TextWebSocketHandler {
    List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws IOException {
        sendMessageToAllButCurrentSession(session, message);
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        sessions.add(session);
        //todo: Add user ids somehow
        TextMessage sessionIdMessage = new TextMessage((String.format("{\"action\": \"sessionId\", \"payload\": \"%s\"}", session.getId())));

        sendMessageToAllSessions(generateCurrentUsersMessage());
        sendMessageToCurrentSession(session, sessionIdMessage);
    }

    @Override
    public void afterConnectionClosed( @NonNull WebSocketSession session, CloseStatus status) throws Exception {
        if (status.getCode() == 1001) {
            sessions.remove(session);
        }
        sendMessageToAllSessions(generateCurrentUsersMessage());
    }

    public TextMessage generateCurrentUsersMessage() {
        List<String> allSessionIds = (sessions.stream().map(WebSocketSession::getId).toList());
        return new TextMessage(String.format("{\"action\": \"currentUsers\", \"payload\": \"%s\"}", allSessionIds));
    }

    public void sendMessageToCurrentSession(WebSocketSession session, TextMessage message) throws IOException {
        session.sendMessage(message);
    }

    public void sendMessageToAllButCurrentSession(WebSocketSession session, TextMessage message) throws IOException {
        for (WebSocketSession webSocketSession : sessions) {
            if (webSocketSession.isOpen() && !session.getId().equals(webSocketSession.getId())) {
                webSocketSession.sendMessage(message);
            }
        }
    }

    public void sendMessageToAllSessions(TextMessage message) throws IOException {
        for (WebSocketSession webSocketSession : sessions) {
            if (webSocketSession.isOpen()) {
                webSocketSession.sendMessage(message);
            }
        }
    }
}
