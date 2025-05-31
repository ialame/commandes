package com.pcagrade.order.controller;

import com.pcagrade.order.dto.DashboardStats;
import com.pcagrade.order.service.DashboardService;
import com.pcagrade.order.service.EmployeService;
import com.pcagrade.order.service.OrderService;
import com.pcagrade.order.service.PlanificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private OrderService commandeService;

    @Autowired
    private EmployeService employeService;

    @Autowired
    private PlanificationService planificationService;

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        DashboardStats stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        Map<String, Object> overview = new HashMap<>();

        // Statistiques des commandes
        Map<String, Object> commandesStats = new HashMap<>();
        commandesStats.put("enAttente", commandeService.getNombreCommandesEnAttente());
        commandesStats.put("enCours", commandeService.getNombreCommandesEnCours());
        commandesStats.put("terminees", commandeService.getNombreCommandesTerminees());
        commandesStats.put("enRetard", commandeService.getCommandesEnRetard().size());

        overview.put("commandes", commandesStats);

        // Statistiques des employés
        Map<String, Object> employesStats = new HashMap<>();
        employesStats.put("total", employeService.getTousEmployes().size());
        employesStats.put("actifs", employeService.getEmployesActifs().size());

        overview.put("employes", employesStats);

        // Charge de travail cette semaine
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        Map<String, Object> chargeStats = planificationService.getChargeParEmploye(startOfWeek, endOfWeek);
        overview.put("chargeTravail", chargeStats);

        return ResponseEntity.ok(overview);
    }
}