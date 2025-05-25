package com.pcagrade.order.repository;

import com.pcagrade.order.entity.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmployeRepository extends JpaRepository<Employe, Long> {

    List<Employe> findByActifTrue();

    @Query("SELECT e FROM Employe e WHERE e.actif = true ORDER BY e.nom, e.prenom")
    List<Employe> findEmployesActifsOrdonnes();

    boolean existsByEmail(String email);
}

