package com.pcagrade.order.repository;

import com.pcagrade.order.entity.Order;
import com.pcagrade.order.ulid.Ulid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Ulid> {

    // Méthodes de base héritées de JpaRepository :
    // - findAll()
    // - findById(Ulid id)
    // - save(Order order)
    // - delete(Order order)

    // Méthodes personnalisées

    @Query("SELECT o FROM Order o WHERE o.annulee = false AND o.paused = false")
    List<Order> findActiveOrders();

    @Query("SELECT o FROM Order o WHERE o.status = :status")
    List<Order> findByStatus(@Param("status") Integer status);

    @Query("SELECT o FROM Order o WHERE o.retard = true")
    List<Order> findDelayedOrders();

    @Query("SELECT o FROM Order o WHERE o.annulee = false AND o.paused = false AND o.status IN (1, 2)")
    List<Order> findOrdersToProcess();

    // === MÉTHODES POUR DASHBOARDSERVICE ===

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatut(@Param("status") Integer status);

    // Adapter la logique selon vos besoins - exemple basé sur retard et délai
    @Query("SELECT o FROM Order o WHERE o.retard = true OR o.date < :dateLimit")
    List<Order> findCommandesEnRetard(@Param("dateLimit") java.time.Instant dateLimit);

    @Query("SELECT o FROM Order o WHERE o.date BETWEEN :debut AND :fin")
    List<Order> findCommandesByPeriode(@Param("debut") java.time.Instant debut, @Param("fin") java.time.Instant fin);

    List<Order> findByDateGreaterThanEqualOrderByDateDesc(@NotNull Instant date);

}