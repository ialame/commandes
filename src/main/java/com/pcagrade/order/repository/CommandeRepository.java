package com.pcagrade.order.repository;// CommandeRepository.java

import com.pcagrade.order.entity.Commande;
import com.pcagrade.order.entity.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommandeRepository extends JpaRepository<Commande, Long> {

    List<Commande> findByStatut(StatutCommande statut);

    @Query("SELECT c FROM Commande c WHERE c.statut IN :statuts ORDER BY c.priorite DESC, c.dateCreation ASC")
    List<Commande> findByStatutInOrderByPrioriteAndDate(@Param("statuts") List<StatutCommande> statuts);

    @Query("SELECT c FROM Commande c WHERE c.dateLimite < :date AND c.statut != 'TERMINEE'")
    List<Commande> findCommandesEnRetard(@Param("date") LocalDateTime date);

    @Query("SELECT COUNT(c) FROM Commande c WHERE c.statut = :statut")
    Long countByStatut(@Param("statut") StatutCommande statut);

    @Query("SELECT c FROM Commande c WHERE c.dateCreation BETWEEN :debut AND :fin")
    List<Commande> findCommandesByPeriode(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);
}