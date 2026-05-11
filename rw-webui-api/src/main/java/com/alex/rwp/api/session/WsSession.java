package com.alex.rwp.api.session;

import io.vertx.core.http.ServerWebSocket;

import java.time.Instant;

public record WsSession(
    String sessionId,
    String username,
    ServerWebSocket ws,
    Instant connectedAt
) {
    WsSession withUsername(String username) {
        return new WsSession(sessionId, username, ws, connectedAt);
    }
}
