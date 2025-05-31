package com.pcagrade.order.controller;// PlanificationController.java

import com.pcagrade.order.entity.Employe;
import com.pcagrade.order.entity.Order;
import com.pcagrade.order.entity.Planification;
import com.pcagrade.order.service.AlgorithmePlanificationService;
import com.pcagrade.order.service.EmployeService;
import com.pcagrade.order.service.OrderService;
import com.pcagrade.order.service.PlanificationService;
import com.pcagrade.order.ulid.Ulid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/planifications")
@CrossOrigin(origins = "*")
public class PlanificationController {

    @Autowired
    private PlanificationService planificationService;
    @Autowired
    private EmployeService employeService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private AlgorithmePlanificationService algorithmePlanificationService;

    @GetMapping("/date/{date}")
    public ResponseEntity<List<Planification>> getPlanificationsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Planification> planifications = planificationService.getPlanificationsByDate(date);
        return ResponseEntity.ok(planifications);
    }

    @GetMapping("/employe/{employeId}")
    public ResponseEntity<List<Planification>> getPlanificationsByEmploye(
            @PathVariable Ulid employeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        Ulid employeUlid = Ulid.fromString(employeId.toString());
        List<Planification> planifications =planificationService.getPlanificationsByEmployeEtPeriode(employeUlid, debut, fin);

        return ResponseEntity.ok(planifications);
    }

    @GetMapping("/periode")
    public ResponseEntity<List<Map<String, Object>>> getPlanificationsByPeriode(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        try {
            List<Planification> planifications = planificationService.getPlanificationsByPeriode(debut, fin);

            List<Map<String, Object>> result = planifications.stream().map(p -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", p.getId().toString());                    // ✅ Convertir ULID en String
                map.put("datePlanifiee", p.getDatePlanifiee());
                map.put("heureDebut", p.getHeureDebut());
                map.put("dureeMinutes", p.getDureeMinutes());
                map.put("terminee", p.getTerminee());

                // Order avec vérification ✅
                Map<String, Object> order = new HashMap<>();
                if (p.getOrder() != null) {
                    order.put("id", p.getOrderId().toString());
                    order.put("numeroCommande", p.getOrder().getNumCommande());
                    order.put("nombreCartes", p.getOrder().getNbDescellements() != null ? p.getOrder().getNbDescellements() : 0);
                    order.put("status", p.getOrder().getStatus());
                    order.put("delai", p.getOrder().getDelai());
                    order.put("retard", p.getOrder().getRetard());
                    order.put("reference", p.getOrder().getReference());
                } else {
                    // Données par défaut si order manquant
                    order.put("id", "");
                    order.put("numeroCommande", "Order inconnue");
                    order.put("nombreCartes", 0);
                    order.put("status", 0);
                    order.put("delai", "");
                    order.put("retard", false);
                    order.put("reference", "");
                }
                map.put("order", order);  // ✅ "order" au lieu de "commande"

                // Employé avec vérification (inchangé)
                Map<String, Object> employe = new HashMap<>();
                if (p.getEmploye() != null) {
                    employe.put("id", p.getEmploye().getId().toString());  // ✅ Convertir ULID en String
                    employe.put("nom", p.getEmploye().getNom());
                    employe.put("prenom", p.getEmploye().getPrenom());
                    employe.put("email", p.getEmploye().getEmail());
                } else {
                    // Données par défaut si employé manquant
                    employe.put("id", "");
                    employe.put("nom", "Employé");
                    employe.put("prenom", "Inconnu");
                    employe.put("email", "inconnu@example.com");
                }
                map.put("employe", employe);

                return map;
            }).collect(Collectors.toList());

            // Log pour debug (adapté)
            System.out.println("Nombre de planifications trouvées: " + result.size());
            result.forEach(r -> {
                Map<String, Object> emp = (Map<String, Object>) r.get("employe");
                Map<String, Object> ord = (Map<String, Object>) r.get("order");  // ✅ "order" au lieu de "commande"
                System.out.println("Planification: " + r.get("id") +
                        " - Employé: " + emp.get("prenom") + " " + emp.get("nom") +
                        " - Order: " + ord.get("numeroCommande"));
            });

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @PostMapping
    public ResponseEntity<Planification> creerPlanification(@Valid @RequestBody Planification planification) {
        try {
            Planification nouvellePlanification = planificationService.creerPlanification(planification);
            return ResponseEntity.ok(nouvellePlanification);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/terminer")
    public ResponseEntity<String> terminerPlanification(@PathVariable Ulid id) {
        try {
            planificationService.marquerPlanificationTerminee(id);
            return ResponseEntity.ok("Planification terminée");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/charge")
    public ResponseEntity<Map<String, Object>> getChargeParEmploye(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        Map<String, Object> charge = planificationService.getChargeParEmploye(debut, fin);
        return ResponseEntity.ok(charge);
    }

    @PostMapping("/planifier-automatique")
    public ResponseEntity<AlgorithmePlanificationService.RapportPlanification> planifierAutomatique() {
        try {
            AlgorithmePlanificationService.RapportPlanification rapport =
                    algorithmePlanificationService.planifierCommandes();
            return ResponseEntity.ok(rapport);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/test-planification")
    public ResponseEntity<Map<String, Object>> testPlanification() {
        Map<String, Object> result = new HashMap<>();

        try {
            // Tester l'accès aux employés
            List<Employe> employes = employeService.getTousEmployes();
            result.put("nombreEmployes", employes.size());
            result.put("employesActifs", employeService.getEmployesActifs().size());

            // Tester l'accès aux commandes
            List<Order> ordersATraiter = orderService.getOrdersATraiter();
            result.put("commandesATraiter", ordersATraiter.size());

            result.put("status", "OK");

        } catch (Exception e) {
            result.put("status", "ERREUR");
            result.put("erreur", e.getMessage());
            result.put("stackTrace", e.getClass().getSimpleName());
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/test-simple")
    public ResponseEntity<Map<String, Object>> testSimple() {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("🔥 Test simple démarré");

            List<Order> orders = orderService.getOrdersATraiter();
            List<Employe> employes = employeService.getEmployesActifs();

            result.put("commandesTrouvees", orders.size());
            result.put("employesTrouves", employes.size());
            result.put("status", "OK");

            // Test d'une commande
            if (!orders.isEmpty()) {
                Order premiere = orders.get(0);
                result.put("premiereCommande", Map.of(
                        "numero", premiere.getNumCommande(),
                        "dateLimite", premiere.getDateLimite().toString(),
                        "tempsEstime", premiere.getTempsEstimeMinutes()
                ));
            }

            System.out.println("✅ Test simple réussi");

        } catch (Exception e) {
            System.err.println("❌ Erreur test simple: " + e.getMessage());
            e.printStackTrace();
            result.put("status", "ERREUR");
            result.put("erreur", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

}

