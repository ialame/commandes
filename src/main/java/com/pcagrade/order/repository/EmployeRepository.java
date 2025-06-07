package com.pcagrade.order.repository;

import com.pcagrade.order.entity.Employe;
import com.github.f4b6a3.ulid.Ulid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmployeRepository extends JpaRepository<Employe, Ulid> {

    // ========== MÉTHODES POUR L'ALGORITHME DE PLANIFICATION ==========

    /**
     * ✅ CORRIGÉ - Trouve les employés actifs (pour l'algorithme)
     */
    @Query("SELECT e FROM Employe e WHERE e.actif = true")
    List<Employe> findEmployesActifs();

    /**
     * Alternative Spring Data automatique
     */
    List<Employe> findByActif(Boolean actif);

    // ========== MÉTHODES POUR LE SERVICE ==========

    /**
     * ✅ CORRIGÉ - Trouve un employé par nom
     */
    Employe findByNom(String nom);

    /**
     * ✅ CORRIGÉ - Trouve un employé par email
     */
    Employe findByEmail(String email);

    /**
     * ✅ CORRIGÉ - Recherche par nom (partielle, insensible à la casse)
     */
    List<Employe> findByNomContainingIgnoreCase(String nom);

    /**
     * ✅ CORRIGÉ - Compte les employés par statut actif/inactif
     */
    Long countByActif(Boolean actif);

    /**
     * ✅ CORRIGÉ - Trouve les employés actifs avec un nombre d'heures minimum
     */
    @Query("SELECT e FROM Employe e WHERE e.actif = true AND e.heuresTravailParJour >= :heuresMin")
    List<Employe> findEmployesActifsAvecHeuresMin(@Param("heuresMin") Integer heuresMin);

    // ========== MÉTHODES SPRING DATA AUTOMATIQUES ==========

    /**
     * Spring Data génère automatiquement ces méthodes
     */
    List<Employe> findByHeuresTravailParJour(Integer heures);
    List<Employe> findByActifAndHeuresTravailParJourGreaterThanEqual(Boolean actif, Integer heuresMin);
    List<Employe> findByPrenomContainingIgnoreCase(String prenom);
    List<Employe> findByEmailContainingIgnoreCase(String email);

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Trouve les employés par nom OU prénom
     */
    @Query("SELECT e FROM Employe e WHERE LOWER(e.nom) LIKE LOWER(CONCAT('%', :terme, '%')) OR LOWER(e.prenom) LIKE LOWER(CONCAT('%', :terme, '%'))")
    List<Employe> rechercherParNomOuPrenom(@Param("terme") String terme);

    /**
     * Statistiques pour le dashboard
     */
    @Query("SELECT COUNT(e) FROM Employe e WHERE e.actif = true")
    Long countEmployesActifs();

    @Query("SELECT COUNT(e) FROM Employe e WHERE e.actif = false")
    Long countEmployesInactifs();

    /**
     * Employés disponibles (actifs avec heures de travail > 0)
     */
    @Query("SELECT e FROM Employe e WHERE e.actif = true AND e.heuresTravailParJour > 0")
    List<Employe> findEmployesDisponibles();

    /**
     * Moyenne des heures de travail par jour
     */
    @Query("SELECT AVG(e.heuresTravailParJour) FROM Employe e WHERE e.actif = true")
    Double getMoyenneHeuresTravail();

    /**
     * Employés récemment créés
     */
//    @Query("SELECT e FROM Employe e WHERE e.dateCreation >= :depuis ORDER BY e.dateCreation DESC")
//    List<Employe> findEmployesRecents(@Param("depuis") java.time.LocalDateTime depuis);

    // ========== MÉTHODES DE VALIDATION ==========

    /**
     * Vérifier l'unicité de l'email
     */
    boolean existsByEmail(String email);

    /**
     * Vérifier l'existence par nom et prénom
     */
    boolean existsByNomAndPrenom(String nom, String prenom);

    /**
     * Trouver par email (version alternative)
     */
    @Query("SELECT e FROM Employe e WHERE LOWER(e.email) = LOWER(:email)")
    Employe findByEmailIgnoreCase(@Param("email") String email);
}