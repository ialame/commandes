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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<List<Planification>> getPlanificationsByPeriode(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        List<Planification> planifications = planificationService.getPlanificationsByPeriode(debut, fin);
        return ResponseEntity.ok(planifications);
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

