package com.pcagrade.order.entity;

public enum OrderStatus {
    PENDING("En attente"),
    SCHEDULED("Planifiée"),
    IN_PROGRESS("En cours"),
    COMPLETED("Terminée"),
    CANCELLED("Annulée");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}
