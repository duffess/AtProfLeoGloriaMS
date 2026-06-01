package com.example.supply_service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/supplies")
public class SupplyController {

    private final LoyaltyClient loyaltyClient;

    public SupplyController(LoyaltyClient loyaltyClient) {
        this.loyaltyClient = loyaltyClient;
    }

    @PostMapping("/checkout")
    @CircuitBreaker(name = "loyaltyCircuitBreaker", fallbackMethod = "loyaltyFallback")
    public String processSupply() {
        // 1. Simula o registro do abastecimento no PostgreSQL...
        
        // 2. Chama o Loyalty Service via Feign (Resiliência aplicada aqui)
        String loyaltyResponse = loyaltyClient.getCustomerPoints("12345");
        
        return "Abastecimento de R$ 200,00 registrado com sucesso! Integração: [" + loyaltyResponse + "]";
    }

    // Método de Fallback se o loyalty-service cair ou demorar muito
    public String loyaltyFallback(Throwable throwable) {
        return "Abastecimento de R$ 200,00 registrado com sucesso! (AVISO: Sistema de fidelidade indisponível no momento. Pontos salvos na fila para processamento futuro).";
    }
}
