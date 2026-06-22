package com.example.supply_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@Testcontainers
public class SupplyRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private SupplyRepository repository;

    @Test
    void testSaveAndFindById() {
        Supply supply = new Supply(250.0, 45.0, "test-123");
        Supply savedSupply = repository.save(supply);

        assertThat(savedSupply.getId()).isNotNull();

        Supply foundSupply = repository.findById(savedSupply.getId()).orElse(null);
        assertThat(foundSupply).isNotNull();
        assertThat(foundSupply.getCustomerId()).isEqualTo("test-123");
        assertThat(foundSupply.getAmount()).isEqualTo(250.0);
    }
}
