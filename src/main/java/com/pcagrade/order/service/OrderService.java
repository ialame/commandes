package com.pcagrade.order.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public OrderDTO createOrder(OrderDTO orderDTO) {
        Order order = new Order(orderDTO.getCustomerName(), orderDTO.getPrice(), orderDTO.getCardCount());
        Order savedOrder = orderRepository.save(order);
        return convertToDTO(savedOrder);
    }

    public OrderDTO updateOrder(Long id, OrderDTO orderDTO) {
        return orderRepository.findById(id)
                .map(order -> {
                    order.setCustomerName(orderDTO.getCustomerName());
                    order.setPrice(orderDTO.getPrice());
                    order.setCardCount(orderDTO.getCardCount());

                    // Si l'employé assigné a changé
                    if (orderDTO.getAssignedEmployeeId() != null) {
                        Employee employee = employeeRepository.findById(orderDTO.getAssignedEmployeeId()).orElse(null);
                        order.setAssignedEmployee(employee);
                    }

                    return convertToDTO(orderRepository.save(order));
                })
                .orElse(null);
    }

    public boolean deleteOrder(Long id) {
        if (orderRepository.existsById(id)) {
            orderRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<OrderDTO> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getOrdersByPriority(PriorityLevel priority) {
        return orderRepository.findByPriorityLevelOrderByCreatedAtAsc(priority).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getPendingOrders() {
        return orderRepository.findByStatus(OrderStatus.PENDING).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getOverdueOrders() {
        return orderRepository.findOverdueOrders(LocalDateTime.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO completeOrder(Long id) {
        return orderRepository.findById(id)
                .map(order -> {
                    order.setStatus(OrderStatus.COMPLETED);
                    order.setCompletedAt(LocalDateTime.now());
                    return convertToDTO(orderRepository.save(order));
                })
                .orElse(null);
    }

    public OrderDTO startOrder(Long id) {
        return orderRepository.findById(id)
                .map(order -> {
                    if (order.getStatus() == OrderStatus.SCHEDULED) {
                        order.setStatus(OrderStatus.IN_PROGRESS);
                        return convertToDTO(orderRepository.save(order));
                    }
                    return convertToDTO(order);
                })
                .orElse(null);
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setCustomerName(order.getCustomerName());
        dto.setPrice(order.getPrice());
        dto.setCardCount(order.getCardCount());
        dto.setPriorityLevel(order.getPriorityLevel());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setDueDate(order.getDueDate());
        dto.setScheduledDate(order.getScheduledDate());
        dto.setCompletedAt(order.getCompletedAt());
        dto.setEstimatedDurationMinutes(order.getEstimatedDurationMinutes());

        if (order.getAssignedEmployee() != null) {
            dto.setAssignedEmployeeId(order.getAssignedEmployee().getId());
            dto.setAssignedEmployeeName(order.getAssignedEmployee().getName());
        }

        return dto;
    }
}
