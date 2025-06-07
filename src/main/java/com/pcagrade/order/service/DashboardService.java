package com.pcagrade.order.service;

import com.pcagrade.order.repository.CommandeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private CommandeRepository commandeRepository;

    /**
     * ✅ MÉTHODE MANQUANTE - getDashboardStats()
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Statistiques des commandes
            stats.put("commandesEnAttente", commandeRepository.countByStatus(1));
            stats.put("commandesEnCours", commandeRepository.countByStatus(2));
            stats.put("commandesTerminees", commandeRepository.countByStatus(3));
            stats.put("totalCommandes", commandeRepository.count());

            // Commandes du mois
            LocalDateTime debutMois = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime finMois = LocalDateTime.now();
            stats.put("commandesDuMois",
                    commandeRepository.findCommandesByPeriode(debutMois, finMois).size());

            // Commandes récentes (7 derniers jours)
            LocalDateTime depuisUneSemaine = LocalDateTime.now().minusDays(7);
            stats.put("commandesRecentes",
                    commandeRepository.findCommandesRecentes(depuisUneSemaine).size());

            // Commandes en retard
            stats.put("commandesEnRetard", commandeRepository.findDelayedOrders().size());

            stats.put("status", "success");
            stats.put("timestamp", LocalDateTime.now());

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération stats dashboard: " + e.getMessage());
            stats.put("status", "error");
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    /**
     * Version alternative si vous avez des problèmes avec les autres repositories
     */
    public Map<String, Object> getStatsCommandes() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("enAttente", commandeRepository.countByStatus(1));
        stats.put("enCours", commandeRepository.countByStatus(2));
        stats.put("terminees", commandeRepository.countByStatus(3));
        stats.put("total", commandeRepository.count());

        return stats;
    }

    public long getNombreCommandesTotal() {
        return commandeRepository.count(); // Ça marche
    }
}