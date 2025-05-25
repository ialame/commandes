package com.pcagrade.order.service;

import com.pcagrade.order.dto.DashboardStats;
import com.pcagrade.order.entity.OrderStatus;
import com.pcagrade.order.entity.PriorityLevel;
import com.pcagrade.order.repository.EmployeeRepository;
import com.pcagrade.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;

@Service
public class DashboardService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();

        // Statistiques générales
        stats.setTotalOrders(orderRepository.count());
        stats.setPendingOrders(orderRepository.findByStatus(OrderStatus.PENDING).size());
        stats.setScheduledOrders(orderRepository.findByStatus(OrderStatus.SCHEDULED).size());
        stats.setCompletedOrders(orderRepository.findByStatus(OrderStatus.COMPLETED).size());
        stats.setOverdueOrders(orderRepository.findOverdueOrders(LocalDateTime.now()).size());
        stats.setActiveEmployees(employeeRepository.findByIsActiveTrue().size());

        // Temps moyen de traitement
        Double avgTime = orderRepository.findAverageProcessingTimeInHours();
        stats.setAverageProcessingTimeHours(avgTime != null ? avgTime : 0.0);

        // Graphique des commandes par jour (30 derniers jours)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Object[]> dailyStats = orderRepository.findDailyOrderStats(thirtyDaysAgo, LocalDateTime.now());
        Map<LocalDate, Long> dailyChart = new LinkedHashMap<>();
        for (Object[] stat : dailyStats) {
            LocalDate date = ((java.sql.Date) stat[0]).toLocalDate();
            Long count = ((Number) stat[1]).longValue();
            dailyChart.put(date, count);
        }
        stats.setDailyOrdersChart(dailyChart);

        // Commandes par priorité
        Map<String, Long> priorityMap = new HashMap<>();
        for (PriorityLevel priority : PriorityLevel.values()) {
            long count = orderRepository.findByPriorityLevelOrderByCreatedAtAsc(priority).size();
            priorityMap.put(priority.getDescription(), count);
        }
        stats.setOrdersByPriority(priorityMap);

        // Charge de travail par employé
        List<Object[]> workloadData = employeeRepository.findEmployeesWithWorkloadCount();
        Map<String, Integer> workloadMap = new HashMap<>();
        for (Object[] data : workloadData) {
        }
    }
}
