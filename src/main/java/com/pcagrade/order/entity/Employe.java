package com.pcagrade.order.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "employes")
public class Employe extends AbstractUlidEntity {

    @NotBlank
    @Column(nullable = false)
    private String nom;

    @NotBlank
    @Column(nullable = false)
    private String prenom;

    @Column(unique = true, nullable = false)
    private String email;

    @NotNull
    @Column(name = "heures_travail_par_jour", nullable = false)
    private Integer heuresTravailParJour = 8; // 8h par défaut

    @Column(name = "actif", nullable = false)
    private Boolean actif = true;

    // SUPPRIMÉ: dateCreation est déjà défini dans AbstractUlidEntity
    // @Column(name = "date_creation")
    // private LocalDateTime dateCreation = LocalDateTime.now();

    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Planification> planifications;

    // Constructeurs
    public Employe() {
        super(); // Génère automatiquement un ULID et définit dateCreation
    }

    public Employe(String nom, String prenom, String email) {
        super(); // Génère automatiquement un ULID et définit dateCreation
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
    }

    // Constructeur avec ULID existant (pour migration)
    public Employe(String idString, String nom, String prenom, String email) {
        super(idString);
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
    }

    // Getters et Setters
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getHeuresTravailParJour() { return heuresTravailParJour; }
    public void setHeuresTravailParJour(Integer heuresTravailParJour) {
        this.heuresTravailParJour = heuresTravailParJour;
    }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    // dateCreation est hérité de AbstractUlidEntity
    // public LocalDateTime getDateCreation() { return dateCreation; }
    // public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public List<Planification> getPlanifications() { return planifications; }
    public void setPlanifications(List<Planification> planifications) {
        this.planifications = planifications;
    }

    // Méthodes utilitaires
    public boolean isActif() {
        return Boolean.TRUE.equals(actif);
    }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    @Override
    public String toString() {
        return "Employe{" +
                "id=" + getIdAsString() +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", actif=" + actif +
                ", heuresTravailParJour=" + heuresTravailParJour +
                '}';
    }
}