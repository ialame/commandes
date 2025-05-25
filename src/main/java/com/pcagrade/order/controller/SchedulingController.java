package com.pcagrade.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scheduling")
@CrossOrigin(origins = "http://localhost:5173")
public class SchedulingController {

    @Autowired
    private SchedulingService schedulingService;

    @PostMapping("/schedule-all")
    public ResponseEntity<SchedulingResult> scheduleAllOrders() {
        SchedulingResult result = schedulingService.scheduleAllOrders();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/reschedule/{orderId}")
    public ResponseEntity<Void> rescheduleOrder(@PathVariable Long orderId) {
        schedulingService.rescheduleFromOrder(orderId);
        return ResponseEntity.ok().build();
    }
}
