package com.alex.rwp.api.ws;

import com.alex.rwp.api.query.PagedResult;
import com.alex.rwp.api.query.RisingWaveQueryService;
import com.alex.rwp.api.session.SessionRegistry;
import com.alex.rwp.api.session.WsSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class WsMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(WsMessageHandler.class);

    private final Vertx vertx;
    private final SessionRegistry registry;
    private final RisingWaveQueryService queryService;
    private final ObjectMapper mapper;

    public WsMessageHandler(Vertx vertx, SessionRegistry registry,
                            RisingWaveQueryService queryService, ObjectMapper mapper) {
        this.vertx = vertx;
        this.registry = registry;
        this.queryService = queryService;
        this.mapper = mapper;
    }

    public void handle(ServerWebSocket ws) {
        String sessionId = UUID.randomUUID().toString();
        registry.register(sessionId, ws);
        log.info("WS connected: sessionId={}", sessionId);

        ws.textMessageHandler(text -> dispatch(sessionId, ws, text));
        ws.closeHandler(v -> {
            registry.remove(sessionId);
            log.info("WS closed: sessionId={}", sessionId);
        });
        ws.exceptionHandler(err -> {
            registry.remove(sessionId);
            log.warn("WS error: sessionId={} err={}", sessionId, err.getMessage());
        });
    }

    private void dispatch(String sessionId, ServerWebSocket ws, String text) {
        JsonNode msg;
        try {
            msg = mapper.readTree(text);
        } catch (JsonProcessingException e) {
            sendError(ws, null, "Invalid JSON: " + e.getMessage());
            return;
        }

        String type = msg.path("type").asText();
        switch (type) {
            case "AUTH"  -> handleAuth(sessionId, ws, msg);
            case "QUERY" -> handleQuery(sessionId, ws, msg);
            default      -> sendError(ws, null, "Unknown message type: " + type);
        }
    }

    private void handleAuth(String sessionId, ServerWebSocket ws, JsonNode msg) {
        String username = msg.path("username").asText("anonymous");
        registry.updateUsername(sessionId, username);
        log.info("WS auth: sessionId={} username={}", sessionId, username);
        send(ws, Map.of("type", "AUTH_OK", "sessionId", sessionId, "username", username));
    }

    private void handleQuery(String sessionId, ServerWebSocket ws, JsonNode msg) {
        String id     = msg.path("id").asText("");
        String action = msg.path("action").asText("");
        JsonNode p    = msg.path("params");

        final int startRow = p.path("startRow").asInt(0);
        final int endRow   = Math.max(p.path("endRow").asInt(100), startRow + 1);

        switch (action) {
            case "SEARCH_ALPHA_BETA" -> vertx.executeBlocking(() ->
                queryService.searchAlphaBeta(
                    startRow, endRow,
                    nullIfBlank(p.path("alphaName").asText()),
                    nullIfBlank(p.path("betaTitle").asText())
                )
            ).onSuccess(r  -> sendResult(ws, id, r))
             .onFailure(err -> sendError(ws, id, err.getMessage()));

            case "SEARCH_ALPHA" -> vertx.executeBlocking(() ->
                queryService.searchAlpha(
                    startRow, endRow,
                    nullIfBlank(p.path("alphaName").asText())
                )
            ).onSuccess(r  -> sendResult(ws, id, r))
             .onFailure(err -> sendError(ws, id, err.getMessage()));

            case "SEARCH_BETA" -> vertx.executeBlocking(() ->
                queryService.searchBeta(
                    startRow, endRow,
                    nullIfBlank(p.path("betaTitle").asText())
                )
            ).onSuccess(r  -> sendResult(ws, id, r))
             .onFailure(err -> sendError(ws, id, err.getMessage()));

            case "SESSIONS" -> {
                var sessions = registry.all().stream()
                    .map(s -> Map.of(
                        "sessionId",   s.sessionId(),
                        "username",    s.username(),
                        "connectedAt", s.connectedAt().toString()
                    ))
                    .toList();
                send(ws, Map.of(
                    "type",      "QUERY_RESULT",
                    "id",        id,
                    "data",      sessions,
                    "rowCount",  (long) sessions.size(),
                    "elapsedMs", 0L
                ));
            }

            default -> sendError(ws, id, "Unknown action: " + action);
        }
    }

    private void sendResult(ServerWebSocket ws, String id, PagedResult r) {
        var out = new LinkedHashMap<String, Object>();
        out.put("type",      "QUERY_RESULT");
        out.put("id",        id);
        out.put("data",      r.data());
        out.put("rowCount",  r.rowCount());
        out.put("elapsedMs", r.elapsedMs());
        send(ws, out);
    }

    private void sendError(ServerWebSocket ws, String id, String message) {
        send(ws, Map.of("type", "ERROR", "id", id != null ? id : "", "message", message));
    }

    private void send(ServerWebSocket ws, Object payload) {
        try {
            ws.writeTextMessage(mapper.writeValueAsString(payload))
              .onFailure(err -> log.warn("WS write failed: {}", err.getMessage()));
        } catch (JsonProcessingException e) {
            log.error("Serialization error", e);
        }
    }

    private static String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
