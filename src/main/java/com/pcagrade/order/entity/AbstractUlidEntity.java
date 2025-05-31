package com.pcagrade.order.entity;

import com.pcagrade.order.ulid.Ulid;
import com.pcagrade.order.ulid.UlidType;
import org.hibernate.annotations.Type;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;


@MappedSuperclass
public abstract class AbstractUlidEntity {

    @Id
    @Type(UlidType.class)
    @Column(name = "id", nullable = false, columnDefinition = "BINARY(16)")  // ✅ Spécifiez le type
    private Ulid id;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    // Constructeurs
    protected AbstractUlidEntity() {
        this.id = Ulid.generate();
        this.dateCreation = LocalDateTime.now();
    }

    protected AbstractUlidEntity(Ulid id) {
        this.id = id != null ? id : Ulid.generate();
        this.dateCreation = LocalDateTime.now();
    }

    protected AbstractUlidEntity(String idString) {
        this.id = idString != null ? Ulid.fromString(idString) : Ulid.generate();
        this.dateCreation = LocalDateTime.now();
    }

    // Getters et Setters
    public Ulid getId() {
        return id;
    }

    public void setId(Ulid id) {
        this.id = id;
    }

    public String getIdAsString() {
        return id != null ? id.toString() : null;
    }

    public LocalDateTime getDateCreation() {  // Assurez-vous que c'est public
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    // Méthodes utilitaires
    public long getTimestampFromId() {
        return id != null ? id.extractTimestamp() : 0L;
    }

    public Instant getInstantFromId() {
        return id != null ? id.extractInstant() : Instant.now();
    }

    public void regenerateId() {
        this.id = Ulid.generate();
    }

    // Méthodes Object
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AbstractUlidEntity that = (AbstractUlidEntity) obj;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + getIdAsString() + "}";
    }
}