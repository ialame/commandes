package com.pcagrade.order.service;

import com.github.f4b6a3.ulid.Ulid;
import com.pcagrade.order.entity.Commande;
import com.pcagrade.order.repository.CommandeRepository;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class CommandeService {

    @Autowired
    private EntityManager entityManager;


    @Autowired
    private CommandeRepository commandeRepository;

    public List<Commande> getToutesCommandes() {
        return commandeRepository.findAll();
    }

    public Optional<Commande> getCommandeById(Ulid id) {
        return commandeRepository.findById(id);
    }

    public Optional<Commande> getCommandeByNumero(String numeroCommande) {
        return commandeRepository.findByNumeroCommande(numeroCommande);
    }

    public Commande creerCommande(Commande commande) {
        // L'ID sera généré automatiquement par @PrePersist
        return commandeRepository.save(commande);
    }

    public Commande mettreAJourCommande(Commande commande) {
        // La date de modification sera mise à jour par @PreUpdate
        return commandeRepository.save(commande);
    }

    public void supprimerCommande(Ulid id) {
        commandeRepository.deleteById(id);
    }

    public List<Commande> getCommandesNonAssignees() {
        return commandeRepository.findCommandesNonAssignees(1); // status = 1
    }

    public List<Commande> getOrdersATraiter() {
        // Retourner les commandes non assignées avec status = 1
        return commandeRepository.findCommandesNonAssignees(1);
    }

    public void marquerCommePlanifie(Ulid commandeId) {
        Optional<Commande> commande = commandeRepository.findById(commandeId);
        if (commande.isPresent()) {
            Commande c = commande.get();
            c.setStatus(2); // ou le status approprié pour "planifié"
            commandeRepository.save(c);
        }
    }

    public long getNombreCommandesEnAttente() {
        return commandeRepository.countByStatus(1);
    }

    public long getNombreCommandesEnCours() {
        return commandeRepository.countByStatus(2);
    }

    public long getNombreCommandesTerminees() {
        return commandeRepository.countByStatus(3);
    }


    // Méthode principale : charger les commandes à partir d'une date donnée
    public List<Map<String, Object>> getCommandesAPlanifierDepuisDate(int jour, int mois, int annee) {
        String sql = """
                SELECT HEX(o.id) as id, o.num_commande, o.date, o.prix_total, 
           o.temps_estime_minutes, o.nombre_cartes, o.priorite_string,
           COUNT(cco.card_certification_id) as nombre_cartes_reel
    FROM `order` o
    LEFT JOIN card_certification_order cco ON o.id = cco.order_id
    WHERE o.date >= ?
    GROUP BY o.id, o.num_commande, o.date, o.prix_total, 
             o.temps_estime_minutes, o.nombre_cartes, o.priorite_string
    ORDER BY o.date ASC
    """;
        List<Map<String, Object>> commandesDeBase = new ArrayList<>(); // ✅ DÉCLARATION ICI

        try {
            Query query = entityManager.createNativeQuery(sql);
            LocalDateTime dateDebut = LocalDateTime.of(annee, mois, jour, 0, 0, 0);
            query.setParameter(1, dateDebut);

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            for (Object[] row : results) {
                Map<String, Object> commande = new HashMap<>();
                commande.put("id", row[0] != null ? row[0].toString() : ""); // ← Maintenant c'est déjà une String
                commande.put("numeroCommande", row[1] != null ? row[1].toString() : "");
                commande.put("dateReception", row[2]); // date, pas date_reception
                commande.put("prixTotal", row[3]);
                commande.put("tempsEstimeMinutes", row[4] != null ? row[4] : 120);
                commande.put("nombreCartes", row[5] != null ? row[5] : 0);
                commande.put("priorite", row[6] != null ? row[6].toString() : "NORMALE");
                commande.put("nombreCartesReel", row[7] != null ? ((Number) row[7]).intValue() : 0);

                commandesDeBase.add(commande);
            }

            System.out.println("✅ " + commandesDeBase.size() + " commandes à planifier depuis le " +
                    jour + "/" + mois + "/" + annee);

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement commandes à planifier: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // Retourner liste vide en cas d'erreur
        }

        // ========== REMPLACER CETTE PARTIE DANS getCommandesAPlanifierDepuisDate() ==========

// APRÈS avoir récupéré les commandes de base, enrichir avec les cartes
        List<Map<String, Object>> commandesEnrichies = new ArrayList<>();

        for (Map<String, Object> commande : commandesDeBase) {
            try {
                String commandeId = (String) commande.get("id");

                if (commandeId != null && !commandeId.isEmpty()) {
                    // ✅ CONVERSION PROPRE HEX STRING → ULID
                    Ulid commandeUlid = stringToUlid(commandeId);

                    // Récupérer la commande avec ses cartes
                    Query carteQuery = entityManager.createQuery(
                            "SELECT c FROM Commande c " +
                                    "LEFT JOIN FETCH c.cardCertifications cc " +
                                    "LEFT JOIN FETCH cc.card card " +
                                    "LEFT JOIN FETCH card.translations " +
                                    "WHERE c.id = :id"
                    );

                    carteQuery.setParameter("id", commandeUlid);

                    // ✅ SOLUTION - gestion des cas où la commande n'existe pas
                    Optional<Commande> commandeOpt = carteQuery.getResultStream().findFirst();

                    if (commandeOpt.isPresent()) {
                        Commande commandeComplete = commandeOpt.get();
                        commande.put("nombreCartesReelles", commandeComplete.getNombreCartes());
                        commande.put("nomsCartes", commandeComplete.getNomsCartes());

                        System.out.println("🎴 Commande " + commande.get("numeroCommande") +
                                " - Cartes: " + commandeComplete.getNombreCartes());
                    } else {
                        System.out.println("⚠️ Commande " + commande.get("numeroCommande") + " non trouvée dans l'entité JPA");
                        commande.put("nombreCartesReelles", 0);
                        commande.put("nomsCartes", new ArrayList<>());
                    }
                } else {
                    System.out.println("⚠️ ID commande vide pour: " + commande.get("numeroCommande"));
                    commande.put("nombreCartesReelles", 0);
                    commande.put("nomsCartes", new ArrayList<>());
                }

            } catch (Exception e) {
                System.out.println("⚠️ Erreur cartes pour commande " + commande.get("numeroCommande") + ": " + e.getMessage());
                commande.put("nombreCartesReelles", 0);
                commande.put("nomsCartes", new ArrayList<>());
            }

            commandesEnrichies.add(commande);
        }
// Après l'enrichissement des cartes, calculer la durée
        for (Map<String, Object> commande : commandesEnrichies) {
            Integer nombreCartes = (Integer) commande.get("nombreCartesReelles");
            if (nombreCartes == null || nombreCartes == 0) {
                // Si pas de cartes trouvées, essayer de calculer
                String commandeId = (String) commande.get("id");
                if (commandeId != null) {
                    Query countQuery = entityManager.createNativeQuery(
                            "SELECT COUNT(cco.card_certification_id) " +
                                    "FROM card_certification_order cco " +
                                    "WHERE cco.order_id = UNHEX(?)"
                    );
                    countQuery.setParameter(1, commandeId);
                    Number result = (Number) countQuery.getSingleResult();
                    nombreCartes = result != null ? result.intValue() : 0;
                    commande.put("nombreCartesReelles", nombreCartes);
                }
            }

            // Calculer la durée : 3 minutes par carte, minimum 15 minutes
            Integer dureeCalculee = Math.max(nombreCartes * 3, 15);
            commande.put("tempsEstimeMinutes", dureeCalculee);

            System.out.println("📊 Commande " + commande.get("numeroCommande") +
                    " : " + nombreCartes + " cartes → " + dureeCalculee + " minutes");
        }
        return commandesEnrichies;
        }
    // Variante avec période (entre deux dates)
    public List<Map<String, Object>> getCommandesPeriode(
            int jourDebut, int moisDebut, int anneeDebut,
            int jourFin, int moisFin, int anneeFin) {
        try {
            Query query = entityManager.createNativeQuery(
                    "SELECT " +
                            "  HEX(id) as id_hex, " +
                            "  num_commande, " +
                            "  priorite_string, " +
                            "  temps_estime_minutes, " +
                            "  date_limite, " +
                            "  delai, " +
                            "  status, " +
                            "  date as date_creation, " +
                            "  date_modification, " +
                            "  HEX(employe_id) as employe_id_hex " +
                            "FROM commandes_db.`order` " +
                            "WHERE date >= ? AND date <= ? " +
                            "ORDER BY date DESC " +
                            "LIMIT 2000"
            );

            LocalDateTime dateDebut = LocalDateTime.of(anneeDebut, moisDebut, jourDebut, 0, 0, 0);
            LocalDateTime dateFin = LocalDateTime.of(anneeFin, moisFin, jourFin, 23, 59, 59);

            query.setParameter(1, dateDebut);
            query.setParameter(2, dateFin);

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();
            List<Map<String, Object>> commandes = new ArrayList<>();

            for (Object[] row : results) {
                Map<String, Object> commande = new HashMap<>();
                commande.put("id", row[0]);
                commande.put("numeroCommande", row[1]);
                commande.put("priorite", row[2] != null ? row[2] : "NORMALE");
                commande.put("tempsEstimeMinutes", row[3] != null ? row[3] : 120);
                commande.put("dateLimite", row[4]);
                commande.put("delai", row[5]);
                commande.put("status", row[6]);
                commande.put("dateCreation", row[7]);
                commande.put("dateModification", row[8]);
                commande.put("employeId", row[9]);
                commandes.add(commande);
            }

            System.out.println("✅ " + commandes.size() + " commandes chargées du " +
                    jourDebut + "/" + moisDebut + "/" + anneeDebut + " au " +
                    jourFin + "/" + moisFin + "/" + anneeFin);
            return commandes;

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement commandes par période: " + e.getMessage());
            throw e;
        }
    }



    // Méthode de compatibilité (garde l'ancienne méthode)
    public List<Map<String, Object>> getToutesCommandesNative() {
        // Par défaut, charger depuis le 1er mai 2025
        return getCommandesAPlanifierDepuisDate(1, 5, 2025);
    }

    // Dans CommandeService.java, ajoutez cette méthode de test
    public void testCartesPourCommande(String numeroCommande) {
        Query query = entityManager.createQuery(
                "SELECT c FROM Commande c " +
                        "LEFT JOIN FETCH c.cardCertifications cc " +
                        "LEFT JOIN FETCH cc.card card " +
                        "LEFT JOIN FETCH card.translations " +
                        "WHERE c.numeroCommande = :numero"
        );

        query.setParameter("numero", numeroCommande);

        try {
            Commande commande = (Commande) query.getSingleResult();
            System.out.println("🔍 Commande: " + commande.getNumeroCommande());
            System.out.println("📦 Nombre de cartes: " + commande.getNombreCartes());
            System.out.println("🎴 Noms des cartes: " + commande.getNomsCartes());
        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage());
        }
    }


    // ========== MÉTHODE UTILITAIRE À AJOUTER DANS CommandeService.java ==========

    /**
     * Convertit une chaîne hexadécimale (UUID sans tirets) vers un ULID
     *
     * @param hexString ex: "0196894BD992D78614399D7C1035125B"
     * @return ULID correspondant pour utilisation avec JPA
     */
    private Ulid hexStringToUlid(String hexString) {
        if (hexString == null || hexString.length() != 32) {
            throw new IllegalArgumentException("Hex string must be 32 characters long");
        }

        try {
            // Convertir "0196894BD992D78614399D7C1035125B"
            // vers "0196894b-d992-d786-1439-9d7c1035125b"
            String formatted = hexString.toLowerCase()
                    .replaceAll("(.{8})(.{4})(.{4})(.{4})(.{12})", "$1-$2-$3-$4-$5");

            // Créer UUID puis ULID
            UUID uuid = UUID.fromString(formatted);
            return Ulid.from(uuid);

        } catch (Exception e) {
            System.err.println("❌ Erreur conversion hex vers ULID: " + hexString + " - " + e.getMessage());
            throw new IllegalArgumentException("Invalid hex string: " + hexString, e);
        }
    }

    private Ulid stringToUlid(String hexString) {
        String formatted = hexString.toLowerCase()
                .replaceAll("(.{8})(.{4})(.{4})(.{4})(.{12})", "$1-$2-$3-$4-$5");
        return Ulid.from(UUID.fromString(formatted));
    }
}
