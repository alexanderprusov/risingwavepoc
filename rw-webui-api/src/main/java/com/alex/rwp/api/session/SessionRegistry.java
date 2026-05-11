package com.alex.rwp.api.session;

import io.vertx.core.http.ServerWebSocket;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRegistry {

    private final ConcurrentHashMap<String, WsSession> sessions = new ConcurrentHashMap<>();

    public WsSession register(String sessionId, ServerWebSocket ws) {
        var session = new WsSession(sessionId, "pending", ws, Instant.now());
        sessions.put(sessionId, session);
        return session;
    }

    public void updateUsername(String sessionId, String username) {
        sessions.computeIfPresent(sessionId, (id, s) -> s.withUsername(username));
    }

    public void remove(String sessionId) {
        sessions.remove(sessionId);
    }

    public Collection<WsSession> all() {
        return sessions.values();
    }
}
