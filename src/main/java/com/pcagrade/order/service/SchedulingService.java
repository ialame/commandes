package com.pcagrade.order.service;

import com.pcagrade.order.entity.Employee;
import com.pcagrade.order.entity.Order;
import com.pcagrade.order.repository.EmployeeRepository;
import com.pcagrade.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SchedulingService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private static final int DAILY_WORK_HOURS = 8;
    private static final int DAILY_WORK_MINUTES = DAILY_WORK_HOURS * 60;
    private static final LocalTime WORK_START_TIME = LocalTime.of(9, 0);

    /**
     * Planifie automatiquement toutes les commandes non assignées
     */
    public SchedulingResult scheduleAllOrders() {
        List<Order> unassignedOrders = orderRepository.findUnassignedOrdersByPriority();
        List<Employee> activeEmployees = employeeRepository.findByIsActiveTrue();

        if (activeEmployees.isEmpty()) {
            return new SchedulingResult(false, "Aucun employé actif disponible", 0);
        }

        // Créer une map de la charge de travail actuelle par employé
        Map<Employee, List<ScheduledTask>> employeeSchedule = initializeEmployeeSchedules(activeEmployees);

        int scheduledCount = 0;
        List<String> warnings = new ArrayList<>();

        for (Order order : unassignedOrders) {
            Employee bestEmployee = findBestEmployeeForOrder(order, employeeSchedule);
            if (bestEmployee != null) {
                LocalDateTime scheduledTime = scheduleOrderForEmployee(order, bestEmployee, employeeSchedule);

                if (scheduledTime != null) {
                    order.setAssignedEmployee(bestEmployee);
                    order.setScheduledDate(scheduledTime);
                    order.setStatus(OrderStatus.SCHEDULED);
                    orderRepository.save(order);
                    scheduledCount++;

                    // Vérifier si la commande sera en retard
                    if (scheduledTime.isAfter(order.getDueDate())) {
                        warnings.add("Commande #" + order.getId() + " planifiée après la date limite");
                    }
                } else {
                    warnings.add("Impossible de planifier la commande #" + order.getId() + " dans les délais");
                }
            }
        }

        return new SchedulingResult(true,
                "Planification terminée. " + scheduledCount + " commandes planifiées.",
                scheduledCount, warnings);
    }

    /**
     * Initialise les plannings des employés avec leurs tâches actuelles
     */
    private Map<Employee, List<ScheduledTask>> initializeEmployeeSchedules(List<Employee> employees) {
        Map<Employee, List<ScheduledTask>> schedules = new HashMap<>();

        for (Employee employee : employees) {
            List<Order> assignedOrders = orderRepository.findByAssignedEmployeeAndStatusIn(
                    employee, Arrays.asList(OrderStatus.SCHEDULED, OrderStatus.IN_PROGRESS));

            List<ScheduledTask> tasks = assignedOrders.stream()
                    .filter(order -> order.getScheduledDate() != null)
                    .map(order -> new ScheduledTask(
                            order.getScheduledDate(),
                            order.getScheduledDate().plusMinutes(order.getEstimatedDurationMinutes()),
                            order))
                    .sorted(Comparator.comparing(ScheduledTask::getStartTime))
                    .collect(Collectors.toList());

            schedules.put(employee, tasks);
        }

        return schedules;
    }

    /**
     * Trouve le meilleur employé pour une commande donnée
     */
    private Employee findBestEmployeeForOrder(Order order, Map<Employee, List<ScheduledTask>> employeeSchedule) {
        Employee bestEmployee = null;
        LocalDateTime earliestAvailableTime = null;

        for (Employee employee : employeeSchedule.keySet()) {
            LocalDateTime availableTime = findEarliestAvailableTime(employee, order, employeeSchedule);

            if (availableTime != null &&
                    (earliestAvailableTime == null || availableTime.isBefore(earliestAvailableTime))) {
                earliestAvailableTime = availableTime;
                bestEmployee = employee;
            }
        }

        return bestEmployee;
    }

    /**
     * Trouve le premier créneau disponible pour un employé
     */
    private LocalDateTime findEarliestAvailableTime(Employee employee, Order order,
                                                    Map<Employee, List<ScheduledTask>> employeeSchedule) {
        List<ScheduledTask> tasks = employeeSchedule.get(employee);
        LocalDateTime now = LocalDateTime.now();
        LocalDate currentDate = now.toLocalDate();

        // Commencer à chercher à partir d'aujourd'hui
        for (int dayOffset = 0; dayOffset < 90; dayOffset++) { // Chercher sur 3 mois max
            LocalDate checkDate = currentDate.plusDays(dayOffset);
            LocalDateTime dayStart = checkDate.atTime(WORK_START_TIME);
            LocalDateTime dayEnd = checkDate.atTime(WORK_START_TIME.plusHours(DAILY_WORK_HOURS));

            // Si c'est aujourd'hui, commencer à partir de maintenant
            if (dayOffset == 0 && now.isAfter(dayStart)) {
                dayStart = now;
            }

            // Vérifier les créneaux libres dans cette journée
            LocalDateTime availableSlot = findAvailableSlotInDay(
                    tasks, dayStart, dayEnd, order.getEstimatedDurationMinutes());

            if (availableSlot != null) {
                return availableSlot;
            }
        }

        return null; // Aucun créneau trouvé
    }

    /**
     * Trouve un créneau libre dans une journée donnée
     */
    private LocalDateTime findAvailableSlotInDay(List<ScheduledTask> tasks,
                                                 LocalDateTime dayStart, LocalDateTime dayEnd,
                                                 int requiredMinutes) {
        // Filtrer les tâches de la journée
        List<ScheduledTask> dayTasks = tasks.stream()
                .filter(task -> task.getStartTime().toLocalDate().equals(dayStart.toLocalDate()))
                .sorted(Comparator.comparing(ScheduledTask::getStartTime))
                .collect(Collectors.toList());

        LocalDateTime currentTime = dayStart;

        for (ScheduledTask task : dayTasks) {
            // Vérifier l'espace avant cette tâche
            if (currentTime.plusMinutes(requiredMinutes).isBefore(task.getStartTime()) ||
                    currentTime.plusMinutes(requiredMinutes).isEqual(task.getStartTime())) {
                return currentTime;
            }
            currentTime = task.getEndTime();
        }

        // Vérifier l'espace après la dernière tâche
        if (currentTime.plusMinutes(requiredMinutes).isBefore(dayEnd) ||
                currentTime.plusMinutes(requiredMinutes).isEqual(dayEnd)) {
            return currentTime;
        }

        return null;
    }

    /**
     * Planifie une commande pour un employé spécifique
     */
    private LocalDateTime scheduleOrderForEmployee(Order order, Employee employee,
                                                   Map<Employee, List<ScheduledTask>> employeeSchedule) {
        LocalDateTime scheduledTime = findEarliestAvailableTime(order, employee, employeeSchedule);

        if (scheduledTime != null) {
            // Ajouter la nouvelle tâche au planning de l'employé
            ScheduledTask newTask = new ScheduledTask(
                    scheduledTime,
                    scheduledTime.plusMinutes(order.getEstimatedDurationMinutes()),
                    order);

            employeeSchedule.get(employee).add(newTask);
            employeeSchedule.get(employee).sort(Comparator.comparing(ScheduledTask::getStartTime));
        }

        return scheduledTime;
    }

    /**
     * Obtient le planning détaillé d'un employé
     */
    public List<ScheduledTask> getEmployeeSchedule(Long employeeId, LocalDate startDate, LocalDate endDate) {
        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        if (employee == null) return new ArrayList<>();

        List<Order> orders = orderRepository.findByAssignedEmployeeAndStatusIn(
                employee, Arrays.asList(OrderStatus.SCHEDULED, OrderStatus.IN_PROGRESS));

        return orders.stream()
                .filter(order -> order.getScheduledDate() != null)
                .filter(order -> {
                    LocalDate orderDate = order.getScheduledDate().toLocalDate();
                    return !orderDate.isBefore(startDate) && !orderDate.isAfter(endDate);
                })
                .map(order -> new ScheduledTask(
                        order.getScheduledDate(),
                        order.getScheduledDate().plusMinutes(order.getEstimatedDurationMinutes()),
                        order))
                .sorted(Comparator.comparing(ScheduledTask::getStartTime))
                .collect(Collectors.toList());
    }

    /**
     * Recalcule la planification après modification d'une commande
     */
    public void rescheduleFromOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null && order.getStatus() == OrderStatus.SCHEDULED) {
            // Remettre en attente et replanifier
            order.setStatus(OrderStatus.PENDING);
            order.setAssignedEmployee(null);
            order.setScheduledDate(null);
            orderRepository.save(order);

            scheduleAllOrders();
        }
    }
}