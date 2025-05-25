package com.pcagrade.order.service;

import com.pcagrade.order.dto.DashboardStats;
import com.pcagrade.order.entity.PrioriteCommande;
import com.pcagrade.order.entity.StatutCommande;
import com.pcagrade.order.repository.CommandeRepository;
import com.pcagrade.order.repository.EmployeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;

@Service
public class DashboardService {

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private EmployeRepository employeRepository;

    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();

        // Statistiques générales
        stats.setTotalOrders(commandeRepository.count());
        stats.setPendingOrders(commandeRepository.countByStatut(StatutCommande.EN_ATTENTE));
        stats.setScheduledOrders(commandeRepository.countByStatut(StatutCommande.PLANIFIEE));
        stats.setCompletedOrders(commandeRepository.countByStatut(StatutCommande.TERMINEE));
        stats.setOverdueOrders((long) commandeRepository.findCommandesEnRetard(LocalDateTime.now()).size());
        stats.setActiveEmployees((long) employeRepository.findByActifTrue().size());

        // Temps moyen de traitement (simulé pour l'instant)
        stats.setAverageProcessingTimeHours(24.5);

        // Graphique des commandes par jour (30 derniers jours)
        Map<LocalDate, Long> dailyChart = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);

            long count = commandeRepository.findCommandesByPeriode(startOfDay, endOfDay).size();
            dailyChart.put(date, count);
        }
        stats.setDailyOrdersChart(dailyChart);

        // Commandes par priorité
        Map<String, Long> priorityMap = new HashMap<>();
        priorityMap.put("HAUTE", commandeRepository.findByStatut(StatutCommande.EN_ATTENTE).stream()
                .filter(c -> c.getPriorite() == PrioriteCommande.HAUTE).count());
        priorityMap.put("MOYENNE", commandeRepository.findByStatut(StatutCommande.EN_ATTENTE).stream()
                .filter(c -> c.getPriorite() == PrioriteCommande.MOYENNE).count());
        priorityMap.put("BASSE", commandeRepository.findByStatut(StatutCommande.EN_ATTENTE).stream()
                .filter(c -> c.getPriorite() == PrioriteCommande.BASSE).count());
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