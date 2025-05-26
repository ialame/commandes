package com.pcagrade.order.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "commandes")
public class Commande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_commande", unique = true, nullable = false)
    private String numeroCommande;

    @NotNull
    @Positive
    @Column(name = "nombre_cartes", nullable = false)
    private Integer nombreCartes;

    @NotNull
    @Column(name = "prix_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "priorite", nullable = false)
    private PrioriteCommande priorite;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutCommande statut = StatutCommande.EN_ATTENTE;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "date_limite", nullable = false)
    private LocalDateTime dateLimite;

    @Column(name = "date_debut_traitement")
    private LocalDateTime dateDebutTraitement;

    @Column(name = "date_fin_traitement")
    private LocalDateTime dateFinTraitement;

    @Column(name = "temps_estime_minutes")
    private Integer tempsEstimeMinutes;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL)
    @JsonIgnore // AJOUTEZ CETTE ANNOTATION
    private List<Planification> planifications;

    // Constructeurs
    public Commande() {}

    public Commande(String numeroCommande, Integer nombreCartes, BigDecimal prixTotal) {
        this.numeroCommande = numeroCommande;
        this.nombreCartes = nombreCartes;
        this.prixTotal = prixTotal;
        this.priorite = calculerPriorite(prixTotal);
        this.dateLimite = calculerDateLimite();
        this.tempsEstimeMinutes = nombreCartes * 5; // 5 min par carte
    }

    // Méthodes utilitaires
    private PrioriteCommande calculerPriorite(BigDecimal prix) {
        if (prix.compareTo(new BigDecimal("1000")) >= 0) {
            return PrioriteCommande.HAUTE; // 1 semaine
        } else if (prix.compareTo(new BigDecimal("500")) >= 0) {
            return PrioriteCommande.MOYENNE; // 2 semaines
        } else {
            return PrioriteCommande.BASSE; // 4 semaines
        }
    }

    private LocalDateTime calculerDateLimite() {
        LocalDateTime limite = this.dateCreation;
        switch (this.priorite) {
            case HAUTE:
                limite = limite.plusWeeks(1);
                break;
            case MOYENNE:
                limite = limite.plusWeeks(2);
                break;
            case BASSE:
                limite = limite.plusWeeks(4);
                break;
        }
        return limite;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroCommande() { return numeroCommande; }
    public void setNumeroCommande(String numeroCommande) { this.numeroCommande = numeroCommande; }

    public Integer getNombreCartes() { return nombreCartes; }
    public void setNombreCartes(Integer nombreCartes) {
        this.nombreCartes = nombreCartes;
        this.tempsEstimeMinutes = nombreCartes * 5;
    }

    public BigDecimal getPrixTotal() { return prixTotal; }
    public void setPrixTotal(BigDecimal prixTotal) {
        this.prixTotal = prixTotal;
        this.priorite = calculerPriorite(prixTotal);
        this.dateLimite = calculerDateLimite();
    }

    public PrioriteCommande getPriorite() { return priorite; }
    public void setPriorite(PrioriteCommande priorite) { this.priorite = priorite; }

    public StatutCommande getStatut() { return statut; }
    public void setStatut(StatutCommande statut) { this.statut = statut; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateLimite() { return dateLimite; }
    public void setDateLimite(LocalDateTime dateLimite) { this.dateLimite = dateLimite; }

    public LocalDateTime getDateDebutTraitement() { return dateDebutTraitement; }
    public void setDateDebutTraitement(LocalDateTime dateDebutTraitement) {
        this.dateDebutTraitement = dateDebutTraitement;
    }

    public LocalDateTime getDateFinTraitement() { return dateFinTraitement; }
    public void setDateFinTraitement(LocalDateTime dateFinTraitement) {
        this.dateFinTraitement = dateFinTraitement;
    }

    public Integer getTempsEstimeMinutes() { return tempsEstimeMinutes; }
    public void setTempsEstimeMinutes(Integer tempsEstimeMinutes) {
        this.tempsEstimeMinutes = tempsEstimeMinutes;
    }

    public List<Planification> getPlanifications() { return planifications; }
    public void setPlanifications(List<Planification> planifications) {
        this.planifications = planifications;
    }
}





