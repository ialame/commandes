package com.pcagrade.order.repository;

import com.github.f4b6a3.ulid.Ulid;
import com.pcagrade.order.entity.Planification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface PlanificationRepository extends JpaRepository<Planification, Ulid> {

    // ========== MÉTHODES POUR L'ALGORITHME DE PLANIFICATION ==========

    /**
     * ✅ CORRIGÉ - Utilise datePlanification au lieu de datePlanifiee
     */
    @Query("SELECT COALESCE(SUM(p.dureeMinutes), 0) FROM Planification p WHERE p.employeId = :employeId AND p.datePlanification = :date")
    Integer getTotalMinutesParEmployeEtDate(@Param("employeId") Ulid employeId, @Param("date") LocalDate date);

    /**
     * Alias pour l'algorithme (méthode utilisée dans PlanificationService)
     */
    default Integer getChargeEmployeParJour(Ulid employeId, LocalDate date) {
        return getTotalMinutesParEmployeEtDate(employeId, date);
    }

    // ========== MÉTHODES POUR LES CONTROLLERS ==========

    /**
     * Trouve les planifications par date
     */
    List<Planification> findByDatePlanification(LocalDate date);

    /**
     * Compte les planifications par date
     */
    Long countByDatePlanification(LocalDate date);

    /**
     * Trouve les planifications par période
     */
    List<Planification> findByDatePlanificationBetween(LocalDate debut, LocalDate fin);

    /**
     * Trouve les planifications par employé et période
     */
    List<Planification> findByEmployeIdAndDatePlanificationBetween(
            Ulid employeId, LocalDate debut, LocalDate fin);

    /**
     * Trouve les planifications par commande
     */
    List<Planification> findByOrderId(Ulid orderId);

    /**
     * Trouve les planifications par employé
     */
    List<Planification> findByEmployeId(Ulid employeId);

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Trouve les planifications non terminées
     */
    @Query("SELECT p FROM Planification p WHERE p.terminee = false")
    List<Planification> findPlanificationsNonTerminees();

    /**
     * Trouve les planifications terminées
     */
    @Query("SELECT p FROM Planification p WHERE p.terminee = true")
    List<Planification> findPlanificationsTerminees();

    /**
     * Trouve les planifications du jour pour un employé
     */
    @Query("SELECT p FROM Planification p WHERE p.employeId = :employeId AND p.datePlanification = :date ORDER BY p.heureDebut")
    List<Planification> findPlanificationsDuJourPourEmploye(@Param("employeId") Ulid employeId, @Param("date") LocalDate date);

    /**
     * Trouve les planifications en cours (aujourd'hui et pas terminées)
     */
    @Query("SELECT p FROM Planification p WHERE p.datePlanification = CURRENT_DATE AND p.terminee = false ORDER BY p.heureDebut")
    List<Planification> findPlanificationsEnCours();

    /**
     * Trouve les planifications en retard (date passée et pas terminées)
     */
    @Query("SELECT p FROM Planification p WHERE p.datePlanification < CURRENT_DATE AND p.terminee = false")
    List<Planification> findPlanificationsEnRetard();

    /**
     * Supprime les planifications d'une commande
     */
    void deleteByOrderId(Ulid orderId);

    /**
     * Compte les planifications d'un employé sur une période
     */
    @Query("SELECT COUNT(p) FROM Planification p WHERE p.employeId = :employeId AND p.datePlanification BETWEEN :debut AND :fin")
    Long countPlanificationsByEmployeAndPeriode(@Param("employeId") Ulid employeId, @Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    // ========== MÉTHODES SPRING DATA AUTOMATIQUES ==========

    /**
     * Spring Data génère automatiquement ces méthodes
     */
    List<Planification> findByTerminee(Boolean terminee);
    List<Planification> findByDatePlanificationAndTerminee(LocalDate date, Boolean terminee);
    List<Planification> findByEmployeIdAndTerminee(Ulid employeId, Boolean terminee);

    // ========== MÉTHODES POUR LE DASHBOARD ==========

    /**
     * Statistiques pour le dashboard
     */
    @Query("SELECT COUNT(p) FROM Planification p WHERE p.datePlanification = CURRENT_DATE")
    Long countPlanificationsAujourdhui();

    @Query("SELECT COUNT(p) FROM Planification p WHERE p.datePlanification BETWEEN :debut AND :fin")
    Long countPlanificationsByPeriode(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    /**
     * Charge totale par employé sur une période
     */
    @Query("SELECT COALESCE(SUM(p.dureeMinutes), 0) FROM Planification p WHERE p.employeId = :employeId AND p.datePlanification BETWEEN :debut AND :fin")
    Integer getTotalMinutesParEmployeEtPeriode(@Param("employeId") Ulid employeId, @Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    boolean existsByOrderIdAndEmployeIdAndDatePlanificationAndHeureDebut(
            Ulid orderId, Ulid employeId, LocalDate datePlanification, LocalTime heureDebut);
}