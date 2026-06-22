package com.example.loyalty_service;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface CustomerLoyaltyRepository extends R2dbcRepository<CustomerLoyalty, Long> {
    Mono<CustomerLoyalty> findByCustomerId(String customerId);
}
