package com.pcagrade.order.repository;

import com.pcagrade.order.entity.Employe;
import com.pcagrade.order.entity.Planification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PlanificationRepository extends JpaRepository<Planification, Long> {

    List<Planification> findByEmployeAndDatePlanifiee(Employe employe, LocalDate date);

    List<Planification> findByDatePlanifiee(LocalDate date);

    @Query("SELECT p FROM Planification p WHERE p.employe.id = :employeId AND p.datePlanifiee BETWEEN :debut AND :fin")
    List<Planification> findByEmployeAndPeriode(@Param("employeId") Long employeId,
                                                @Param("debut") LocalDate debut,
                                                @Param("fin") LocalDate fin);

    @Query("SELECT SUM(p.dureeMinutes) FROM Planification p WHERE p.employe.id = :employeId AND p.datePlanifiee = :date")
    Integer getTotalMinutesParEmployeEtDate(@Param("employeId") Long employeId, @Param("date") LocalDate date);

    @Query("SELECT p FROM Planification p WHERE p.datePlanifiee BETWEEN :debut AND :fin ORDER BY p.datePlanifiee, p.employe.nom")
    List<Planification> findPlanificationsByPeriode(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);
}
