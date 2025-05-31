package com.pcagrade.order.controller;

import com.pcagrade.order.entity.Order;
import com.pcagrade.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/commandes")
@CrossOrigin(origins = "*")
public class CommandeController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getToutesCommandes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate depuis) {

        // Si vos commandes sont en 2024 par exemple
        if (depuis == null) {
            depuis = LocalDate.of(2024, 1, 1);  // Ajustez selon vos données réelles
        }

        // Convertir LocalDate en LocalDateTime (début de journée)
        LocalDateTime depuisDateTime = depuis.atStartOfDay();

        // Récupérer les commandes filtrées par date
        List<Order> commandes = orderService.getCommandesDepuis(depuisDateTime);

        // Conversion en Map pour éviter les problèmes de sérialisation ULID
        List<Map<String, Object>> result = commandes.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getIdAsString()); // Utilise la méthode de BaseEntity
            map.put("numeroCommande", c.getNumeroCommande());
            map.put("nombreCartes", c.getNombreCartes());
            map.put("prixTotal", c.getPrixTotal());
            map.put("priorite", c.getPriorite().toString());
            map.put("statut", c.getStatut().toString());
            map.put("dateCreation", c.getDateCreation());
            map.put("dateLimite", c.getDateLimite());
            map.put("dateDebutTraitement", c.getDateDebutTraitement());
            map.put("dateFinTraitement", c.getDateFinTraitement());
            map.put("tempsEstimeMinutes", c.getTempsEstimeMinutes());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCommandeById(@PathVariable String id) {
        try {
            Order commande = orderService.getCommandeById(id);

            Map<String, Object> result = new HashMap<>();
            result.put("id", commande.getIdAsString());
            result.put("numeroCommande", commande.getNumeroCommande());
            result.put("nombreCartes", commande.getNombreCartes());
            result.put("prixTotal", commande.getPrixTotal());
            result.put("priorite", commande.getPriorite().toString());
            result.put("statut", commande.getStatut().toString());
            result.put("dateCreation", commande.getDateCreation());
            result.put("dateLimite", commande.getDateLimite());
            result.put("dateDebutTraitement", commande.getDateDebutTraitement());
            result.put("dateFinTraitement", commande.getDateFinTraitement());
            result.put("tempsEstimeMinutes", commande.getTempsEstimeMinutes());

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> creerCommande(@Valid @RequestBody Map<String, Object> commandeData) {
        try {
            // Créer une nouvelle commande à partir des données reçues
            Order commande = new Order();
            commande.setNumeroCommande((String) commandeData.get("numeroCommande"));
            commande.setNombreCartes((Integer) commandeData.get("nombreCartes"));

            // Gestion du prix (peut être Number ou String)
            Object prixObj = commandeData.get("prixTotal");
            if (prixObj instanceof Number) {
                commande.setPrixTotal(new java.math.BigDecimal(prixObj.toString()));
            } else if (prixObj instanceof String) {
                commande.setPrixTotal(new java.math.BigDecimal((String) prixObj));
            }

            Order nouvelleCommande = orderService.creerCommande(commande);

            // Retourner la réponse avec l'ID en string
            Map<String, Object> result = new HashMap<>();
            result.put("id", nouvelleCommande.getIdAsString());
            result.put("numeroCommande", nouvelleCommande.getNumeroCommande());
            result.put("nombreCartes", nouvelleCommande.getNombreCartes());
            result.put("prixTotal", nouvelleCommande.getPrixTotal());
            result.put("priorite", nouvelleCommande.getPriorite().toString());
            result.put("statut", nouvelleCommande.getStatut().toString());
            result.put("dateCreation", nouvelleCommande.getDateCreation());
            result.put("dateLimite", nouvelleCommande.getDateLimite());
            result.put("tempsEstimeMinutes", nouvelleCommande.getTempsEstimeMinutes());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/a-traiter")
    public ResponseEntity<List<Map<String, Object>>> getCommandesATraiter() {
        List<Order> commandes = orderService.getCommandesATraiter();

        List<Map<String, Object>> result = commandes.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getIdAsString());
            map.put("numeroCommande", c.getNumeroCommande());
            map.put("nombreCartes", c.getNombreCartes());
            map.put("prixTotal", c.getPrixTotal());
            map.put("priorite", c.getPriorite().toString());
            map.put("statut", c.getStatut().toString());
            map.put("dateCreation", c.getDateCreation());
            map.put("dateLimite", c.getDateLimite());
            map.put("tempsEstimeMinutes", c.getTempsEstimeMinutes());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/en-retard")
    public ResponseEntity<List<Map<String, Object>>> getCommandesEnRetard() {
        List<Order> commandes = orderService.getCommandesEnRetard();

        List<Map<String, Object>> result = commandes.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getIdAsString());
            map.put("numeroCommande", c.getNumeroCommande());
            map.put("nombreCartes", c.getNombreCartes());
            map.put("prixTotal", c.getPrixTotal());
            map.put("priorite", c.getPriorite().toString());
            map.put("statut", c.getStatut().toString());
            map.put("dateCreation", c.getDateCreation());
            map.put("dateLimite", c.getDateLimite());
            map.put("tempsEstimeMinutes", c.getTempsEstimeMinutes());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/commencer")
    public ResponseEntity<String> commencerCommande(@PathVariable String id) {
        try {
            orderService.marquerCommandeCommencee(id);
            return ResponseEntity.ok("Commande commencée");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/terminer")
    public ResponseEntity<String> terminerCommande(@PathVariable String id) {
        try {
            orderService.marquerCommandeTerminee(id);
            return ResponseEntity.ok("Commande terminée");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiques() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("enAttente", orderService.getNombreCommandesEnAttente());
        stats.put("enCours", orderService.getNombreCommandesEnCours());
        stats.put("terminees", orderService.getNombreCommandesTerminees());
        return ResponseEntity.ok(stats);
    }

    // Endpoint de test pour les ULIDs
    @GetMapping("/test-ulid")
    public ResponseEntity<Map<String, Object>> testUlid() {
        Order commande = new Order();

        Map<String, Object> result = new HashMap<>();
        result.put("ulidString", commande.getIdAsString());
        //result.put("timestamp", commande.getCreationTimestamp());
        //result.put("instant", commande.getCreationInstant().toString());
        result.put("now", java.time.Instant.now().toString());

        return ResponseEntity.ok(result);
    }
}