package com.pcagrade.order.entity;

import com.pcagrade.order.ulid.Ulid;
import com.pcagrade.order.ulid.UlidType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "planifications")
public class Planification extends AbstractUlidEntity {

    // Stocker les ULID directement
    @Column(name = "order_id", nullable = false)
    @Type(UlidType.class)
    private Ulid orderId;

    @Column(name = "employe_id", nullable = false)
    @Type(UlidType.class)
    private Ulid employeId;

    // Relations optionnelles
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", insertable = false, updatable = false)
    private Employe employe;

    // Champs de planification
    @Column(name = "date_planifiee", nullable = false)
    private LocalDate datePlanifiee;

    @Column(name = "heure_debut", nullable = false)
    private LocalTime heureDebut;

    @Column(name = "duree_minutes", nullable = false)
    private Integer dureeMinutes;

    @Column(name = "terminee", nullable = false)
    private Boolean terminee = false;

    // Constructeurs
    public Planification() {
        super();
        this.terminee = false;
    }

    public Planification(Order order, Employe employe, LocalDate datePlanifiee,
                         LocalTime heureDebut, Integer dureeMinutes) {
        super();
        this.order = order;
        this.employe = employe;
        this.orderId = order != null ? order.getId() : null;
        this.employeId = employe != null ? employe.getId() : null;
        this.datePlanifiee = datePlanifiee;
        this.heureDebut = heureDebut;
        this.dureeMinutes = dureeMinutes;
        this.terminee = false;
    }

    // Constructeur avec ULID directs (pour AlgorithmePlanificationService)
    public Planification(Ulid orderId, Ulid employeId, LocalDate datePlanifiee,
                         LocalTime heureDebut, Integer dureeMinutes) {
        super();
        this.orderId = orderId;
        this.employeId = employeId;
        this.datePlanifiee = datePlanifiee;
        this.heureDebut = heureDebut;
        this.dureeMinutes = dureeMinutes;
        this.terminee = false;
    }

    // === GETTERS ET SETTERS ===

    public Ulid getOrderId() {
        return orderId;
    }

    public void setOrderId(Ulid orderId) {
        this.orderId = orderId;
    }

    public Ulid getEmployeId() {
        return employeId;
    }

    public void setEmployeId(Ulid employeId) {
        this.employeId = employeId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
        this.orderId = order != null ? order.getId() : null;
    }

    public Employe getEmploye() {
        return employe;
    }

    public void setEmploye(Employe employe) {
        this.employe = employe;
        this.employeId = employe != null ? employe.getId() : null;
    }

    public LocalDate getDatePlanifiee() {
        return datePlanifiee;
    }

    public void setDatePlanifiee(LocalDate datePlanifiee) {
        this.datePlanifiee = datePlanifiee;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public Integer getDureeMinutes() {
        return dureeMinutes;
    }

    public void setDureeMinutes(Integer dureeMinutes) {
        this.dureeMinutes = dureeMinutes;
    }

    public Boolean getTerminee() {
        return terminee;
    }

    public void setTerminee(Boolean terminee) {
        this.terminee = terminee;
    }

    // === MÉTHODES UTILITAIRES ===

    public boolean isTerminee() {
        return Boolean.TRUE.equals(terminee);
    }

    public void marquerTerminee() {
        this.terminee = true;
    }

    public LocalTime getHeureFin() {
        return heureDebut != null && dureeMinutes != null
                ? heureDebut.plusMinutes(dureeMinutes)
                : null;
    }

    @Override
    public String toString() {
        return "Planification{" +
                "id=" + getIdAsString() +
                ", orderId=" + (orderId != null ? orderId.toString() : "null") +
                ", employeId=" + (employeId != null ? employeId.toString() : "null") +
                ", datePlanifiee=" + datePlanifiee +
                ", heureDebut=" + heureDebut +
                ", dureeMinutes=" + dureeMinutes +
                ", terminee=" + terminee +
                '}';
    }
}