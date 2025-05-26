package com.pcagrade.order.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "planifications")
public class Planification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER) // CHANGEZ DE LAZY à EAGER
    @JoinColumn(name = "commande_id", nullable = false)
    @JsonBackReference("commande-planifications") // AJOUT
    private Commande commande;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER) // CHANGEZ DE LAZY à EAGER
    @JoinColumn(name = "employe_id", nullable = false)
    @JsonBackReference("employe-planifications") // AJOUT
    private Employe employe;

    @NotNull
    @Column(name = "date_planifiee", nullable = false)
    private LocalDate datePlanifiee;

    @NotNull
    @Column(name = "heure_debut", nullable = false)
    private Integer heureDebut;

    @NotNull
    @Column(name = "duree_minutes", nullable = false)
    private Integer dureeMinutes;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "terminee")
    private Boolean terminee = false;

    // Constructeurs
    public Planification() {}

    public Planification(Commande commande, Employe employe, LocalDate datePlanifiee,
                         Integer heureDebut, Integer dureeMinutes) {
        this.commande = commande;
        this.employe = employe;
        this.datePlanifiee = datePlanifiee;
        this.heureDebut = heureDebut;
        this.dureeMinutes = dureeMinutes;
    }

    // Getters et Setters (identiques)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Commande getCommande() { return commande; }
    public void setCommande(Commande commande) { this.commande = commande; }

    public Employe getEmploye() { return employe; }
    public void setEmploye(Employe employe) { this.employe = employe; }

    public LocalDate getDatePlanifiee() { return datePlanifiee; }
    public void setDatePlanifiee(LocalDate datePlanifiee) { this.datePlanifiee = datePlanifiee; }

    public Integer getHeureDebut() { return heureDebut; }
    public void setHeureDebut(Integer heureDebut) { this.heureDebut = heureDebut; }

    public Integer getDureeMinutes() { return dureeMinutes; }
    public void setDureeMinutes(Integer dureeMinutes) { this.dureeMinutes = dureeMinutes; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public Boolean getTerminee() { return terminee; }
    public void setTerminee(Boolean terminee) { this.terminee = terminee; }
}