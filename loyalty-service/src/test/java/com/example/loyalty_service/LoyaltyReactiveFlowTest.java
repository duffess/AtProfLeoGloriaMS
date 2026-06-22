package com.example.loyalty_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class LoyaltyReactiveFlowTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testAddPointsReactiveFlow() {
        // Testa a rota POST usando WebTestClient (Reativo e Não-bloqueante)
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/loyalty/test-flow-123/add-points")
                        .queryParam("points", 300)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(response -> {
                    assert response.contains("test-flow-123");
                    assert response.contains("300 pontos");
                });
                
        // Testa a rota GET garantindo que os pontos foram salvos via R2DBC
        webTestClient.get()
                .uri("/api/loyalty/test-flow-123/points")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(response -> {
                    assert response.contains("300 pontos");
                });
    }
}
