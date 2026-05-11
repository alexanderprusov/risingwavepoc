package com.alex.rwp.api.config;

import com.alex.rwp.api.ws.WsMessageHandler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class VertxWebSocketServer {

    private static final Logger log = LoggerFactory.getLogger(VertxWebSocketServer.class);

    private final Vertx vertx;
    private final WsMessageHandler handler;

    @Value("${rw-webui-api.ws.port:8082}")
    private int port;

    @Value("${rw-webui-api.ws.path:/ws}")
    private String path;

    private HttpServer server;

    public VertxWebSocketServer(Vertx vertx, WsMessageHandler handler) {
        this.vertx = vertx;
        this.handler = handler;
    }

    @PostConstruct
    public void start() throws Exception {
        server = vertx.createHttpServer(
            new HttpServerOptions().setMaxWebSocketFrameSize(1024 * 1024)
        );
        server.webSocketHandler(ws -> {
            if (!path.equals(ws.path())) {
                ws.reject(404);
                return;
            }
            handler.handle(ws);
        });

        server.listen(port)
              .toCompletionStage()
              .toCompletableFuture()
              .get(10, TimeUnit.SECONDS);

        log.info("WebSocket server listening on port {} at {}", port, path);
    }

    @PreDestroy
    public void stop() {
        if (server != null) server.close();
        vertx.close();
    }
}
