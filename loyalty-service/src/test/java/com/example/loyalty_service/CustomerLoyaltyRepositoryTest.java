package com.example.loyalty_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;
import java.util.HashSet;
import java.util.Set;

@DataR2dbcTest
@Testcontainers
public class CustomerLoyaltyRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private CustomerLoyaltyRepository repository;

    @Test
    void testSaveAndFindByCustomerId() {
        CustomerLoyalty loyalty = new CustomerLoyalty();
        loyalty.setCustomerId("test-123");
        loyalty.setFullName("Teste User");
        loyalty.setPoints(500);
        
        // Add some processed supply ids
        Set<Long> supplyIds = new HashSet<>();
        supplyIds.add(10L);
        supplyIds.add(20L);
        loyalty.setProcessedSupplyIds(supplyIds);

        repository.save(loyalty)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();

        repository.findByCustomerId("test-123")
                .as(StepVerifier::create)
                .expectNextMatches(saved -> 
                        saved.getCustomerId().equals("test-123") && 
                        saved.getPoints() == 500 &&
                        saved.getProcessedSupplyIds().contains(10L))
                .verifyComplete();
    }
}
