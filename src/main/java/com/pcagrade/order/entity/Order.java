package com.pcagrade.order.entity;

import com.pcagrade.order.ulid.Ulid;
import com.pcagrade.order.ulid.UlidType;
import com.pcagrade.order.entity.Planification;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "`order`")
public class Order extends AbstractUlidEntity{

    @Column(name = "num")
    private Integer num;

    @NotNull
    @Column(name = "date", nullable = false)
    private Instant date;

    @Size(max = 255)
    @NotNull
    @Column(name = "num_commande", nullable = false)
    private String numCommande;

    @Size(max = 3)
    @Column(name = "langue", length = 3)
    private String langue;

    @Size(max = 255)
    @Column(name = "reference")
    private String reference;

    @Column(name = "note_minimale")
    private Double noteMinimale;

    @Column(name = "note_minimale_csn")
    private Boolean noteMinimaleCsn;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "mere", nullable = false)
    private Boolean mere = false;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "annulee", nullable = false)
    private Boolean annulee = false;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "retard", nullable = false)
    private Boolean retard = false;

    @Column(name = "nb_descellements")
    private Integer nbDescellements;

    @Size(max = 255)
    @NotNull
    @Column(name = "num_commande_client", nullable = false)
    private String numCommandeClient;

    @NotNull
    @Column(name = "status", nullable = false)
    private Integer status;

    @Size(max = 255)
    @NotNull
    @Column(name = "delai", nullable = false)
    private String delai;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "notation_unique", nullable = false)
    private Boolean notationUnique = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_mere_id")
    private Order commandeMere;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "paused", nullable = false)
    private Boolean paused = false;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "special_grades", nullable = false)
    private Boolean specialGrades = false;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "type", nullable = false)
    private Integer type;

    @ColumnDefault("1")
    @Column(name = "priority_other_grades_ewg")
    private Boolean priorityOtherGradesEwg;

    // Relation avec les planifications
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Planification> planifications;

    // === MÉTHODES UTILITAIRES POUR LA PLANIFICATION ===

    /**
     * Calcule le temps estimé en minutes pour cette commande
     */
    public Integer getTempsEstimeMinutes() {
        int baseTime = 60; // 1 heure de base

        // Ajuster selon le nombre de descellements
        if (nbDescellements != null) {
            baseTime += nbDescellements * 30; // 30 min par descellement
        }

        // Ajuster selon le type
        if (type != null && type > 1) {
            baseTime += 60; // 1 heure supplémentaire pour types spéciaux
        }

        // Ajuster pour grades spéciaux
        if (Boolean.TRUE.equals(specialGrades)) {
            baseTime += 30;
        }

        return baseTime;
    }

    /**
     * Détermine la priorité basée sur les attributs de l'order
     */
    public String getPrioriteString() {
        if (Boolean.TRUE.equals(retard)) {
            return "HAUTE";
        }

        if (type != null && type > 0) {
            return "HAUTE";
        }

        if (Boolean.TRUE.equals(specialGrades)) {
            return "MOYENNE";
        }

        return "BASSE";
    }

    /**
     * Parse le délai pour déterminer la date limite
     */
    public LocalDateTime getDateLimite() {
        if (delai == null || date == null) {
            // Par défaut, 7 jours après la date de commande
            return getDateCreationAsLocalDateTime().plusDays(7);
        }

        try {
            // Si le délai est en jours (ex: "5", "10")
            int jours = Integer.parseInt(delai);
            return getDateCreationAsLocalDateTime().plusDays(jours);
        } catch (NumberFormatException e) {
            // Si le délai est un format de date ou autre, retourner 7 jours par défaut
            return getDateCreationAsLocalDateTime().plusDays(7);
        }
    }

    /**
     * Convertit la date Instant en LocalDateTime
     */
    public LocalDateTime getDateCreationAsLocalDateTime() {
        if (date == null) return LocalDateTime.now();
        return LocalDateTime.ofInstant(date, ZoneId.systemDefault());
    }

    /**
     * Vérifie si l'order doit être traité (non annulé, non pausé)
     */
    public boolean estATraiter() {
        return !Boolean.TRUE.equals(annulee) && !Boolean.TRUE.equals(paused);
    }

    /**
     * Retourne le nombre de cartes (alias pour nbDescellements)
     */
    public Integer getNombreCartes() {
        return nbDescellements != null ? nbDescellements : 1;
    }

    // Méthodes utilitaires pour la compatibilité
    public String getIdAsString() {
        return getId() != null ? getId().toString() : null;  // ✅ Utiliser getId() au lieu de id direct
    }

    public void setIdFromString(String idString) {
        this.setId(idString != null ? Ulid.fromString(idString) : null);  // ✅ Utiliser setId() au lieu de id direct
    }

    // Getter/Setter pour planifications
    public List<Planification> getPlanifications() {
        return planifications;
    }

    public void setPlanifications(List<Planification> planifications) {
        this.planifications = planifications;
    }

    // === MÉTHODES TEMPORAIRES POUR COMPATIBILITÉ ===

    public String getNumeroCommande() {
        return numCommande;
    }

    public void setNumeroCommande(String numeroCommande) {
        this.numCommande = numeroCommande;
    }

    public String getPriorite() {
        return getPrioriteString();
    }

    public String getStatut() {
        return status != null ? status.toString() : "0";
    }

    // Méthodes fictives pour champs qui n'existent pas dans Order
    public java.math.BigDecimal getPrixTotal() {
        return java.math.BigDecimal.ZERO; // Valeur par défaut
    }

    public void setPrixTotal(java.math.BigDecimal prix) {
        // Ne fait rien car ce champ n'existe pas dans Order
    }

    public void setNombreCartes(Integer nombre) {
        this.nbDescellements = nombre;
    }

    public java.time.LocalDateTime getDateDebutTraitement() {
        return null; // Champ non disponible dans Order
    }

    public java.time.LocalDateTime getDateFinTraitement() {
        return null; // Champ non disponible dans Order
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + getIdAsString() +
                ", numCommande='" + numCommande + '\'' +
                ", status=" + status +
                ", retard=" + retard +
                ", priorite='" + getPrioriteString() + '\'' +
                '}';
    }
}