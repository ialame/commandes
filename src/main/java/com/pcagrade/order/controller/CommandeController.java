package com.pcagrade.order.controller;

import com.github.f4b6a3.ulid.Ulid;
import com.pcagrade.order.entity.Commande;
import com.pcagrade.order.repository.CommandeRepository;
import com.pcagrade.order.service.CommandeService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/commandes")
@CrossOrigin(origins = "*")
public class CommandeController {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CommandeService commandeService;
    @Autowired
    private CommandeRepository commandeRepository;

    // Endpoint principal - avec date par défaut (1er mai 2025)
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getCommandes() {
        try {
            List<Map<String, Object>> commandes = commandeService.getCommandesAPlanifierDepuisDate(1, 5, 2025);
            return ResponseEntity.ok(commandes);
        } catch (Exception e) {
            System.err.println("❌ Erreur récupération commandes: " + e.getMessage());
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    // Endpoint avec date paramétrable
    @GetMapping("/depuis")
    public ResponseEntity<List<Map<String, Object>>> getCommandesDepuisDate(
            @RequestParam int jour,
            @RequestParam int mois,
            @RequestParam int annee) {
        try {
            List<Map<String, Object>> commandes = commandeService.getCommandesAPlanifierDepuisDate(jour, mois, annee);
            return ResponseEntity.ok(commandes);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    // Endpoint avec période (entre deux dates)
    @GetMapping("/periode")
    public ResponseEntity<List<Map<String, Object>>> getCommandesPeriode(
            @RequestParam int jourDebut,
            @RequestParam int moisDebut,
            @RequestParam int anneeDebut,
            @RequestParam int jourFin,
            @RequestParam int moisFin,
            @RequestParam int anneeFin) {
        try {
            List<Map<String, Object>> commandes = commandeService.getCommandesPeriode(
                    jourDebut, moisDebut, anneeDebut,
                    jourFin, moisFin, anneeFin
            );
            return ResponseEntity.ok(commandes);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    // Statistiques depuis une date
    @GetMapping("/stats/depuis")
    public ResponseEntity<Map<String, Object>> getStatsDepuisDate(
            @RequestParam int jour,
            @RequestParam int mois,
            @RequestParam int annee) {
        try {
            LocalDateTime dateDebut = LocalDateTime.of(annee, mois, jour, 0, 0, 0);

            Query query = entityManager.createNativeQuery(
                    "SELECT " +
                            "  COUNT(*) as total, " +
                            "  SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as en_attente, " +
                            "  SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as en_cours, " +
                            "  SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) as terminees, " +
                            "  SUM(CASE WHEN delai = 'X' THEN 1 ELSE 0 END) as urgentes, " +
                            "  SUM(CASE WHEN employe_id IS NULL THEN 1 ELSE 0 END) as non_assignees " +
                            "FROM commandes_db.`order` " +
                            "WHERE date >= ?"
            );

            query.setParameter(1, dateDebut);
            Object[] result = (Object[]) query.getSingleResult();

            Map<String, Object> stats = new HashMap<>();
            stats.put("total", result[0]);
            stats.put("enAttente", result[1]);
            stats.put("enCours", result[2]);
            stats.put("terminees", result[3]);
            stats.put("urgentes", result[4]);
            stats.put("nonAssignees", result[5]);
            stats.put("dateDebut", jour + "/" + mois + "/" + annee);

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/{commandeId}/cartes")
    public ResponseEntity<Map<String, Object>> getCartesCommande(@PathVariable String commandeId) {
        try {
            Commande commande = commandeRepository.findById(Ulid.from(commandeId)).orElse(null);
            if (commande == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> result = new HashMap<>();
            result.put("nombreCartes", commande.getNombreCartes());
            result.put("nomsCartes", commande.getNomsCartes());
            result.put("resumeCartes", commande.getResumerCartes());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}