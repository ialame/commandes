package com.pcagrade.order.controller;// PlanificationController.java

import com.pcagrade.order.entity.Planification;
import com.pcagrade.order.service.AlgorithmePlanificationService;
import com.pcagrade.order.service.CommandeService;
import com.pcagrade.order.service.PlanificationService;
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
    private com.pcagrade.order.service.PlanificationService planificationService;

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
            @PathVariable Long employeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        List<Planification> planifications = planificationService.getPlanificationsByEmployeEtPeriode(employeId, debut, fin);
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
                map.put("id", p.getId());
                map.put("datePlanifiee", p.getDatePlanifiee());
                map.put("heureDebut", p.getHeureDebut());
                map.put("dureeMinutes", p.getDureeMinutes());
                map.put("terminee", p.getTerminee());

                // Commande avec vérification
                Map<String, Object> commande = new HashMap<>();
                if (p.getCommande() != null) {
                    commande.put("id", p.getCommande().getId());
                    commande.put("numeroCommande", p.getCommande().getNumeroCommande());
                    commande.put("nombreCartes", p.getCommande().getNombreCartes());
                    commande.put("prixTotal", p.getCommande().getPrixTotal());
                    commande.put("priorite", p.getCommande().getPriorite().toString());
                } else {
                    // Données par défaut si commande manquante
                    commande.put("id", 0);
                    commande.put("numeroCommande", "Commande inconnue");
                    commande.put("nombreCartes", 0);
                    commande.put("prixTotal", 0.0);
                    commande.put("priorite", "BASSE");
                }
                map.put("commande", commande);

                // Employé avec vérification
                Map<String, Object> employe = new HashMap<>();
                if (p.getEmploye() != null) {
                    employe.put("id", p.getEmploye().getId());
                    employe.put("nom", p.getEmploye().getNom());
                    employe.put("prenom", p.getEmploye().getPrenom());
                    employe.put("email", p.getEmploye().getEmail());
                } else {
                    // Données par défaut si employé manquant
                    employe.put("id", 0);
                    employe.put("nom", "Employé");
                    employe.put("prenom", "Inconnu");
                    employe.put("email", "inconnu@example.com");
                }
                map.put("employe", employe);

                return map;
            }).collect(Collectors.toList());

            // Log pour debug
            System.out.println("Nombre de planifications trouvées: " + result.size());
            result.forEach(r -> {
                Map<String, Object> emp = (Map<String, Object>) r.get("employe");
                Map<String, Object> cmd = (Map<String, Object>) r.get("commande");
                System.out.println("Planification: " + r.get("id") +
                        " - Employé: " + emp.get("prenom") + " " + emp.get("nom") +
                        " - Commande: " + cmd.get("numeroCommande"));
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
    public ResponseEntity<String> terminerPlanification(@PathVariable Long id) {
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
}

