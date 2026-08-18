package com.alex.rwp.config;

import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.openapi.server-url}")
    private String serverUrl;

    @Bean
    public OpenApiCustomizer serverUrlCustomizer() {
        return openApi -> openApi.servers(List.of(new Server().url(serverUrl)));
    }
}
