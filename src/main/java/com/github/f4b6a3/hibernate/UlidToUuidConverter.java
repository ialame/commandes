package com.github.f4b6a3.hibernate;

import com.github.f4b6a3.ulid.Ulid;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.UUID;

/**
 * Convertisseur automatique entre ULID (Java) et UUID (Base de données)
 *
 * Permet d'utiliser des ULID dans le code Java tout en stockant
 * des UUID dans la base de données pour la compatibilité.
 */
@Converter(autoApply = true)
public class UlidToUuidConverter implements AttributeConverter<Ulid, UUID> {

    @Override
    public UUID convertToDatabaseColumn(Ulid ulid) {
        if (ulid == null) {
            return null;
        }
        System.out.println("🔄 ULID → UUID: " + ulid + " → " + ulid.toUuid());
        return ulid.toUuid();
    }

    @Override
    public Ulid convertToEntityAttribute(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        try {
            Ulid ulid = Ulid.from(uuid);
            System.out.println("🔄 UUID → ULID: " + uuid + " → " + ulid);
            return ulid;
        } catch (Exception e) {
            System.err.println("❌ Erreur conversion UUID → ULID: " + uuid + " - " + e.getMessage());
            throw e;
        }
    }
}