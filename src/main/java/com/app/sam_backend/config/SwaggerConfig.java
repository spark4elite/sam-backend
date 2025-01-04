package com.app.sam_backend.config;

import org.springdoc.api.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenApiCustomiser customiseOpenApi() {
        return openApi -> openApi.getServers().add(new io.swagger.v3.oas.models.servers.Server()
                .url("https://sam-backend-production.up.railway.app")
                .description("Production Server"));
    }
}
