package com.pcagrade.order.util;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

/**
 * Utilitaire pour la génération et manipulation d'ULIDs
 * (Universally Unique Lexicographically Sortable Identifier)
 */
public class UlidGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] CROCKFORD_BASE32 = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();

    /**
     * Génère un nouvel ULID en bytes (16 bytes)
     * Format: 6 bytes timestamp + 10 bytes randomness
     */
    public static byte[] generate() {
        // Timestamp en millisecondes depuis epoch
        long timestamp = Instant.now().toEpochMilli();

        // Générer 10 bytes de randomness
        byte[] randomBytes = new byte[10];
        RANDOM.nextBytes(randomBytes);

        // Combiner timestamp (6 bytes) + randomness (10 bytes) = 16 bytes total
        ByteBuffer buffer = ByteBuffer.allocate(16);

        // Encoder le timestamp sur 6 bytes (48 bits)
        buffer.putShort((short) (timestamp >>> 32));
        buffer.putInt((int) (timestamp & 0xFFFFFFFFL));

        // Ajouter les 10 bytes de randomness
        buffer.put(randomBytes);

        return buffer.array();
    }

    /**
     * Génère un nouvel ULID directement en String
     */
    public static String generateString() {
        return toString(generate());
    }

    /**
     * Convertit des bytes ULID en String (Base32 Crockford - 26 caractères)
     */
    public static String toString(byte[] ulidBytes) {
        if (ulidBytes == null || ulidBytes.length != 16) {
            throw new IllegalArgumentException("ULID doit faire exactement 16 bytes");
        }

        // Convertir en string Base32 Crockford (26 caractères)
        StringBuilder sb = new StringBuilder(26);

        // Traiter le timestamp (6 premiers bytes -> 10 caractères)
        long timestamp = 0;
        for (int i = 0; i < 6; i++) {
            timestamp = (timestamp << 8) | (ulidBytes[i] & 0xFF);
        }

        // Encoder le timestamp en base32 (10 caractères)
        for (int i = 9; i >= 0; i--) {
            sb.append(CROCKFORD_BASE32[(int) (timestamp >>> (i * 5)) & 0x1F]);
        }

        // Traiter la partie random (10 derniers bytes -> 16 caractères)
        long randomPart1 = 0;
        for (int i = 6; i < 11; i++) {
            randomPart1 = (randomPart1 << 8) | (ulidBytes[i] & 0xFF);
        }

        long randomPart2 = 0;
        for (int i = 11; i < 16; i++) {
            randomPart2 = (randomPart2 << 8) | (ulidBytes[i] & 0xFF);
        }

        // Encoder en base32 (8 + 8 = 16 caractères)
        for (int i = 7; i >= 0; i--) {
            sb.append(CROCKFORD_BASE32[(int) (randomPart1 >>> (i * 5)) & 0x1F]);
        }
        for (int i = 7; i >= 0; i--) {
            sb.append(CROCKFORD_BASE32[(int) (randomPart2 >>> (i * 5)) & 0x1F]);
        }

        return sb.toString();
    }

    /**
     * Convertit une String ULID en bytes
     */
    public static byte[] fromString(String ulidString) {
        if (ulidString == null || ulidString.length() != 26) {
            throw new IllegalArgumentException("ULID string doit faire exactement 26 caractères");
        }

        ulidString = ulidString.toUpperCase();

        // Décoder de base32 vers bytes
        ByteBuffer buffer = ByteBuffer.allocate(16);

        // Décoder le timestamp (10 premiers caractères)
        long timestamp = 0;
        for (int i = 0; i < 10; i++) {
            char c = ulidString.charAt(i);
            int value = getBase32Value(c);
            timestamp = (timestamp << 5) | value;
        }

        // Stocker le timestamp sur 6 bytes
        buffer.putShort((short) (timestamp >>> 32));
        buffer.putInt((int) (timestamp & 0xFFFFFFFFL));

        // Décoder la partie random (16 derniers caractères)
        long randomPart1 = 0;
        for (int i = 10; i < 18; i++) {
            char c = ulidString.charAt(i);
            int value = getBase32Value(c);
            randomPart1 = (randomPart1 << 5) | value;
        }

        long randomPart2 = 0;
        for (int i = 18; i < 26; i++) {
            char c = ulidString.charAt(i);
            int value = getBase32Value(c);
            randomPart2 = (randomPart2 << 5) | value;
        }

        // Stocker les parties random
        for (int i = 4; i >= 0; i--) {
            buffer.put((byte) (randomPart1 >>> (i * 8)));
        }
        for (int i = 4; i >= 0; i--) {
            buffer.put((byte) (randomPart2 >>> (i * 8)));
        }

        return buffer.array();
    }

    /**
     * Extrait le timestamp (en millisecondes) d'un ULID
     */
    public static long extractTimestamp(byte[] ulidBytes) {
        if (ulidBytes == null || ulidBytes.length != 16) {
            throw new IllegalArgumentException("ULID invalide");
        }

        ByteBuffer buffer = ByteBuffer.wrap(ulidBytes);
        long timestamp = ((long) buffer.getShort() << 32) | (buffer.getInt() & 0xFFFFFFFFL);
        return timestamp;
    }

    /**
     * Extrait le timestamp d'un ULID String
     */
    public static long extractTimestamp(String ulidString) {
        return extractTimestamp(fromString(ulidString));
    }

    /**
     * Obtient l'Instant de création d'un ULID
     */
    public static Instant extractInstant(byte[] ulidBytes) {
        return Instant.ofEpochMilli(extractTimestamp(ulidBytes));
    }

    /**
     * Obtient l'Instant de création d'un ULID String
     */
    public static Instant extractInstant(String ulidString) {
        return extractInstant(fromString(ulidString));
    }

    /**
     * Valide qu'une String est un ULID valide
     */
    public static boolean isValid(String ulidString) {
        if (ulidString == null || ulidString.length() != 26) {
            return false;
        }

        try {
            ulidString = ulidString.toUpperCase();
            for (char c : ulidString.toCharArray()) {
                getBase32Value(c); // Lève une exception si invalide
            }
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Obtient la valeur numérique d'un caractère base32 Crockford
     */
    private static int getBase32Value(char c) {
        if (c >= '0' && c <= '9') return c - '0';
        if (c >= 'A' && c <= 'H') return c - 'A' + 10;
        if (c == 'J' || c == 'K') return c - 'A' + 10 - 1; // J=18, K=19
        if (c >= 'M' && c <= 'N') return c - 'A' + 10 - 2; // M=20, N=21
        if (c >= 'P' && c <= 'T') return c - 'A' + 10 - 3; // P=22, Q=23, R=24, S=25, T=26
        if (c >= 'V' && c <= 'Z') return c - 'A' + 10 - 4; // V=27, W=28, X=29, Y=30, Z=31
        throw new IllegalArgumentException("Caractère ULID invalide: " + c);
    }
}