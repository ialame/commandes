package com.pcagrade.order.repository;


import com.pcagrade.order.entity.Employee;
import com.pcagrade.order.entity.Order;
import com.pcagrade.order.entity.OrderStatus;
import com.pcagrade.order.entity.PriorityLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Commandes non traitées (en attente ou planifiées)
    List<Order> findByStatusIn(List<OrderStatus> statuses);

    // Commandes par statut
    List<Order> findByStatus(OrderStatus status);

    // Commandes par employé et statut
    List<Order> findByAssignedEmployeeAndStatusIn(Employee employee, List<OrderStatus> statuses);

    // Commandes par date de création
    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Commandes par date d'échéance
    List<Order> findByDueDateBefore(LocalDateTime date);

    // Commandes par priorité
    List<Order> findByPriorityLevelOrderByCreatedAtAsc(PriorityLevel priority);

    // Commandes en retard
    @Query("SELECT o FROM Order o WHERE o.dueDate < :now AND o.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Order> findOverdueOrders(@Param("now") LocalDateTime now);

    // Charge de travail par employé pour une période donnée
    @Query("SELECT o.assignedEmployee, SUM(o.estimatedDurationMinutes) FROM Order o " +
            "WHERE o.scheduledDate BETWEEN :start AND :end " +
            "AND o.status IN ('SCHEDULED', 'IN_PROGRESS') " +
            "GROUP BY o.assignedEmployee")
    List<Object[]> findWorkloadByEmployeeForPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Commandes non assignées
    @Query("SELECT o FROM Order o WHERE o.assignedEmployee IS NULL AND o.status = 'PENDING' ORDER BY o.priorityLevel ASC, o.createdAt ASC")
    List<Order> findUnassignedOrdersByPriority();

    // Statistiques par jour
    @Query("SELECT DATE(o.createdAt), COUNT(o) FROM Order o " +
            "WHERE o.createdAt BETWEEN :start AND :end " +
            "GROUP BY DATE(o.createdAt) ORDER BY DATE(o.createdAt)")
    List<Object[]> findDailyOrderStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Temps moyen de traitement
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, o.scheduledDate, o.completedAt)) FROM Order o " +
            "WHERE o.status = 'COMPLETED' AND o.scheduledDate IS NOT NULL AND o.completedAt IS NOT NULL")
    Double findAverageProcessingTimeInHours();
}