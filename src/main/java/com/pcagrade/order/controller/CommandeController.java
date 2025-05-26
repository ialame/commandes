package com.pcagrade.order.controller;


import com.pcagrade.order.dto.CommandeDTO;
import com.pcagrade.order.entity.Commande;
import com.pcagrade.order.service.CommandeService;
import com.pcagrade.order.service.EmployeService;
import com.pcagrade.order.service.PlanificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/commandes")
@CrossOrigin(origins = "*")
public class CommandeController {

    @Autowired
    private CommandeService commandeService;

    @GetMapping
    public ResponseEntity<List<CommandeDTO>> getToutesCommandes() {
        List<Commande> commandes = commandeService.getToutesCommandes();

        List<CommandeDTO> commandeDTOs = commandes.stream()
                .map(c -> new CommandeDTO(
                        c.getId(),
                        c.getNumeroCommande(),
                        c.getNombreCartes(),
                        c.getPrixTotal(),
                        c.getPriorite().toString(),
                        c.getStatut().toString(),
                        c.getDateCreation(),
                        c.getDateLimite(),
                        c.getTempsEstimeMinutes()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(commandeDTOs);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Commande> getCommandeById(@PathVariable Long id) {
        try {
            Commande commande = commandeService.getCommandeById(id);
            return ResponseEntity.ok(commande);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Commande> creerCommande(@Valid @RequestBody Commande commande) {
        try {
            Commande nouvelleCommande = commandeService.creerCommande(commande);
            return ResponseEntity.ok(nouvelleCommande);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/a-traiter")
    public ResponseEntity<List<Commande>> getCommandesATraiter() {
        List<Commande> commandes = commandeService.getCommandesATraiter();
        return ResponseEntity.ok(commandes);
    }

    @GetMapping("/en-retard")
    public ResponseEntity<List<Commande>> getCommandesEnRetard() {
        List<Commande> commandes = commandeService.getCommandesEnRetard();
        return ResponseEntity.ok(commandes);
    }

    @PutMapping("/{id}/commencer")
    public ResponseEntity<String> commencerCommande(@PathVariable Long id) {
        try {
            commandeService.marquerCommandeCommencee(id);
            return ResponseEntity.ok("Commande commencée");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/terminer")
    public ResponseEntity<String> terminerCommande(@PathVariable Long id) {
        try {
            commandeService.marquerCommandeTerminee(id);
            return ResponseEntity.ok("Commande terminée");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiques() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("enAttente", commandeService.getNombreCommandesEnAttente());
        stats.put("enCours", commandeService.getNombreCommandesEnCours());
        stats.put("terminees", commandeService.getNombreCommandesTerminees());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/simple")
    public ResponseEntity<List<Map<String, Object>>> getCommandesSimple() {
        List<Commande> commandes = commandeService.getToutesCommandes();

        List<Map<String, Object>> result = commandes.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("numeroCommande", c.getNumeroCommande());
            map.put("nombreCartes", c.getNombreCartes());
            map.put("prixTotal", c.getPrixTotal());
            map.put("priorite", c.getPriorite());
            map.put("statut", c.getStatut());
            map.put("dateCreation", c.getDateCreation());
            map.put("dateLimite", c.getDateLimite());
            map.put("tempsEstimeMinutes", c.getTempsEstimeMinutes());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}

