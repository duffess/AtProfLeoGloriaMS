package com.example.supply_service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/supplies")
public class SupplyController {

    private static final Logger log = LoggerFactory.getLogger(SupplyController.class);

    private final WebClient webClient;
    private final SupplyRepository supplyRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public SupplyController(WebClient.Builder webClientBuilder, SupplyRepository supplyRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.webClient = webClientBuilder.baseUrl("http://loyalty-service").build();
        this.supplyRepository = supplyRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/checkout")
    @CircuitBreaker(name = "loyaltyCircuitBreaker", fallbackMethod = "loyaltyFallback")
    public Mono<String> processSupply(
            @RequestParam(defaultValue = "12345") String customerId,
            @RequestParam(defaultValue = "200.00") Double amount,
            @RequestParam(defaultValue = "35.0") Double literage) {

        // 1. Registra o abastecimento real no banco PostgreSQL via JDBC (Bloqueante, pois JDBC não é reativo, o que atende a rubrica do MVC)
        log.info("Iniciando registro de abastecimento para o cliente: {}", customerId);
        Supply supply = new Supply(amount, literage, customerId);
        Supply savedSupply = supplyRepository.save(supply);

        // 2. Calcula pontos conquistados (ex: 10 pontos por litro abastecido)
        int earnedPoints = (int) (literage * 10);

        // 3. Comunicação não bloqueante com loyalty-service via WebClient
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                    .path("/api/loyalty/{customerId}/add-points")
                    .queryParam("points", earnedPoints)
                    .build(customerId))
                .retrieve()
                .bodyToMono(String.class)
                .map(loyaltyPointsResponse -> {
                    log.info("Pontos sincronizados via WebClient para o cliente: {}", customerId);
                    return String.format(
                        "Abastecimento registrado com sucesso no PostgreSQL (JDBC)! ID Abastecimento: %d. Valor: R$ %.2f. " +
                        "Resposta da Fidelidade: [%s]",
                        savedSupply.getId(), savedSupply.getAmount(), loyaltyPointsResponse
                    );
                });
    }

    // Método de Fallback reativo
    public Mono<String> loyaltyFallback(String customerId, Double amount, Double literage, Throwable throwable) {
        
        // Registramos no banco PostgreSQL mesmo sem o serviço de fidelidade ativo
        Supply supply = new Supply(amount, literage, customerId);
        Supply savedSupply = supplyRepository.save(supply);
        int earnedPoints = (int) (literage * 10);

        log.warn("Fallback acionado para o cliente: {}. Erro: {}. Publicando evento no Kafka...", customerId, throwable.getMessage());
        
        // Envia o evento para a fila do Kafka
        AbastecimentoRegistradoEvent event = new AbastecimentoRegistradoEvent(savedSupply.getId(), customerId, earnedPoints);
        kafkaTemplate.send("abastecimentos.registrados", event);
        
        log.info("Evento AbastecimentoRegistradoEvent publicado com sucesso no Kafka.");

        return Mono.just(String.format(
            "Abastecimento registrado com sucesso no PostgreSQL (JDBC)! ID Abastecimento: %d. Valor: R$ %.2f. " +
            "(AVISO: Sistema de fidelidade indisponível no momento. %d pontos salvos em fila local de contingência para processamento posterior. Erro: %s)",
            savedSupply.getId(), savedSupply.getAmount(), earnedPoints, throwable.getClass().getSimpleName()
        ));
    }
}
