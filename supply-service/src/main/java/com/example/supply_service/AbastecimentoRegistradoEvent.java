package com.example.supply_service;

public class AbastecimentoRegistradoEvent {
    
    private Long supplyId;
    private String customerId;
    private int points;

    public AbastecimentoRegistradoEvent() {
    }

    public AbastecimentoRegistradoEvent(Long supplyId, String customerId, int points) {
        this.supplyId = supplyId;
        this.customerId = customerId;
        this.points = points;
    }

    public Long getSupplyId() {
        return supplyId;
    }

    public void setSupplyId(Long supplyId) {
        this.supplyId = supplyId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
