package com.pcagrade.order.repository;
import com.pcagrade.order.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByIsActiveTrue();

    @Query("SELECT e FROM Employee e WHERE e.isActive = true ORDER BY " +
            "(SELECT COUNT(o) FROM Order o WHERE o.assignedEmployee = e AND o.status IN ('SCHEDULED', 'IN_PROGRESS'))")
    List<Employee> findActiveEmployeesOrderByWorkload();

    @Query("SELECT e, COUNT(o) as orderCount FROM Employee e " +
            "LEFT JOIN e.assignedOrders o " +
            "WHERE e.isActive = true AND (o.status IN ('SCHEDULED', 'IN_PROGRESS') OR o IS NULL) " +
            "GROUP BY e.id ORDER BY orderCount ASC")
    List<Object[]> findEmployeesWithWorkloadCount();
}
