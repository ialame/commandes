package com.pcagrade.order.ulid;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

/**
 * Représente un ULID (Universally Unique Lexicographically Sortable Identifier)
 * Immutable et thread-safe
 */
public final class Ulid implements Serializable, Comparable<Ulid> {

    private static final long serialVersionUID = 1L;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] CROCKFORD_BASE32 = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();

    // Les 16 bytes qui composent l'ULID
    private final byte[] bytes;

    // Cache pour la représentation String (calculée une seule fois)
    private transient String stringRepresentation;
    private transient Long timestamp;

    // Constructeurs privés pour contrôler la création
    private Ulid(byte[] bytes) {
        if (bytes == null || bytes.length != 16) {
            throw new IllegalArgumentException("ULID doit faire exactement 16 bytes");
        }
        this.bytes = Arrays.copyOf(bytes, 16); // Copie défensive
    }

    // Méthodes de création statiques

    /**
     * Génère un nouvel ULID
     */
    public static Ulid random() {
        return new Ulid(generateBytes());
    }

    /**
     * Crée un ULID à partir d'un tableau de bytes
     */
    public static Ulid fromBytes(byte[] bytes) {
        return new Ulid(bytes);
    }

    /**
     * Crée un ULID à partir d'une String (Base32 Crockford - 26 caractères)
     */
    public static Ulid fromString(String ulidString) {
        if (ulidString == null || ulidString.length() != 26) {
            throw new IllegalArgumentException("ULID string doit faire exactement 26 caractères");
        }
        return new Ulid(parseString(ulidString));
    }

    /**
     * Crée un ULID à partir d'un timestamp et de bytes aléatoires
     */
    public static Ulid of(long timestampMillis, byte[] randomBytes) {
        if (randomBytes == null || randomBytes.length != 10) {
            throw new IllegalArgumentException("Random bytes doivent faire exactement 10 bytes");
        }

        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putShort((short) (timestampMillis >>> 32));
        buffer.putInt((int) (timestampMillis & 0xFFFFFFFFL));
        buffer.put(randomBytes);

        return new Ulid(buffer.array());
    }

    // Méthodes d'accès

    /**
     * Retourne les bytes de l'ULID (copie défensive)
     */
    public byte[] getBytes() {
        return Arrays.copyOf(bytes, 16);
    }

    /**
     * Retourne la représentation String de l'ULID
     */
    @Override
    public String toString() {
        if (stringRepresentation == null) {
            stringRepresentation = bytesToString(bytes);
        }
        return stringRepresentation;
    }

    /**
     * Extrait le timestamp (en millisecondes) de l'ULID
     */
    public long getTimestamp() {
        if (timestamp == null) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            timestamp = ((long) buffer.getShort() << 32) | (buffer.getInt() & 0xFFFFFFFFL);
        }
        return timestamp;
    }

    /**
     * Extrait l'Instant de création de l'ULID
     */
    public Instant getInstant() {
        return Instant.ofEpochMilli(getTimestamp());
    }

    /**
     * Extrait la partie aléatoire de l'ULID
     */
    public byte[] getRandomBytes() {
        return Arrays.copyOfRange(bytes, 6, 16);
    }

    // Méthodes de validation

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
                getBase32Value(c);
            }
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // Méthodes Object

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Ulid ulid = (Ulid) obj;
        return Arrays.equals(bytes, ulid.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    // Implémentation Comparable (tri lexicographique)
    @Override
    public int compareTo(Ulid other) {
        if (other == null) return 1;

        // Comparer d'abord par timestamp
        int timestampComparison = Long.compare(this.getTimestamp(), other.getTimestamp());
        if (timestampComparison != 0) {
            return timestampComparison;
        }

        // Si même timestamp, comparer par bytes
        return Arrays.compare(this.bytes, other.bytes);
    }

    // Méthodes privées utilitaires

    private static byte[] generateBytes() {
        long timestamp = Instant.now().toEpochMilli();
        byte[] randomBytes = new byte[10];
        RANDOM.nextBytes(randomBytes);

        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putShort((short) (timestamp >>> 32));
        buffer.putInt((int) (timestamp & 0xFFFFFFFFL));
        buffer.put(randomBytes);

        return buffer.array();
    }

    private static String bytesToString(byte[] ulidBytes) {
        StringBuilder sb = new StringBuilder(26);

        // Timestamp (6 premiers bytes -> 10 caractères)
        long timestamp = 0;
        for (int i = 0; i < 6; i++) {
            timestamp = (timestamp << 8) | (ulidBytes[i] & 0xFF);
        }

        for (int i = 9; i >= 0; i--) {
            sb.append(CROCKFORD_BASE32[(int) (timestamp >>> (i * 5)) & 0x1F]);
        }

        // Random part (10 derniers bytes -> 16 caractères)
        long randomPart1 = 0;
        for (int i = 6; i < 11; i++) {
            randomPart1 = (randomPart1 << 8) | (ulidBytes[i] & 0xFF);
        }

        long randomPart2 = 0;
        for (int i = 11; i < 16; i++) {
            randomPart2 = (randomPart2 << 8) | (ulidBytes[i] & 0xFF);
        }

        for (int i = 7; i >= 0; i--) {
            sb.append(CROCKFORD_BASE32[(int) (randomPart1 >>> (i * 5)) & 0x1F]);
        }
        for (int i = 7; i >= 0; i--) {
            sb.append(CROCKFORD_BASE32[(int) (randomPart2 >>> (i * 5)) & 0x1F]);
        }

        return sb.toString();
    }

    private static byte[] parseString(String ulidString) {
        ulidString = ulidString.toUpperCase();
        ByteBuffer buffer = ByteBuffer.allocate(16);

        // Décoder le timestamp (10 premiers caractères)
        long timestamp = 0;
        for (int i = 0; i < 10; i++) {
            char c = ulidString.charAt(i);
            int value = getBase32Value(c);
            timestamp = (timestamp << 5) | value;
        }

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

        for (int i = 4; i >= 0; i--) {
            buffer.put((byte) (randomPart1 >>> (i * 8)));
        }
        for (int i = 4; i >= 0; i--) {
            buffer.put((byte) (randomPart2 >>> (i * 8)));
        }

        return buffer.array();
    }

    private static int getBase32Value(char c) {
        if (c >= '0' && c <= '9') return c - '0';
        if (c >= 'A' && c <= 'H') return c - 'A' + 10;
        if (c == 'J' || c == 'K') return c - 'A' + 10 - 1;
        if (c >= 'M' && c <= 'N') return c - 'A' + 10 - 2;
        if (c >= 'P' && c <= 'T') return c - 'A' + 10 - 3;
        if (c >= 'V' && c <= 'Z') return c - 'A' + 10 - 4;
        throw new IllegalArgumentException("Caractère ULID invalide: " + c);
    }

    /**
     * Alias pour random() - pour compatibilité avec AbstractUlidEntity
     */
    public static Ulid generate() {
        return random();
    }

    /**
     * Alias pour getTimestamp() - pour compatibilité avec AbstractUlidEntity
     */
    public long extractTimestamp() {
        return getTimestamp();
    }

    /**
     * Alias pour getInstant() - pour compatibilité avec AbstractUlidEntity
     */
    public Instant extractInstant() {
        return getInstant();
    }

    /**
     * Méthode utilitaire pour créer un ULID à partir d'un Long (si nécessaire)
     * Attention: cette méthode assume que le Long représente un ID au format String
     */
    public static Ulid fromLong(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID ne peut pas être null");
        }
        // Si votre Long représente en fait un ULID stocké comme String
        // vous devrez adapter cette méthode selon votre logique métier
        return fromString(id.toString());
    }


}
