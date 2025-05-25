package com.pcagrade.order.entity;

public enum PriorityLevel {
    HIGH(1, "Haute - 1 semaine"),
    MEDIUM(2, "Moyenne - 2 semaines"),
    LOW(4, "Basse - 4 semaines");

    private final int weeksDeadline;
    private final String description;

    PriorityLevel(int weeksDeadline, String description) {
        this.weeksDeadline = weeksDeadline;
        this.description = description;
    }

    public int getWeeksDeadline() { return weeksDeadline; }
    public String getDescription() { return description; }
}