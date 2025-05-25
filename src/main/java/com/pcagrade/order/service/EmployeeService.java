package com.pcagrade.order.service;


import com.pcagrade.order.dto.EmployeeDTO;
import com.pcagrade.order.repository.EmployeeRepository;
import com.pcagrade.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrderRepository orderRepository;

    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<EmployeeDTO> getActiveEmployees() {
        return employeeRepository.findByIsActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public EmployeeDTO getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO) {
        Employee employee = new Employee(employeeDTO.getName(), employeeDTO.getEmail());
        Employee savedEmployee = employeeRepository.save(employee);
        return convertToDTO(savedEmployee);
    }

    public EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO) {
        return employeeRepository.findById(id)
                .map(employee -> {
                    employee.setName(employeeDTO.getName());
                    employee.setEmail(employeeDTO.getEmail());
                    if (employeeDTO.getIsActive() != null) {
                        employee.setIsActive(employeeDTO.getIsActive());
                    }
                    return convertToDTO(employeeRepository.save(employee));
                })
                .orElse(null);
    }

    public EmployeeDTO deactivateEmployee(Long id) {
        return employeeRepository.findById(id)
                .map(employee -> {
                    employee.setIsActive(false);

                    // Remettre les commandes assignées en attente
                    List<Order> assignedOrders = orderRepository.findByAssignedEmployeeAndStatusIn(
                            employee, Arrays.asList(OrderStatus.SCHEDULED, OrderStatus.IN_PROGRESS));

                    for (Order order : assignedOrders) {
                        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
                            order.setStatus(OrderStatus.PENDING);
                            order.setAssignedEmployee(null);
                            order.setScheduledDate(null);
                            orderRepository.save(order);
                        }
                    }

                    return convertToDTO(employeeRepository.save(employee));
                })
                .orElse(null);
    }

    public EmployeeDTO activateEmployee(Long id) {
        return employeeRepository.findById(id)
                .map(employee -> {
                    employee.setIsActive(true);
                    return convertToDTO(employeeRepository.save(employee));
                })
                .orElse(null);
    }

    private EmployeeDTO convertToDTO(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(employee.getId());
        dto.setName(employee.getName());
        dto.setEmail(employee.getEmail());
        dto.setIsActive(employee.getIsActive());
        dto.setCreatedAt(employee.getCreatedAt());

        // Calculer la charge de travail actuelle
        List<Order> currentOrders = orderRepository.findByAssignedEmployeeAndStatusIn(
                employee, Arrays.asList(OrderStatus.SCHEDULED, OrderStatus.IN_PROGRESS));

        dto.setCurrentWorkload(currentOrders.size());
        dto.setTotalEstimatedMinutes(currentOrders.stream()
                .mapToInt(Order::getEstimatedDurationMinutes)
                .sum());

        return dto;
    }
}
