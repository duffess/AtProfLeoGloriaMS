package com.example.supply_service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// O nome "loyalty-service" deve ser exatamente o nome registrado no Eureka
@FeignClient(name = "loyalty-service")
public interface LoyaltyClient {

    @GetMapping("/api/loyalty/{customerId}/points")
    String getCustomerPoints(@PathVariable("customerId") String customerId);
}
