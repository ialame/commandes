package com.pcagrade.order.entity;


import com.github.f4b6a3.hibernate.AbstractUlidEntity;
import com.github.f4b6a3.ulid.Ulid;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "planification")
@Data
@EqualsAndHashCode(callSuper = true)  // Inclut les champs de AbstractUlidEntity
@ToString(callSuper = true)           // Inclut toString() de AbstractUlidEntity
@NoArgsConstructor                    // Constructeur par défaut requis par JPA
public class Planification extends AbstractUlidEntity {

    @Column(name = "order_id", columnDefinition = "BINARY(16)", nullable = false)
    private Ulid orderId;

    @Column(name = "employe_id", columnDefinition = "BINARY(16)", nullable = false)
    private Ulid employeId;

    @Column(name = "date_planification", nullable = false)
    private LocalDate datePlanification;

    @Column(name = "heure_debut", nullable = false)
    private LocalTime heureDebut;

    @Column(name = "duree_minutes", nullable = false)
    private Integer dureeMinutes;

    @Column(name = "terminee", nullable = false)
    private Boolean terminee = false;

    // ========== RELATIONS JPA ==========
    // @ToString.Exclude pour éviter les boucles infinies dans toString()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    @ToString.Exclude
    private Commande order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", insertable = false, updatable = false)
    @ToString.Exclude
    private Employe employe;

    // ========== CONSTRUCTEUR POUR L'ALGORITHME ==========

    /**
     * Constructeur pour votre algorithme de planification
     */
    public Planification(Ulid orderId, Ulid employeId, LocalDate datePlanification,
                         LocalTime heureDebut, Integer dureeMinutes) {
        super();
        this.orderId = orderId;
        this.employeId = employeId;
        this.datePlanification = datePlanification;
        this.heureDebut = heureDebut;
        this.dureeMinutes = dureeMinutes;
        this.terminee = false;
    }

    // ========== MÉTHODES ALIAS POUR COMPATIBILITÉ ==========

    /**
     * Alias pour getDatePlanification() - compatibilité avec votre code existant
     */
    public LocalDate getDatePlanifiee() {
        return datePlanification;
    }

    public void setDatePlanifiee(LocalDate datePlanifiee) {
        this.datePlanification = datePlanifiee;
    }

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Vérifie si la planification est en cours (aujourd'hui et pas encore terminée)
     */
    public boolean isEnCours() {
        return LocalDate.now().equals(datePlanification) && !terminee;
    }

    /**
     * Vérifie si la planification est en retard (date passée et pas terminée)
     */
    public boolean isEnRetard() {
        return LocalDate.now().isAfter(datePlanification) && !terminee;
    }

    /**
     * Calcule l'heure de fin théorique
     */
    public LocalTime getHeureFin() {
        if (heureDebut != null && dureeMinutes != null) {
            return heureDebut.plusMinutes(dureeMinutes);
        }
        return null;
    }

    /**
     * Setter pour maintenir la cohérence avec la relation
     */
    public void setOrder(Commande order) {
        this.order = order;
        if (order != null) {
            this.orderId = order.getId();
        }
    }

    /**
     * Setter pour maintenir la cohérence avec la relation
     */
    public void setEmploye(Employe employe) {
        this.employe = employe;
        if (employe != null) {
            this.employeId = employe.getId();
        }
    }

    // ========== BUILDER PATTERN (OPTIONNEL) ==========

    public static PlanificationBuilder builder() {
        return new PlanificationBuilder();
    }

    public static class PlanificationBuilder {
        private Ulid orderId;
        private Ulid employeId;
        private LocalDate datePlanification;
        private LocalTime heureDebut;
        private Integer dureeMinutes;

        public PlanificationBuilder orderId(Ulid orderId) {
            this.orderId = orderId;
            return this;
        }

        public PlanificationBuilder employeId(Ulid employeId) {
            this.employeId = employeId;
            return this;
        }

        public PlanificationBuilder datePlanification(LocalDate datePlanification) {
            this.datePlanification = datePlanification;
            return this;
        }

        public PlanificationBuilder heureDebut(LocalTime heureDebut) {
            this.heureDebut = heureDebut;
            return this;
        }

        public PlanificationBuilder dureeMinutes(Integer dureeMinutes) {
            this.dureeMinutes = dureeMinutes;
            return this;
        }

        public Planification build() {
            return new Planification(orderId, employeId, datePlanification, heureDebut, dureeMinutes);
        }
    }
}