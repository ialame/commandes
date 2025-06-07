package com.pcagrade.order.entity;

import com.github.f4b6a3.hibernate.AbstractUlidEntity;
import com.github.f4b6a3.ulid.Ulid;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import java.util.Map;
import java.util.HashMap;

@Entity
@Table(name = "`order`")  // Échapper le mot-clé SQL
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class Commande extends AbstractUlidEntity {

    @Column(name = "num")
    private Integer num;

    @Column(name = "customer_id", columnDefinition = "BINARY(16)")
    private Ulid customerId;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "num_commande")
    private String numeroCommande;

    @Column(name = "status")
    private Integer status;

    @Column(name = "delai")
    private String delai;  // ← String, pas Integer !

    @Column(name = "employe_id")
    private String employeId;  // ← String selon la table

    @Column(name = "nombre_cartes")
    private Integer nombreCartes;

    @Column(name = "priorite_string")
    private String priorite;

    @Column(name = "prix_total")
    private Double prixTotal;

    @Column(name = "temps_estime_minutes")
    private Integer tempsEstimeMinutes;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    // ========== CHAMPS MANQUANTS ==========

    @Column(name = "date_limite")
    private LocalDateTime dateLimite;

    @Column(name = "date_debut_traitement")
    private LocalDateTime dateDebutTraitement;

    @Column(name = "date_fin_traitement")
    private LocalDateTime dateFinTraitement;

    @Column(name = "nb_descellements")
    private Integer nbDescellements;

    // ========== MÉTHODES ALIAS POUR COMPATIBILITÉ ==========

    /**
     * Alias pour setNumeroCommande() - compatibilité avec Test.java
     */
    public void setNumCommande(String numCommande) {
        this.numeroCommande = numCommande;
    }

    public String getNumCommande() {
        return this.numeroCommande;
    }


    // Votre relation N-N existante
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "card_certification_order",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "card_certification_id")
    )
    @ToString.Exclude
    private Set<CardCertification> cardCertifications = new HashSet<>();

    // Vos méthodes utilitaires existantes pour les cartes...
    public int getNombreCartes() {
        return cardCertifications != null ? cardCertifications.size() : 0;
    }

    // etc...

    /**
     * Retourne un résumé des cartes avec leurs quantités
     */
    public Map<String, Long> getResumerCartes() {
        if (cardCertifications == null) return new HashMap<>();

        return cardCertifications.stream()
                .collect(Collectors.groupingBy(
                        certification -> {
                            Card card = certification.getCard();
                            if (card != null && card.getTranslations() != null) {
                                return card.getTranslations().stream()
                                        .filter(t -> "en".equals(t.getLocale()) || "en_US".equals(t.getLocale()))
                                        .findFirst()
                                        .map(CardTranslation::getName)
                                        .orElse("Carte #" + card.getNum());
                            }
                            return "Carte inconnue";
                        },
                        Collectors.counting()
                ));
    }

    // ========== MÉTHODES UTILITAIRES POUR LES CARTES ==========

    /**
     * Retourne la liste des noms de cartes (en anglais par défaut)
     */
    public List<String> getNomsCartes() {
        if (cardCertifications == null) return new ArrayList<>();

        return cardCertifications.stream()
                .map(certification -> {
                    Card card = certification.getCard();
                    if (card != null && card.getTranslations() != null) {
                        // Chercher la traduction anglaise
                        return card.getTranslations().stream()
                                .filter(t -> "en".equals(t.getLocale()))
                                .findFirst()
                                .map(CardTranslation::getName)
                                .orElse("Carte #" + card.getNum());
                    }
                    return "Carte inconnue";
                })
                .collect(Collectors.toList());
    }

}