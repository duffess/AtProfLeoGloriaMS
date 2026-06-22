package com.example.loyalty_service;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.util.HashSet;
import java.util.Set;

@Table("customer_loyalty")
public class CustomerLoyalty {

    @Id
    private Long id;
    private String customerId;
    private String fullName;
    private int points;
    private Set<Long> processedSupplyIds = new HashSet<>();

    public CustomerLoyalty() {
    }

    public CustomerLoyalty(String customerId, String fullName, int points) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.points = points;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public Set<Long> getProcessedSupplyIds() {
        return processedSupplyIds;
    }

    public void setProcessedSupplyIds(Set<Long> processedSupplyIds) {
        this.processedSupplyIds = processedSupplyIds;
    }
}
