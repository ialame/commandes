package com.github.f4b6a3.hibernate;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * Classe de base pour les entités utilisant des ULID
 *
 * Stockage : UUID en base (BINARY(16))
 * Utilisation : ULID en Java
 * Conversion automatique via UlidToUuidConverter
 */
@MappedSuperclass
public abstract class AbstractUlidEntity {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    @Convert(converter = UlidToUuidConverter.class)
    private Ulid id = UlidCreator.getUlid();

    public AbstractUlidEntity() {
        this.id = UlidCreator.getUlid();
    }

    // Getters & Setters
    public Ulid getId() {
        return id;
    }

    public void setId(Ulid id) {
        this.id = id;
    }

    // Méthodes utilitaires
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AbstractUlidEntity that = (AbstractUlidEntity) obj;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + "}";
    }
}