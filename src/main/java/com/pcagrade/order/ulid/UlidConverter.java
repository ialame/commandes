package com.pcagrade.order.ulid;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convertisseur JPA pour ULID
 * Alternative plus simple au UserType
 */
@Converter(autoApply = true)
public class UlidConverter implements AttributeConverter<Ulid, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(Ulid ulid) {
        System.out.println("🔥🔥🔥 CONVERTER APPELÉ - convertToDatabaseColumn");
        if (ulid == null) {
            System.out.println("❌ ULID est null, retour null");
            return null;
        }

        byte[] bytes = ulid.getBytes();
        System.out.println("✅ ULID: " + ulid.toString());
        System.out.println("✅ ULID length: " + ulid.toString().length());
        System.out.println("✅ Bytes length: " + bytes.length);
        System.out.println("✅ Bytes hex: " + bytesToHex(bytes));

        // VÉRIFICATION CRITIQUE
        if (bytes.length != 16) {
            System.out.println("🚨🚨🚨 PROBLÈME: Les bytes ne font pas 16! Taille: " + bytes.length);
            throw new RuntimeException("ULID bytes should be exactly 16, got: " + bytes.length);
        }

        System.out.println("✅ Conversion OK, retour de " + bytes.length + " bytes");
        return bytes;
    }

    @Override
    public Ulid convertToEntityAttribute(byte[] dbData) {
        System.out.println("🔥🔥🔥 CONVERTER APPELÉ - convertToEntityAttribute");
        if (dbData == null) {
            System.out.println("❌ DB data est null");
            return null;
        }

        System.out.println("✅ DB data length: " + dbData.length);
        System.out.println("✅ DB data hex: " + bytesToHex(dbData));

        if (dbData.length != 16) {
            System.out.println("🚨🚨🚨 PROBLÈME: DB data ne fait pas 16 bytes! Taille: " + dbData.length);
            return null;
        }

        Ulid result = Ulid.fromBytes(dbData);
        System.out.println("✅ ULID reconstruit: " + result.toString());
        return result;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

// Et dans votre AbstractUlidEntity, utilisez simplement :
/*
@Id
@Convert(converter = UlidConverter.class)
@Column(name = "id", length = 26, nullable = false)
private Ulid id;
*/