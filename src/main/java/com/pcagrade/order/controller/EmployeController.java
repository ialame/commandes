package com.pcagrade.order.controller;

import com.github.f4b6a3.ulid.Ulid;
import com.pcagrade.order.entity.Employe;
import com.pcagrade.order.service.EmployeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employes")
@CrossOrigin(origins = "*")
public class EmployeController {

    @Autowired
    private EmployeService employeService;


    /**
     * Employé par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getEmployeById(@PathVariable Ulid id) {
        try {

            Optional<Employe> employeOpt = employeService.trouverParId(id);

            if (employeOpt.isPresent()) {
                Employe e = employeOpt.get();
                Map<String, Object> result = new HashMap<>();
                result.put("id", e.getId().toString());
                result.put("nom", e.getNom());
                result.put("prenom", e.getPrenom());
                result.put("email", e.getEmail());
                result.put("heuresTravailParJour", e.getHeuresTravailParJour());
                result.put("actif", e.getActif());

                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération employé: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Créer un employé
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> creerEmploye(@Valid @RequestBody Map<String, Object> employeData) {
        try {
            Employe employe = new Employe();
            employe.setNom((String) employeData.get("nom"));
            employe.setPrenom((String) employeData.get("prenom"));
            employe.setEmail((String) employeData.get("email"));

            Integer heures = (Integer) employeData.get("heuresTravailParJour");
            employe.setHeuresTravailParJour(heures != null ? heures : 8);

            Boolean actif = (Boolean) employeData.get("actif");
            employe.setActif(actif != null ? actif : true);

            Employe nouvelEmploye = employeService.sauvegarder(employe);

            Map<String, Object> result = new HashMap<>();
            result.put("id", nouvelEmploye.getId().toString());
            result.put("nom", nouvelEmploye.getNom());
            result.put("prenom", nouvelEmploye.getPrenom());
            result.put("email", nouvelEmploye.getEmail());
            result.put("heuresTravailParJour", nouvelEmploye.getHeuresTravailParJour());
            result.put("actif", nouvelEmploye.getActif());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("❌ Erreur création employé: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Mettre à jour un employé
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> mettreAJourEmploye(
            @PathVariable Ulid id,
            @Valid @RequestBody Map<String , Object> employeData) {
        try {
            Optional<Employe> employeOpt = employeService.trouverParId(id);

            if (employeOpt.isPresent()) {
                Employe employe = employeOpt.get();

                if (employeData.containsKey("nom")) {
                    employe.setNom((String) employeData.get("nom"));
                }
                if (employeData.containsKey("prenom")) {
                    employe.setPrenom((String) employeData.get("prenom"));
                }
                if (employeData.containsKey("email")) {
                    employe.setEmail((String) employeData.get("email"));
                }
                if (employeData.containsKey("heuresTravailParJour")) {
                    employe.setHeuresTravailParJour((Integer) employeData.get("heuresTravailParJour"));
                }
                if (employeData.containsKey("actif")) {
                    employe.setActif((Boolean) employeData.get("actif"));
                }

                Employe employeMisAJour = employeService.sauvegarder(employe);

                Map<String, Object> result = new HashMap<>();
                result.put("id", employeMisAJour.getId().toString());
                result.put("nom", employeMisAJour.getNom());
                result.put("prenom", employeMisAJour.getPrenom());
                result.put("email", employeMisAJour.getEmail());
                result.put("heuresTravailParJour", employeMisAJour.getHeuresTravailParJour());
                result.put("actif", employeMisAJour.getActif());

                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur mise à jour employé: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Supprimer un employé
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> supprimerEmploye(@PathVariable Ulid id) {
        try {
            Optional<Employe> employeOpt = employeService.trouverParId(id);

            if (employeOpt.isPresent()) {
                employeService.supprimer(id);

                Map<String, Object> result = new HashMap<>();
                result.put("message", "Employé supprimé avec succès");
                result.put("id", id);

                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur suppression employé: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }


    // Endpoint principal - tous les employés
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getTousEmployes() {
        try {
            List<Map<String, Object>> employes = employeService.getTousEmployesNative();
            return ResponseEntity.ok(employes);
        } catch (Exception e) {
            System.err.println("❌ Erreur récupération employés: " + e.getMessage());
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    // Nouveau endpoint - employés actifs uniquement
    @GetMapping("/actifs")
    public ResponseEntity<List<Map<String, Object>>> getEmployesActifs() {
        try {
            List<Map<String, Object>> employes = employeService.getTousEmployesActifs();
            return ResponseEntity.ok(employes);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    // Employés disponibles pour une date donnée
    @GetMapping("/disponibles")
    public ResponseEntity<List<Map<String, Object>>> getEmployesDisponibles(
            @RequestParam String date) {  // Format: YYYY-MM-DD
        try {
            LocalDate datePlanification = LocalDate.parse(date);
            List<Map<String, Object>> employes = employeService.getEmployesDisponibles(datePlanification);
            return ResponseEntity.ok(employes);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    // Statistiques des employés
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatsEmployes() {
        try {
            long totalEmployes = employeService.getTousEmployesNative().size();
            long employesActifs = employeService.getNombreEmployesActifs();

            Map<String, Object> stats = new HashMap<>();
            stats.put("total", totalEmployes);
            stats.put("actifs", employesActifs);
            stats.put("inactifs", totalEmployes - employesActifs);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}