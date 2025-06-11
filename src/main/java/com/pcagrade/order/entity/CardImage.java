package com.pcagrade.order.entity;

import com.github.f4b6a3.hibernate.AbstractUlidEntity;
import com.github.f4b6a3.hibernate.LocalizationColumnDefinitions;
import com.github.f4b6a3.hibernate.UlidColumnDefinitions;
import com.github.f4b6a3.hibernate.UlidType;
import com.github.f4b6a3.ulid.Ulid;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

@Data
@Entity
@Table(name = "card_image", indexes = {
        @Index(name = "card_image_card_id_idx", columnList = "card_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "card_image_card_id_localization_uq", columnNames = {"card_id", "localization"})
})

public class CardImage extends AbstractUlidEntity{

    @Column(name = "card_id", nullable = false, columnDefinition = UlidColumnDefinitions.DEFINITION)
    @Type(UlidType.class)
    private Ulid cardId;

    @Column(name = "langue",nullable = false, columnDefinition = LocalizationColumnDefinitions.DEFINITION)
    private Localization localization;

    @ManyToOne
    @JoinColumn(name = "image_id")
    private Image image;

//    @OneToMany(mappedBy = "image", cascade = CascadeType.ALL)
//    private List<CardImageHistory> history;


    @Column(name = "fichier", nullable = false)
    private String fichier = "toto";

    @Column(name = "traits", nullable = false, columnDefinition = "longtext")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> traits=Collections.emptyMap();

    @Column(name = "statut", nullable = false)
    private Integer statut=0;

    @Column(name = "infos", nullable = false, columnDefinition = "longtext")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> infos= Collections.emptyMap();

    @Column(name = "downloaded_at", nullable = false)
    private Instant downloadedAt = Instant.now();

    @Column(name = "taille_img", length = 50)
    private String tailleImg;

    @Column(name = "cards")
    private String cards;

    @Column(name = "src")
    private String src;

    /// ///////////////////////////////////////////////////////////

}
