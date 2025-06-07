package com.pcagrade.order.service;

import com.github.f4b6a3.ulid.Ulid;
import com.pcagrade.order.entity.Commande;
import com.pcagrade.order.entity.Planification;
import com.pcagrade.order.repository.PlanificationRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
@Transactional
public class PlanificationService {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlanificationRepository planificationRepository;
    @Autowired
    private CommandeService commandeService;

    @Autowired
    private EmployeService employeService;

    public Map<String, Object> executerPlanificationAutomatique() {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("🔍 Service: Début planification automatique");

            // Récupérer les données
            List<Map<String, Object>> commandes = commandeService.getToutesCommandesNative();
            List<Map<String, Object>> employes = employeService.getTousEmployesNative();

            System.out.println("📦 Commandes: " + commandes.size() + ", 👥 Employés: " + employes.size());

            if (commandes.isEmpty() || employes.isEmpty()) {
                result.put("success", false);
                result.put("message", "Données insuffisantes");
                return result;
            }

            List<Map<String, Object>> planificationsCreees = new ArrayList<>();

            for (int i = 0; i < commandes.size(); i++) {
                Map<String, Object> commande = commandes.get(i);
                Map<String, Object> employe = employes.get(i % employes.size());

                try {
                    String insertSql = "INSERT INTO planification " +
                            "(id, order_id, employe_id, date_planification, heure_debut, duree_minutes, terminee, date_creation, date_modification) " +
                            "VALUES (UNHEX(?), UNHEX(?), UNHEX(?), ?, ?, ?, ?, NOW(), NOW())";

                    String newId = UUID.randomUUID().toString().replace("-", "");
                    String commandeId = ((String) commande.get("id")).replace("-", "");
                    String employeId = ((String) employe.get("id")).replace("-", "");

                    int rowsAffected = entityManager.createNativeQuery(insertSql)
                            .setParameter(1, newId)
                            .setParameter(2, commandeId)
                            .setParameter(3, employeId)
                            .setParameter(4, LocalDate.now().plusDays(1))
                            .setParameter(5, LocalTime.of(8, 0).plusMinutes(i * 75))
                            .setParameter(6, (Integer) commande.get("tempsEstimeMinutes"))
                            .setParameter(7, false)
                            .executeUpdate();

                    if (rowsAffected > 0) {
                        planificationsCreees.add(Map.of(
                                "commande", commande.get("numeroCommande"),
                                "employe", employe.get("prenom") + " " + employe.get("nom")
                        ));
                        System.out.println("✅ Planification " + (i + 1) + " créée");
                    }




                } catch (Exception e) {
                    System.err.println("❌ Erreur planification " + (i + 1) + ": " + e.getMessage());
                }
            }

            result.put("success", true);
            result.put("message", "Planifications créées avec succès");
            result.put("planificationsCreees", planificationsCreees.size());
            result.put("planifications", planificationsCreees);

            return result;

        } catch (Exception e) {
            System.err.println("❌ Erreur service planification: " + e.getMessage());
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }




    // Méthode native adaptée à votre structure
    public List<Map<String, Object>> getToutesPlanificationsNative() {
        try {
            Query query = entityManager.createNativeQuery(
                    "SELECT " +
                            "  HEX(id) as id_hex, " +
                            "  HEX(order_id) as order_id_hex, " +
                            "  HEX(employe_id) as employe_id_hex, " +
                            "  date_planification, " +
                            "  heure_debut, " +
                            "  duree_minutes, " +
                            "  terminee, " +
                            "  date_creation, " +
                            "  date_modification " +
                            "FROM planification " +
                            "ORDER BY date_planification, heure_debut"
            );

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();
            List<Map<String, Object>> planifications = new ArrayList<>();

            for (Object[] row : results) {
                Map<String, Object> planification = new HashMap<>();
                planification.put("id", row[0]);
                planification.put("orderId", row[1]);
                planification.put("employeId", row[2]);
                planification.put("datePlanification", row[3]);
                planification.put("heureDebut", row[4]);
                planification.put("dureeMinutes", row[5]);
                planification.put("terminee", row[6]);
                planification.put("dateCreation", row[7]);
                planification.put("dateModification", row[8]);
                planifications.add(planification);
            }

            return planifications;

        } catch (Exception e) {
            System.err.println("❌ Erreur requête native planifications: " + e.getMessage());
            throw e;
        }
    }



    // ========== MÉTHODES MANQUANTES ==========

    /**
     * Créer une planification (alias pour sauvegarderPlanification)
     */
    public Planification creerPlanification(Planification planification) {
        return planificationRepository.save(planification);
    }

    /**
     * Sauvegarder une planification
     */
    public Planification sauvegarderPlanification(Planification planification) {
        return planificationRepository.save(planification);
    }

    /**
     * Calculer la charge de travail d'un employé pour un jour donné
     */
    public int getChargeEmployeParJour(Ulid employeId, LocalDate date) {
        try {
            // Requête native pour éviter les problèmes de désérialisation
            Query query = entityManager.createNativeQuery(
                    "SELECT COALESCE(SUM(duree_minutes), 0) " +
                            "FROM planification " +
                            "WHERE employe_id = UNHEX(?) AND date_planification = ?"
            );

            query.setParameter(1, employeId.toString().replace("-", ""));
            query.setParameter(2, date);

            Object result = query.getSingleResult();
            return result != null ? ((Number) result).intValue() : 0;

        } catch (Exception e) {
            System.err.println("❌ Erreur calcul charge employé: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Obtenir toutes les planifications d'un employé pour une date
     */
    public List<Map<String, Object>> getPlanificationsEmployeParJour(Ulid employeId, LocalDate date) {
        try {
            Query query = entityManager.createNativeQuery(
                    "SELECT HEX(id), heure_debut, duree_minutes, terminee " +
                            "FROM planification " +
                            "WHERE employe_id = UNHEX(?) AND date_planification = ? " +
                            "ORDER BY heure_debut"
            );

            query.setParameter(1, employeId.toString().replace("-", ""));
            query.setParameter(2, date);

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();
            List<Map<String, Object>> planifications = new ArrayList<>();

            for (Object[] row : results) {
                Map<String, Object> planif = new HashMap<>();
                planif.put("id", row[0]);
                planif.put("heureDebut", row[1]);
                planif.put("dureeMinutes", row[2]);
                planif.put("terminee", row[3]);
                planifications.add(planif);
            }

            return planifications;

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération planifications employé: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Vérifier si un employé est disponible à une heure donnée
     */
    public boolean isEmployeDisponible(Ulid employeId, LocalDate date, LocalTime heureDebut, int dureeMinutes) {
        try {
            LocalTime heureFin = heureDebut.plusMinutes(dureeMinutes);

            Query query = entityManager.createNativeQuery(
                    "SELECT COUNT(*) FROM planification " +
                            "WHERE employe_id = UNHEX(?) " +
                            "AND date_planification = ? " +
                            "AND NOT (heure_debut >= ? OR TIME(heure_debut + INTERVAL duree_minutes MINUTE) <= ?)"
            );

            query.setParameter(1, employeId.toString().replace("-", ""));
            query.setParameter(2, date);
            query.setParameter(3, heureFin);
            query.setParameter(4, heureDebut);

            Number count = (Number) query.getSingleResult();
            return count.intValue() == 0;

        } catch (Exception e) {
            System.err.println("❌ Erreur vérification disponibilité: " + e.getMessage());
            return false;
        }
    }

    /**
     * Supprimer toutes les planifications (pour les tests)
     */
    public int supprimerToutesPlanifications() {
        try {
            return entityManager.createNativeQuery("DELETE FROM planification").executeUpdate();
        } catch (Exception e) {
            System.err.println("❌ Erreur suppression planifications: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Obtenir le repository (si d'autres services en ont besoin)
     */
    public PlanificationRepository getRepository() {
        return planificationRepository;
    }

    // ========== MÉTHODES POUR STATISTIQUES ==========

    /**
     * Compter les planifications par statut
     */
    public long countPlanificationsTerminees() {
        try {
            Query query = entityManager.createNativeQuery(
                    "SELECT COUNT(*) FROM planification WHERE terminee = true"
            );
            return ((Number) query.getSingleResult()).longValue();
        } catch (Exception e) {
            return 0;
        }
    }

    public long countPlanificationsEnCours() {
        try {
            Query query = entityManager.createNativeQuery(
                    "SELECT COUNT(*) FROM planification WHERE date_planification = CURDATE() AND terminee = false"
            );
            return ((Number) query.getSingleResult()).longValue();
        } catch (Exception e) {
            return 0;
        }
    }

    public long countPlanificationsAVenir() {
        try {
            Query query = entityManager.createNativeQuery(
                    "SELECT COUNT(*) FROM planification WHERE date_planification > CURDATE()"
            );
            return ((Number) query.getSingleResult()).longValue();
        } catch (Exception e) {
            return 0;
        }
    }
}
