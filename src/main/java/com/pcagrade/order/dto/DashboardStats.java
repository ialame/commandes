package com.pcagrade.order.dto;

import java.util.Map;
import java.time.LocalDate;

public class DashboardStats {
    private long totalOrders;
    private long pendingOrders;
    private long scheduledOrders;
    private long completedOrders;
    private long overdueOrders;
    private int activeEmployees;
    private double averageProcessingTimeHours;
    private Map<LocalDate, Long> dailyOrdersChart;
    private Map<String, Long> ordersByPriority;
    private Map<String, Integer> employeeWorkload;

    // Constructeurs
    public DashboardStats() {}

    // Getters et setters
    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }

    public long getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(long pendingOrders) { this.pendingOrders = pendingOrders; }

    public long getScheduledOrders() { return scheduledOrders; }
    public void setScheduledOrders(long scheduledOrders) { this.scheduledOrders = scheduledOrders; }

    public long getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(long completedOrders) { this.completedOrders = completedOrders; }

    public long getOverdueOrders() { return overdueOrders; }
    public void setOverdueOrders(long overdueOrders) { this.overdueOrders = overdueOrders; }

    public int getActiveEmployees() { return activeEmployees; }
    public void setActiveEmployees(int activeEmployees) { this.activeEmployees = activeEmployees; }

    public double getAverageProcessingTimeHours() { return averageProcessingTimeHours; }
    public void setAverageProcessingTimeHours(double averageProcessingTimeHours) { this.averageProcessingTimeHours = averageProcessingTimeHours; }

    public Map<LocalDate, Long> getDailyOrdersChart() { return dailyOrdersChart; }
    public void setDailyOrdersChart(Map<LocalDate, Long> dailyOrdersChart) { this.dailyOrdersChart = dailyOrdersChart; }

    public Map<String, Long> getOrdersByPriority() { return ordersByPriority; }
    public void setOrdersByPriority(Map<String, Long> ordersByPriority) { this.ordersByPriority = ordersByPriority; }

    public Map<String, Integer> getEmployeeWorkload() { return employeeWorkload; }
    public void setEmployeeWorkload(Map<String, Integer> employeeWorkload) { this.employeeWorkload = employeeWorkload; }
}