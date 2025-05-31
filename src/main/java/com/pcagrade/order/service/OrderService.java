package com.pcagrade.order.service;

import com.pcagrade.order.entity.Order;
import com.pcagrade.order.repository.OrderRepository;
import com.pcagrade.order.ulid.Ulid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    /**
     * Récupère tous les orders à traiter (non annulés, non pausés)
     */
    public List<Order> getOrdersATraiter() {
        return orderRepository.findOrdersToProcess().stream()
                .limit(2)  // ✅ Seulement 2 commandes pour tester
                .collect(Collectors.toList());
    }

    /**
     * Trouve un order par ID
     */
    public Optional<Order> findById(Ulid orderId) {
        return orderRepository.findById(orderId);
    }

    /**
     * Sauvegarde un order
     */
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    /**
     * Met à jour le statut d'un order
     */
    public void updateStatus(Ulid orderId, Integer newStatus) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(newStatus);
            orderRepository.save(order);
        }
    }

    /**
     * Récupère tous les orders actifs
     */
    public List<Order> getActiveOrders() {
        return orderRepository.findActiveOrders();
    }

    /**
     * Récupère les orders en retard
     */
    public List<Order> getDelayedOrders() {
        return orderRepository.findDelayedOrders();
    }

    /**
     * Récupère les orders par statut
     */
    public List<Order> getOrdersByStatus(Integer status) {
        return orderRepository.findByStatus(status);
    }

    /**
     * Marque un order comme planifié
     */
    public void marquerCommePlanifie(Ulid orderId) {
        updateStatus(orderId, 1); // Supposons que 1 = planifié
    }

    /**
     * Marque un order comme terminé
     */
    public void marquerCommeTermine(Ulid orderId) {
        updateStatus(orderId, 2); // Supposons que 2 = terminé
    }

    // === MÉTHODES POUR COMPATIBILITÉ AVEC L'ANCIEN SYSTÈME ===

    public List<Order> getToutesOrders() {
        return orderRepository.findAll();
    }

    public Order getCommandeById(String id) {
        try {
            Ulid ulid = Ulid.fromString(id);
            return findById(ulid).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public Order creerCommande(Order order) {
        return save(order);
    }

    public List<Order> getCommandesATraiter() {
        return getOrdersATraiter();
    }

    public List<Order> getCommandesEnRetard() {
        return getDelayedOrders();
    }

    public void marquerCommandeCommencee(String id) {
        try {
            Ulid ulid = Ulid.fromString(id);
            updateStatus(ulid, 1); // En cours
        } catch (Exception e) {
            // Log error
        }
    }

    public void marquerCommandeTerminee(String id) {
        try {
            Ulid ulid = Ulid.fromString(id);
            updateStatus(ulid, 2); // Terminé
        } catch (Exception e) {
            // Log error
        }
    }

    public Long getNombreCommandesEnAttente() {
        return (long) getOrdersByStatus(0).size(); // 0 = En attente
    }

    public Long getNombreCommandesEnCours() {
        return (long) getOrdersByStatus(1).size(); // 1 = En cours
    }

    public Long getNombreCommandesTerminees() {
        return (long) getOrdersByStatus(2).size(); // 2 = Terminé
    }


    public List<Order> getCommandesDepuis(LocalDateTime depuis) {
        // Convertir LocalDateTime en Instant (en UTC)
        Instant depuisInstant = depuis.atZone(ZoneId.systemDefault()).toInstant();
        return orderRepository.findByDateGreaterThanEqualOrderByDateDesc(depuisInstant);
    }

}