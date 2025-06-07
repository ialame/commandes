package com.pcagrade.order;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderApplicationTests {

    @Test
    void contextLoads() {
        // Test de base Spring Boot
    }

    @Test
    void testUlidConversion() {

        Ulid original = UlidCreator.getMonotonicUlid();


        System.out.println("ULID original: " + original.toString());
        UUID uuid = original.toUuid();
        System.out.println("Bytes hex: " + original.toUuid().toString());
        //Ulid ulid = UlidCreator.
        //System.out.println("ULID restauré: " + restored.toString());
    }

    @Test
    void testUlidGeneration() {
        Ulid ulid1 = UlidCreator.getMonotonicUlid();
        Ulid ulid2 = UlidCreator.getMonotonicUlid();

        assertNotNull(ulid1);
        assertNotNull(ulid2);
        assertNotEquals(ulid1.toString(), ulid2.toString());

        // Vérifier que les ULID sont bien de 26 caractères
        assertEquals(26, ulid1.toString().length());
        assertEquals(26, ulid2.toString().length());
    }

    @Test
    void testUlidTimestamp() {
        Ulid ulid = UlidCreator.getMonotonicUlid();
        long timestamp = ulid.getTime();

        // Le timestamp doit être récent (dans les dernières secondes)
        long now = System.currentTimeMillis();
        assertTrue(Math.abs(now - timestamp) < 5000); // Moins de 5 secondes
    }
}