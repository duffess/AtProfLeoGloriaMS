package com.example.loyalty_service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loyalty")
public class LoyaltyController {

    @GetMapping("/{customerId}/points")
    public String getPoints(@PathVariable String customerId) {
        // Simulação de banco de dados para evitar erro se o MongoDB não estiver rodando
        return "Cliente " + customerId + " possui 1500 pontos no sistema de fidelidade!";
    }
}
