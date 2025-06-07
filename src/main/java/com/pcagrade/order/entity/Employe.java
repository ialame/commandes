package com.pcagrade.order.entity;


import com.github.f4b6a3.hibernate.AbstractUlidEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.List;

@Entity
@Table(name = "employe")
@Data
@EqualsAndHashCode(callSuper = false)  // ✅ false pour éviter les conflits
@NoArgsConstructor
public class Employe extends AbstractUlidEntity {

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @Column(name = "email")
    private String email;

    @Column(name = "heures_travail_par_jour", nullable = false)
    private Integer heuresTravailParJour = 8;

    @Column(name = "actif", nullable = false)
    private Boolean actif = true;


    // Relations
    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Planification> planifications;

    // ========== CONSTRUCTEURS PERSONNALISÉS ==========

    public Employe(String nom, String prenom, String email) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
    }

    public Employe(String nom, String prenom, String email, Integer heuresTravailParJour) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.heuresTravailParJour = heuresTravailParJour;
    }

    // ========== MÉTHODES UTILITAIRES ==========

    public String getNomComplet() {
        return (prenom != null ? prenom + " " : "") + nom;
    }

    public boolean isDisponible() {
        return actif != null && actif;
    }

    // Pas besoin de toString() custom car Lombok s'en charge
    // mais on exclut les relations pour éviter les boucles infinies

    /**
     * Calcule les minutes de travail par jour à partir des heures
     */
    public Integer getMinutesParJour() {
        return heuresTravailParJour != null ? heuresTravailParJour * 60 : 480; // 8h par défaut
    }
}