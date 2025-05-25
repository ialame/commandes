package com.pcagrade.order.controller;


import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        OrderDTO order = orderService.getOrderById(id);
        return order != null ? ResponseEntity.ok(order) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody OrderDTO orderDTO) {
        OrderDTO createdOrder = orderService.createOrder(orderDTO);
        return ResponseEntity.ok(createdOrder);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable Long id,
                                                @Valid @RequestBody OrderDTO orderDTO) {
        OrderDTO updatedOrder = orderService.updateOrder(id, orderDTO);
        return updatedOrder != null ? ResponseEntity.ok(updatedOrder) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        boolean deleted = orderService.deleteOrder(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDTO>> getOrdersByStatus(@PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<OrderDTO>> getOrdersByPriority(@PathVariable PriorityLevel priority) {
        return ResponseEntity.ok(orderService.getOrdersByPriority(priority));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<OrderDTO>> getPendingOrders() {
        return ResponseEntity.ok(orderService.getPendingOrders());
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<OrderDTO>> getOverdueOrders() {
        return ResponseEntity.ok(orderService.getOverdueOrders());
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<OrderDTO> completeOrder(@PathVariable Long id) {
        OrderDTO completedOrder = orderService.completeOrder(id);
        return completedOrder != null ? ResponseEntity.ok(completedOrder) : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<OrderDTO> startOrder(@PathVariable Long id) {
        OrderDTO startedOrder = orderService.startOrder(id);
        return startedOrder != null ? ResponseEntity.ok(startedOrder) : ResponseEntity.notFound().build();
    }
}