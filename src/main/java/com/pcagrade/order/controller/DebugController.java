package com.pcagrade.order.controller;

import com.pcagrade.order.entity.Employe;
import com.pcagrade.order.entity.Order;
import com.pcagrade.order.entity.Planification;
import com.pcagrade.order.service.EmployeService;
import com.pcagrade.order.service.OrderService;
import com.pcagrade.order.service.PlanificationService;
import com.pcagrade.order.ulid.Ulid;
import com.pcagrade.order.ulid.UlidConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
public class DebugController {

    @Autowired
    EmployeService employeService;
    @Autowired
    OrderService orderService;
    @Autowired
    PlanificationService planificationService;

    @GetMapping("/debug/ulid")
    public String debugUlid() {
        StringBuilder sb = new StringBuilder();

        sb.append("=== DEBUG ULID COMPLET ===\n");

        // Test plusieurs ULID
        for (int i = 0; i < 3; i++) {
            sb.append("\n--- Test ").append(i + 1).append(" ---\n");

            Ulid testUlid = Ulid.generate();
            sb.append("ULID: ").append(testUlid.toString()).append("\n");
            sb.append("ULID length: ").append(testUlid.toString().length()).append("\n");

            byte[] bytes = testUlid.getBytes();
            sb.append("Bytes length: ").append(bytes.length).append("\n");
            sb.append("Bytes hex: ").append(bytesToHex(bytes)).append("\n");

            // Test converter
            UlidConverter converter = new UlidConverter();
            byte[] convertedBytes = converter.convertToDatabaseColumn(testUlid);
            sb.append("Converter bytes length: ").append(convertedBytes != null ? convertedBytes.length : "null").append("\n");

            if (convertedBytes != null && convertedBytes.length != 16) {
                sb.append("🚨 PROBLÈME DÉTECTÉ: bytes != 16\n");
            }
        }

        // Test avec des bytes spécifiques
        sb.append("\n--- Test bytes manuels ---\n");
        byte[] testBytes = new byte[16];
        for (int i = 0; i < 16; i++) {
            testBytes[i] = (byte) i;
        }
        sb.append("Test bytes length: ").append(testBytes.length).append("\n");
        sb.append("Test bytes hex: ").append(bytesToHex(testBytes)).append("\n");

        try {
            Ulid fromTestBytes = Ulid.fromBytes(testBytes);
            sb.append("ULID from test bytes: ").append(fromTestBytes.toString()).append("\n");

            byte[] backToBytes = fromTestBytes.getBytes();
            sb.append("Back to bytes length: ").append(backToBytes.length).append("\n");
            sb.append("Bytes égaux ? ").append(java.util.Arrays.equals(testBytes, backToBytes)).append("\n");
        } catch (Exception e) {
            sb.append("ERREUR avec test bytes: ").append(e.getMessage()).append("\n");
        }

        sb.append("\n==========================");

        return sb.toString().replace("\n", "<br>");
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
            List<Order> orders = orderService.getOrdersATraiter();

            if (employes.isEmpty() || orders.isEmpty()) {
                return "Pas d'employé ou de commande disponible pour le test";
            }

            Employe employe = employes.get(0);
            Order order = orders.get(0);

            System.out.println("=== TEST CRÉATION PLANIFICATION ===");
            System.out.println("Employé ID: " + employe.getIdAsString());
            System.out.println("Commande ID: " + order.getIdAsString());

            // Créer une planification simple
            Planification planification = new Planification(
                    order,               // ✅ Ulid reste Ulid pour orderId
                    employe,               // ✅ Ulid reste Ulid pour employeId
                    LocalDate.now().plusDays(1),   // Date de demain
                    LocalTime.of(9, 0),           // 9h00
                    120                           // 2 heures
            );

            System.out.println("Planification créée, tentative de sauvegarde...");
            System.out.println("Order ID: " + planification.getOrderId());
            System.out.println("Employé ID: " + planification.getEmployeId());

            // Sauvegarder
            Planification saved = planificationService.creerPlanification(planification);

            System.out.println("✅ Planification sauvegardée avec ID: " + saved.getIdAsString());
            return "✅ Planification créée avec succès! ID: " + saved.getIdAsString();

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