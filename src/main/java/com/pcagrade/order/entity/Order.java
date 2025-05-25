package com.pcagrade.order.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @NotNull
    @Column(name = "card_count", nullable = false)
    @Min(1)
    private Integer cardCount = 20; // par défaut 20 cartes

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority_level", nullable = false)
    private PriorityLevel priorityLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "scheduled_date")
    private LocalDateTime scheduledDate;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @ManyToOne
    @JoinColumn(name = "assigned_employee_id")
    private Employee assignedEmployee;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes; // cardCount * 5 minutes

    // Constructeurs
    public Order() {}

    public Order(String customerName, BigDecimal price, Integer cardCount) {
        this.customerName = customerName;
        this.price = price;
        this.cardCount = cardCount != null ? cardCount : 20;
        this.priorityLevel = determinePriorityLevel(price);
        this.dueDate = calculateDueDate();
        this.estimatedDurationMinutes = this.cardCount * 5;
    }

    // Méthodes utilitaires
    private PriorityLevel determinePriorityLevel(BigDecimal price) {
        if (price.compareTo(new BigDecimal("100")) <= 0) {
            return PriorityLevel.LOW;
        } else if (price.compareTo(new BigDecimal("500")) <= 0) {
            return PriorityLevel.MEDIUM;
        } else {
            return PriorityLevel.HIGH;
        }
    }

    private LocalDateTime calculateDueDate() {
        LocalDateTime now = LocalDateTime.now();
        switch (this.priorityLevel) {
            case HIGH:
                return now.plusWeeks(1); // d1 = 1 semaine
            case MEDIUM:
                return now.plusWeeks(2); // d2 = 2 semaines
            case LOW:
            default:
                return now.plusWeeks(4); // d3 = 4 semaines
        }
    }

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public Integer getCardCount() { return cardCount; }
    public void setCardCount(Integer cardCount) {
        this.cardCount = cardCount;
        this.estimatedDurationMinutes = cardCount * 5;
    }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) {
        this.price = price;
        this.priorityLevel = determinePriorityLevel(price);
        this.dueDate = calculateDueDate();
    }

    public PriorityLevel getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(PriorityLevel priorityLevel) { this.priorityLevel = priorityLevel; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public LocalDateTime getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDateTime scheduledDate) { this.scheduledDate = scheduledDate; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Employee getAssignedEmployee() { return assignedEmployee; }
    public void setAssignedEmployee(Employee assignedEmployee) { this.assignedEmployee = assignedEmployee; }

    public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }
}