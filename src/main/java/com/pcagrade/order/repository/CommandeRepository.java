package com.pcagrade.order.repository;

import com.github.f4b6a3.ulid.Ulid;
import com.pcagrade.order.entity.Commande;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommandeRepository extends JpaRepository<Commande, Ulid> {

    Optional<Commande> findByNumeroCommande(String numeroCommande);

    List<Commande> findByStatus(Integer status);

    List<Commande> findByPriorite(String priorite);

    @Query("SELECT c FROM Commande c WHERE c.dateLimite BETWEEN ?1 AND ?2 ORDER BY c.dateLimite")
    List<Commande> findByDateLimiteBetween(LocalDateTime debut, LocalDateTime fin);

    @Query("SELECT c FROM Commande c WHERE c.employeId IS NULL AND c.status = ?1")
    List<Commande> findCommandesNonAssignees(Integer status);

    long countByStatus(Integer status);

    @Query("SELECT c FROM Commande c WHERE c.dateLimite BETWEEN ?1 AND ?2")
    List<Commande> findCommandesByPeriode(LocalDateTime debut, LocalDateTime fin);

    @Query("SELECT c FROM Commande c WHERE c.dateDebutTraitement >= ?1")
    List<Commande> findCommandesRecentes(LocalDateTime depuis);

    @Query("SELECT c FROM Commande c WHERE c.dateLimite < CURRENT_TIMESTAMP AND c.status != 3")
    List<Commande> findDelayedOrders();

    List<Commande> findByDateGreaterThanEqualOrderByDate(LocalDateTime date);

}