package com.pcagrade.order.repository;

import com.pcagrade.order.entity.Planification;
import com.pcagrade.order.ulid.Ulid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PlanificationRepository extends JpaRepository<Planification, Ulid> {

    // ✅ Version sans JOIN FETCH - fonctionne même si les tables n'existent pas
    @Query("SELECT p FROM Planification p WHERE p.datePlanifiee BETWEEN :debut AND :fin ORDER BY p.datePlanifiee")
    List<Planification> findPlanificationsByPeriode(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    @Query("SELECT p FROM Planification p WHERE p.datePlanifiee = :date ORDER BY p.heureDebut")
    List<Planification> findByDatePlanifiee(@Param("date") LocalDate date);

    @Query("SELECT p FROM Planification p WHERE p.employeId = :employeId AND p.datePlanifiee BETWEEN :debut AND :fin ORDER BY p.datePlanifiee, p.heureDebut")
    List<Planification> findByEmployeIdAndDatePlanifieeBetween(@Param("employeId") Ulid employeId, @Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    @Query("SELECT p FROM Planification p WHERE p.employeId = :employeId AND p.datePlanifiee BETWEEN :debut AND :fin ORDER BY p.datePlanifiee, p.heureDebut")
    List<Planification> findByEmployeAndPeriode(@Param("employeId") Ulid employeId, @Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    @Query("SELECT SUM(p.dureeMinutes) FROM Planification p WHERE p.employeId = :employeId AND p.datePlanifiee = :date")
    Integer sumDureeMinutesByEmployeIdAndDatePlanifiee(@Param("employeId") Ulid employeId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(p.dureeMinutes), 0) FROM Planification p WHERE p.employeId = :employeId AND p.datePlanifiee = :date")
    Integer getTotalMinutesParEmployeEtDate(@Param("employeId") Ulid employeId, @Param("date") LocalDate date);

    @Query("SELECT p FROM Planification p WHERE p.terminee = false ORDER BY p.datePlanifiee, p.heureDebut")
    List<Planification> findPlanificationsNonTerminees();
}