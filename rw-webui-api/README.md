# rw-webui-api

Spring Boot + Vert.x WebSocket API for the RisingWave POC UI.

Non-servlet application (`web-application-type: none`). A Vert.x `HttpServer` handles all
WebSocket traffic on port **8082** at path **`/ws`**.

## Component layout

| Class | Role |
|---|---|
| `VertxConfig` | Declares `Vertx` and `ObjectMapper` beans (JavaTimeModule, ISO-8601 dates) |
| `VertxWebSocketServer` | `@PostConstruct` starts the Vert.x HTTP server; rejects non-`/ws` paths |
| `SessionRegistry` | `ConcurrentHashMap<sessionId, WsSession>` — register / updateUsername / remove |
| `WsSession` | Record: `sessionId`, `username`, `ServerWebSocket`, `connectedAt` |
| `WsMessageHandler` | Dispatches `AUTH` and `QUERY` messages; runs JDBC on `vertx.executeBlocking()` |
| `RisingWaveQueryService` | `SEARCH_ALPHA_BETA / SEARCH_ALPHA / SEARCH_BETA` with `COUNT(*) OVER()` for SSRM total row count; `SESSIONS` list |

## Wire protocol

JSON messages over a single WebSocket connection.

### Client → Server

```jsonc
// Authenticate — send once after connecting; sets the session username
{ "type": "AUTH", "username": "alex" }

// Query joined alpha-beta rows (ag-Grid SSRM params)
{
  "type": "QUERY",
  "id": "r1",
  "action": "SEARCH_ALPHA_BETA",
  "params": { "startRow": 0, "endRow": 100, "alphaName": "foo", "betaTitle": "bar" }
}

// Query entity_alpha rows
{
  "type": "QUERY",
  "id": "r2",
  "action": "SEARCH_ALPHA",
  "params": { "startRow": 0, "endRow": 100, "alphaName": "foo" }
}

// Query entity_beta rows
{
  "type": "QUERY",
  "id": "r3",
  "action": "SEARCH_BETA",
  "params": { "startRow": 0, "endRow": 100, "betaTitle": "bar" }
}

// List active WebSocket sessions
{ "type": "QUERY", "id": "r4", "action": "SESSIONS" }
```

### Server → Client

```jsonc
// Auth confirmation
{ "type": "AUTH_OK", "sessionId": "uuid", "username": "alex" }

// Query result — data + total row count for ag-Grid SSRM
{ "type": "QUERY_RESULT", "id": "r1", "data": [...], "rowCount": 4200, "elapsedMs": 38 }

// Error
{ "type": "ERROR", "id": "r1", "message": "Unknown action: FOO" }
```

`startRow` / `endRow` in query params map directly to ag-Grid's `IServerSideGetRowsRequest`,
so the Angular datasource can forward them as-is. `rowCount` is derived from
`COUNT(*) OVER()` — a single SQL pass, no separate count query.

## Running locally

```bash
./gradlew :rw-webui-api:bootRun
```

Connect with any WebSocket client at `ws://localhost:8082/ws`.
