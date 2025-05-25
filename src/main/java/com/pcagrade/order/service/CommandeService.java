package com.pcagrade.order.service;// CommandeService.java

import com.pcagrade.order.entity.Commande;
import com.pcagrade.order.entity.StatutCommande;
import com.pcagrade.order.repository.CommandeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;

@Service
@Transactional
public class CommandeService {

    @Autowired
    private CommandeRepository commandeRepository;

    public Commande creerCommande(Commande commande) {
        // Générer un numéro de commande unique si non fourni
        if (commande.getNumeroCommande() == null || commande.getNumeroCommande().isEmpty()) {
            commande.setNumeroCommande(genererNumeroCommande());
        }
        return commandeRepository.save(commande);
    }

    public List<Commande> getCommandesATraiter() {
        List<StatutCommande> statuts = Arrays.asList(StatutCommande.EN_ATTENTE, StatutCommande.PLANIFIEE);
        return commandeRepository.findByStatutInOrderByPrioriteAndDate(statuts);
    }

    public List<Commande> getCommandesEnRetard() {
        return commandeRepository.findCommandesEnRetard(LocalDateTime.now());
    }

    public void marquerCommandeCommencee(Long commandeId) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
        commande.setStatut(StatutCommande.EN_COURS);
        commande.setDateDebutTraitement(LocalDateTime.now());
        commandeRepository.save(commande);
    }

    public void marquerCommandeTerminee(Long commandeId) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
        commande.setStatut(StatutCommande.TERMINEE);
        commande.setDateFinTraitement(LocalDateTime.now());
        commandeRepository.save(commande);
    }

    public List<Commande> getToutesCommandes() {
        return commandeRepository.findAll();
    }

    public Commande getCommandeById(Long id) {
        return commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
    }

    private String genererNumeroCommande() {
        return "CMD-" + System.currentTimeMillis();
    }

    public Long getNombreCommandesEnAttente() {
        return commandeRepository.countByStatut(StatutCommande.EN_ATTENTE);
    }

    public Long getNombreCommandesEnCours() {
        return commandeRepository.countByStatut(StatutCommande.EN_COURS);
    }

    public Long getNombreCommandesTerminees() {
        return commandeRepository.countByStatut(StatutCommande.TERMINEE);
    }
}
