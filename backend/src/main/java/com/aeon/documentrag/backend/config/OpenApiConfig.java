package com.aeon.documentrag.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI documentRagOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Document RAG API")
                        .description("REST API for document ingestion, semantic retrieval, and conversational RAG over ChromaDB.")
                        .version("v1")
                        .contact(new Contact().name("Codex Scaffold")))
                .addServersItem(new Server().url("http://localhost:8080").description("Local backend"));
    }
}
