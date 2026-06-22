package com.example.supply_service;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Table("supply")
public class Supply {

    @Id
    private Long id;

    private Double amount;
    private Double literage;
    private String customerId;
    private LocalDateTime timestamp;

    public Supply() {
    }

    public Supply(Double amount, Double literage, String customerId) {
        this.amount = amount;
        this.literage = literage;
        this.customerId = customerId;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getLiterage() {
        return literage;
    }

    public void setLiterage(Double literage) {
        this.literage = literage;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
