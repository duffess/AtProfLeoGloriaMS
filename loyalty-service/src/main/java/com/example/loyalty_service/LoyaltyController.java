package com.example.loyalty_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/loyalty")
public class LoyaltyController {

    private static final Logger log = LoggerFactory.getLogger(LoyaltyController.class);
    private final CustomerLoyaltyRepository repository;

    public LoyaltyController(CustomerLoyaltyRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{customerId}/points")
    public Mono<String> getPoints(@PathVariable String customerId) {
        log.info("Consultando pontos para o cliente: {}", customerId);
        return repository.findByCustomerId(customerId)
                .switchIfEmpty(
                        repository.save(new CustomerLoyalty(customerId, "Guilherme Duffes", 1500))
                                .doOnNext(saved -> log.info("Cliente não encontrado. Criado novo cliente padrão: {}", saved.getCustomerId()))
                )
                .map(loyalty -> "Cliente ID: " + loyalty.getCustomerId() + " (" + loyalty.getFullName() + ") possui " + loyalty.getPoints() + " pontos registrados no PostgreSQL (R2DBC)!");
    }

    @PostMapping("/{customerId}/add-points")
    public Mono<String> addPoints(@PathVariable String customerId, @RequestParam(defaultValue = "100") int points) {
        log.info("Adicionando {} pontos para o cliente: {}", points, customerId);
        return repository.findByCustomerId(customerId)
                .defaultIfEmpty(new CustomerLoyalty(customerId, "Cliente " + customerId, 0))
                .flatMap(loyalty -> {
                    loyalty.setPoints(loyalty.getPoints() + points);
                    return repository.save(loyalty);
                })
                .map(loyalty -> {
                    log.info("Pontos atualizados. Novo saldo do cliente {}: {}", customerId, loyalty.getPoints());
                    return "Sucesso! Adicionados " + points + " pontos ao cliente " + customerId + ". Novo saldo no PostgreSQL (R2DBC): " + loyalty.getPoints();
                });
    }
}
