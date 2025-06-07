package com.pcagrade.order.service;

import com.github.f4b6a3.ulid.Ulid;
import com.pcagrade.order.entity.Employe;
import com.pcagrade.order.repository.EmployeRepository;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class EmployeService {

    @Autowired
    private EmployeRepository employeRepository;


    @Autowired
    private EntityManager entityManager;


    // Méthode native temporaire pour contourner le problème
    public List<Map<String, Object>> getTousEmployesNative() {
        try {
            // SEULEMENT les colonnes qui existent dans votre table
            Query query = entityManager.createNativeQuery(
                    "SELECT " +
                            "  HEX(id) as id_hex, " +
                            "  nom, " +
                            "  prenom, " +
                            "  email, " +
                            "  heures_travail_par_jour, " +
                            "  actif " +
                            "FROM commandes_db.employe " +
                            "ORDER BY nom, prenom"
            );

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();
            List<Map<String, Object>> employes = new ArrayList<>();

            for (Object[] row : results) {
                Map<String, Object> employe = new HashMap<>();
                employe.put("id", row[0]);
                employe.put("nom", row[1] != null ? row[1] : "");
                employe.put("prenom", row[2] != null ? row[2] : "");
                employe.put("email", row[3] != null ? row[3] : "");
                employe.put("heuresTravailParJour", row[4] != null ? row[4] : 8);

                // Gestion du champ bit(1) pour actif
                boolean actif = false;
                if (row[5] != null) {
                    if (row[5] instanceof byte[]) {
                        byte[] bits = (byte[]) row[5];
                        actif = bits.length > 0 && bits[0] == 1;
                    } else if (row[5] instanceof Boolean) {
                        actif = (Boolean) row[5];
                    } else if (row[5] instanceof Number) {
                        actif = ((Number) row[5]).intValue() == 1;
                    }
                }
                employe.put("actif", actif);

                employes.add(employe);
            }

            System.out.println("✅ " + employes.size() + " employés chargés");
            return employes;

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement employés: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * ✅ MÉTHODES PRINCIPALES POUR L'ALGORITHME
     */

    /**
     * Récupère tous les employés actifs (pour l'algorithme)
     */
    public List<Employe> getEmployesActifs() {
        return employeRepository.findEmployesActifs();  // ✅ Maintenant disponible
    }

    /**
     * Alias pour getEmployesActifs() - compatibilité
     */
    public List<Employe> findEmployesActifs() {
        return getEmployesActifs();
    }

    /**
     * ✅ MÉTHODES DE BASE POUR LE CONTROLLER
     */

    /**
     * Récupère tous les employés
     */
    public List<Employe> getTousEmployes() {
        return employeRepository.findAll();
    }

    /**
     * Trouve un employé par ID
     */
    public Optional<Employe> trouverParId(Ulid id) {
        return employeRepository.findById(id);
    }

    /**
     * Sauvegarde un employé
     */
    public Employe sauvegarder(Employe employe) {
        return employeRepository.save(employe);
    }

    /**
     * Supprime un employé
     */
    public void supprimer(Ulid id) {
        employeRepository.deleteById(id);
    }

    /**
     * Met à jour un employé
     */
    public Employe mettreAJour(Employe employe) {
        return employeRepository.save(employe);
    }

    /**
     * ✅ MÉTHODES SIMPLIFIÉES POUR ÉVITER LES ERREURS
     */

    /**
     * Trouve un employé par nom (version simple)
     */
    public Employe trouverParNom(String nom) {
        try {
            return employeRepository.findByNom(nom);  // ✅ Maintenant disponible
        } catch (Exception e) {
            System.err.println("❌ Erreur recherche par nom: " + e.getMessage());
            return null;
        }
    }

    /**
     * Trouve un employé par email (version simple)
     */
    public Employe trouverParEmail(String email) {
        try {
            return employeRepository.findByEmail(email);  // ✅ Maintenant disponible
        } catch (Exception e) {
            System.err.println("❌ Erreur recherche par email: " + e.getMessage());
            return null;
        }
    }

    /**
     * Recherche par nom (version simple)
     */
    public List<Employe> rechercherParNom(String nom) {
        try {
            return employeRepository.findByNomContainingIgnoreCase(nom);  // ✅ Maintenant disponible
        } catch (Exception e) {
            System.err.println("❌ Erreur recherche partielle: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Active/désactive un employé
     */
    public Employe changerStatutActif(Ulid id, Boolean actif) {
        Optional<Employe> employeOpt = employeRepository.findById(id);
        if (employeOpt.isPresent()) {
            Employe employe = employeOpt.get();
            employe.setActif(actif);
            return employeRepository.save(employe);
        }
        throw new RuntimeException("Employé non trouvé avec ID: " + id);
    }

    /**
     * Compte les employés actifs (version simple)
     */
    public Long compterEmployesActifs() {
        try {
            return employeRepository.countByActif(true);  // ✅ Maintenant disponible
        } catch (Exception e) {
            System.err.println("❌ Erreur comptage actifs: " + e.getMessage());
            return 0L;
        }
    }

    /**
     * Compte les employés inactifs (version simple)
     */
    public Long compterEmployesInactifs() {
        try {
            return employeRepository.countByActif(false);  // ✅ Maintenant disponible
        } catch (Exception e) {
            System.err.println("❌ Erreur comptage inactifs: " + e.getMessage());
            return 0L;
        }
    }

    /**
     * Vérifie si un employé existe par email (version simple)
     */
    public boolean emailExiste(String email) {
        try {
            return employeRepository.findByEmail(email) != null;  // ✅ Maintenant disponible
        } catch (Exception e) {
            System.err.println("❌ Erreur vérification email: " + e.getMessage());
            return false;
        }
    }

    /**
     * Trouve les employés avec un nombre d'heures minimum (version simple)
     */
    public List<Employe> getEmployesAvecHeuresMin(Integer heuresMin) {
        try {
            return employeRepository.findEmployesActifsAvecHeuresMin(heuresMin);  // ✅ Maintenant disponible
        } catch (Exception e) {
            System.err.println("❌ Erreur recherche heures min: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Crée un nouvel employé avec validation simple
     */
    public Employe creerEmploye(String nom, String prenom, String email, Integer heuresTravail) {
        if (emailExiste(email)) {
            throw new RuntimeException("Un employé avec cet email existe déjà: " + email);
        }

        Employe employe = new Employe();
        employe.setNom(nom);
        employe.setPrenom(prenom);
        employe.setEmail(email);
        employe.setHeuresTravailParJour(heuresTravail != null ? heuresTravail : 8);
        employe.setActif(true);

        return sauvegarder(employe);
    }

    // NOUVELLE MÉTHODE : Employés actifs uniquement
    public List<Map<String, Object>> getTousEmployesActifs() {
        try {
            Query query = entityManager.createNativeQuery(
                    "SELECT " +
                            "  HEX(id) as id_hex, " +
                            "  nom, " +
                            "  prenom, " +
                            "  email, " +
                            "  heures_travail_par_jour, " +
                            "  actif " +
                            "FROM commandes_db.employe " +
                            "WHERE actif = 1 " +  // Pour bit(1)
                            "ORDER BY nom, prenom"
            );

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();
            List<Map<String, Object>> employes = new ArrayList<>();

            for (Object[] row : results) {
                Map<String, Object> employe = new HashMap<>();
                employe.put("id", row[0]);
                employe.put("nom", row[1] != null ? row[1] : "");
                employe.put("prenom", row[2] != null ? row[2] : "");
                employe.put("email", row[3] != null ? row[3] : "");
                employe.put("heuresTravailParJour", row[4] != null ? row[4] : 8);

                // Pour les actifs, on sait qu'ils sont actifs
                employe.put("actif", true);

                employes.add(employe);
            }

            System.out.println("✅ " + employes.size() + " employés actifs chargés");
            return employes;

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement employés actifs: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // Méthode utilitaire : Compter les employés actifs
    public long getNombreEmployesActifs() {
        try {
            Query query = entityManager.createNativeQuery(
                    "SELECT COUNT(*) FROM commandes_db.employe WHERE actif = 1"
            );
            return ((Number) query.getSingleResult()).longValue();
        } catch (Exception e) {
            System.err.println("❌ Erreur comptage employés actifs: " + e.getMessage());
            return 0;
        }
    }

    // Méthode utilitaire : Employés avec disponibilité
    public List<Map<String, Object>> getEmployesDisponibles(LocalDate datePlanification) {
        try {
            Query query = entityManager.createNativeQuery(
                    "SELECT " +
                            "  HEX(e.id) as id_hex, " +
                            "  e.nom, " +
                            "  e.prenom, " +
                            "  e.email, " +
                            "  e.heures_travail_par_jour, " +
                            "  COALESCE(SUM(p.duree_minutes), 0) as minutes_planifiees " +
                            "FROM commandes_db.employe e " +
                            "LEFT JOIN commandes_db.planification p ON e.id = p.employe_id " +
                            "  AND p.date_planification = ? " +
                            "WHERE e.actif = TRUE " +
                            "GROUP BY e.id, e.nom, e.prenom, e.email, e.heures_travail_par_jour " +
                            "HAVING minutes_planifiees < (e.heures_travail_par_jour * 60) " +  // Moins que la capacité journalière
                            "ORDER BY minutes_planifiees ASC, e.nom, e.prenom"
            );

            query.setParameter(1, datePlanification);

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();
            List<Map<String, Object>> employes = new ArrayList<>();

            for (Object[] row : results) {
                Map<String, Object> employe = new HashMap<>();
                employe.put("id", row[0]);
                employe.put("nom", row[1]);
                employe.put("prenom", row[2]);
                employe.put("email", row[3]);
                employe.put("heuresTravailParJour", row[4]);
                employe.put("minutesPlanifiees", row[5]);

                // Calculer la disponibilité restante
                int heuresTravail = ((Number) row[4]).intValue();
                int minutesPlanifiees = ((Number) row[5]).intValue();
                int minutesDisponibles = (heuresTravail * 60) - minutesPlanifiees;

                employe.put("minutesDisponibles", minutesDisponibles);
                employe.put("disponibilitePercent", (minutesDisponibles * 100) / (heuresTravail * 60));

                employes.add(employe);
            }

            System.out.println("✅ " + employes.size() + " employés disponibles pour le " + datePlanification);
            return employes;

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement employés disponibles: " + e.getMessage());
            throw e;
        }
    }
}