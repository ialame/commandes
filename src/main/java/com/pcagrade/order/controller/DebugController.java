package com.pcagrade.order.controller;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;
import com.pcagrade.order.entity.Commande;
import com.pcagrade.order.entity.Employe;
import com.pcagrade.order.entity.Planification;
import com.pcagrade.order.service.EmployeService;
import com.pcagrade.order.service.CommandeService;
import com.pcagrade.order.service.PlanificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
public class DebugController {

    @Autowired
    EmployeService employeService;
    @Autowired
    CommandeService orderService;
    @Autowired
    PlanificationService planificationService;

    @GetMapping("/debug/ulid")
    public String debugUlid() {
        StringBuilder sb = new StringBuilder();

        sb.append("=== DEBUG ULID COMPLET ===\n");

        // Test plusieurs ULID
        for (int i = 0; i < 3; i++) {
            sb.append("\n--- Test ").append(i + 1).append(" ---\n");

            Ulid testUlid = UlidCreator.getMonotonicUlid();
            sb.append("ULID: ").append(testUlid.toString()).append("\n");
            sb.append("ULID length: ").append(testUlid.toString().length()).append("\n");


            // Test converter

            UUID convertedBytes = testUlid.toUuid();

            System.out.println("Converted bytes: " + convertedBytes);
        }
        return sb.toString();
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }


    // Ajoutez ce test dans un contrôleur ou service pour débugger

    // Ajoutez ce test dans un contrôleur ou service pour débugger

    // Ajoutez ce test dans un contrôleur ou service pour débugger

    // Ajoutez ce test dans un contrôleur ou service pour débugger

    @GetMapping("/debug/test-planification")
    public String testPlanificationCreation() {
        try {
            // Récupérer un employé et une commande existants
            List<Employe> employes = employeService.getEmployesActifs();
            List<Commande> orders = orderService.getOrdersATraiter();

            if (employes.isEmpty() || orders.isEmpty()) {
                return "Pas d'employé ou de commande disponible pour le test";
            }

            Employe employe = employes.get(0);
            Commande order = orders.get(0);

            System.out.println("=== TEST CRÉATION PLANIFICATION ===");
            System.out.println("Employé ID: " + employe.getId());
            System.out.println("Commande ID: " + order.getId());

            // Créer une planification simple
            Planification planification = new Planification(
                    order.getId(),               // ✅ Ulid reste Ulid pour orderId
                    employe.getId(),               // ✅ Ulid reste Ulid pour employeId
                    LocalDate.now().plusDays(1),   // Date de demain
                    LocalTime.of(9, 0),           // 9h00
                    120                           // 2 heures
            );

            System.out.println("Planification créée, tentative de sauvegarde...");
            System.out.println("Commande ID: " + planification.getOrderId());
            System.out.println("Employé ID: " + planification.getEmployeId());

            // Sauvegarder
            Planification saved = planificationService.creerPlanification(planification);

            System.out.println("✅ Planification sauvegardée avec ID: " + saved.getId());
            return "✅ Planification créée avec succès! ID: " + saved.getId();

        } catch (Exception e) {
            System.out.println("❌ ERREUR lors de la création de planification:");
            System.out.println("Message: " + e.getMessage());
            System.out.println("=== STACK TRACE COMPLÈTE ===");
            e.printStackTrace();
            System.out.println("=============================");
            return "❌ ERREUR: " + e.getMessage() + " - Voir logs pour détails";
        }
    }
}