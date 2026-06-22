package com.example.loyalty_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class LoyaltyKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(LoyaltyKafkaConsumer.class);
    private final CustomerLoyaltyRepository repository;

    public LoyaltyKafkaConsumer(CustomerLoyaltyRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "abastecimentos.registrados", groupId = "loyalty-group")
    public void consumeAbastecimentoRegistradoEvent(AbastecimentoRegistradoEvent event) {
        log.info("Mensagem recebida do Kafka! Evento de Abastecimento Registrado (ID: {}) para o cliente: {}", event.getSupplyId(), event.getCustomerId());
        
        repository.findByCustomerId(event.getCustomerId())
                .defaultIfEmpty(new CustomerLoyalty(event.getCustomerId(), "Cliente " + event.getCustomerId(), 0))
                .flatMap(loyalty -> {
                    // Verificação de Idempotência
                    if (loyalty.getProcessedSupplyIds().contains(event.getSupplyId())) {
                        log.warn("Evento de abastecimento ID {} já foi processado anteriormente. Ignorando para evitar duplicidade de pontos.", event.getSupplyId());
                        return reactor.core.publisher.Mono.empty(); // Retorna vazio se já processou
                    }

                    loyalty.setPoints(loyalty.getPoints() + event.getPoints());
                    loyalty.getProcessedSupplyIds().add(event.getSupplyId());
                    return repository.save(loyalty);
                })
                .subscribe(savedLoyalty -> 
                    log.info("Pontos atualizados com sucesso via Kafka! Cliente {} tem agora {} pontos.", savedLoyalty.getCustomerId(), savedLoyalty.getPoints())
                );
    }
}
