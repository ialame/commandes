package com.pcagrade.order.controller;

import com.pcagrade.order.entity.Order;
import com.pcagrade.order.ulid.Ulid;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        // Création
        Ulid id = Ulid.random();
        Order order = new Order();  // Constructeur par défaut
        order.setNumCommande("CMD-001");
        order.setNbDescellements(5);
// Accès
        System.out.println("ID: " + order.getId());
        System.out.println("Créé le: " + order.getDateCreation());

// Comparaison
        List<Order> orders = new ArrayList<>(); // au lieu de List<Commande>
        orders.sort((c1, c2) -> c1.getId().compareTo(c2.getId())); // Tri chronologique
    }
}
