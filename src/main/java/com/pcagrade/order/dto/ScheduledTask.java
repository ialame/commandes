package com.pcagrade.order.dto;

import com.pcagrade.order.entity.Order;

import java.time.LocalDateTime;

public class ScheduledTask {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Order order;

    public ScheduledTask(LocalDateTime startTime, LocalDateTime endTime, Order order) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.order = order;
    }

    // Getters et setters
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public int getDurationMinutes() {
        return (int) java.time.Duration.between(startTime, endTime).toMinutes();
    }
}
