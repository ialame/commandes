package com.pcagrade.order.service;

import com.pcagrade.order.dto.DashboardStats;
import com.pcagrade.order.repository.EmployeRepository;
import com.pcagrade.order.repository.OrderRepository;
import com.pcagrade.order.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

@Service
public class DashboardService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EmployeRepository employeRepository;

    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();

        // Statistiques générales
        stats.setTotalOrders(orderRepository.count());
        stats.setPendingOrders(orderRepository.countByStatut(0));      // ✅ 0 = EN_ATTENTE
        stats.setScheduledOrders(orderRepository.countByStatut(1));    // ✅ 1 = PLANIFIEE
        stats.setCompletedOrders(orderRepository.countByStatut(2));    // ✅ 2 = TERMINEE
        stats.setOverdueOrders((long) orderRepository.findCommandesEnRetard(Instant.now()).size()); // ✅ Instant au lieu de LocalDateTime
        stats.setActiveEmployees((long) employeRepository.findByActifTrue().size());

        // Temps moyen de traitement (simulé pour l'instant)
        stats.setAverageProcessingTimeHours(24.5);

        // Graphique des commandes par jour (30 derniers jours)
        Map<LocalDate, Long> dailyChart = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);

            // Convertir LocalDateTime en Instant pour Order
            Instant startOfDay = date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
            Instant endOfDay = date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

            long count = orderRepository.findCommandesByPeriode(startOfDay, endOfDay).size();
            dailyChart.put(date, count);
        }
        stats.setDailyOrdersChart(dailyChart);

        // Commandes par priorité (basé sur les champs Order)
        Map<String, Long> priorityMap = new HashMap<>();
        List<Order> ordersEnAttente = orderRepository.findByStatus(0); // Orders en attente

        priorityMap.put("HAUTE", ordersEnAttente.stream()
                .filter(o -> "HAUTE".equals(o.getPrioriteString())).count());
        priorityMap.put("MOYENNE", ordersEnAttente.stream()
                .filter(o -> "MOYENNE".equals(o.getPrioriteString())).count());
        priorityMap.put("BASSE", ordersEnAttente.stream()
                .filter(o -> "BASSE".equals(o.getPrioriteString())).count());
        stats.setOrdersByPriority(priorityMap);

        // Charge de travail par employé (simulé)
        Map<String, Integer> workloadMap = new HashMap<>();
        employeRepository.findByActifTrue().forEach(employe -> {
            String employeName = employe.getPrenom() + " " + employe.getNom();
            // Simuler une charge entre 60 et 100%
            workloadMap.put(employeName, 60 + (int)(Math.random() * 40));
        });
        stats.setEmployeeWorkload(workloadMap);

        return stats;
    }
}