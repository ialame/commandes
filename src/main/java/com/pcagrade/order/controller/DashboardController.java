package com.pcagrade.order.controller;

import com.pcagrade.order.service.CommandeService;
import com.pcagrade.order.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private CommandeService commandeService;

    /**
     * ✅ CORRIGÉ - Retourne Map<String, Object> au lieu de DashboardStats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> stats = dashboardService.getDashboardStats();  // ✅ Retourne déjà une Map
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("❌ Erreur dashboard: " + e.getMessage());
            e.printStackTrace();

            // Retourner un objet d'erreur au lieu de null
            Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "error", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Statistiques simplifiées des commandes
     */
    @GetMapping("/commandes")
    public ResponseEntity<Map<String, Object>> getStatsCommandes() {
        try {
            Map<String, Object> stats = Map.of(
                    "enAttente", commandeService.getNombreCommandesEnAttente(),
                    "enCours", commandeService.getNombreCommandesEnCours(),
                    "terminees", commandeService.getNombreCommandesTerminees(),
                    "status", "success"
            );
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "error", e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Test de connectivité
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = Map.of(
                "status", "OK",
                "message", "Dashboard API is working",
                "timestamp", System.currentTimeMillis()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Version simplifiée du dashboard sans DTO
     */
    @GetMapping("/simple")
    public ResponseEntity<Map<String, Object>> getSimpleStats() {
        try {
            Map<String, Object> stats = Map.of(
                    "commandesEnAttente", commandeService.getNombreCommandesEnAttente(),
                    "commandesEnCours", commandeService.getNombreCommandesEnCours(),
                    "commandesTerminees", commandeService.getNombreCommandesTerminees(),
                    "totalCommandes", commandeService.getToutesCommandes().size(),
                    "status", "success",
                    "timestamp", System.currentTimeMillis()
            );
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "error", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}