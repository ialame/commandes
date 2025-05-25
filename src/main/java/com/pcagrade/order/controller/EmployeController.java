package com.pcagrade.order.controller;// EmployeController.java

import com.pcagrade.order.entity.Employe;
import com.pcagrade.order.service.CommandeService;
import com.pcagrade.order.service.EmployeService;
import com.pcagrade.order.service.PlanificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employes")
@CrossOrigin(origins = "*")
public class EmployeController {

    @Autowired
    private com.pcagrade.order.service.EmployeService employeService;

    @GetMapping
    public ResponseEntity<List<Employe>> getTousEmployes() {
        List<Employe> employes = employeService.getTousEmployes();
        return ResponseEntity.ok(employes);
    }

    @GetMapping("/actifs")
    public ResponseEntity<List<Employe>> getEmployesActifs() {
        List<Employe> employes = employeService.getEmployesActifs();
        return ResponseEntity.ok(employes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employe> getEmployeById(@PathVariable Long id) {
        try {
            Employe employe = employeService.getEmployeById(id);
            return ResponseEntity.ok(employe);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Employe> creerEmploye(@Valid @RequestBody Employe employe) {
        try {
            Employe nouvelEmploye = employeService.creerEmploye(employe);
            return ResponseEntity.ok(nouvelEmploye);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employe> modifierEmploye(@PathVariable Long id, @Valid @RequestBody Employe employe) {
        try {
            employe.setId(id);
            Employe employeModifie = employeService.modifierEmploye(employe);
            return ResponseEntity.ok(employeModifie);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> desactiverEmploye(@PathVariable Long id) {
        try {
            employeService.desactiverEmploye(id);
            return ResponseEntity.ok("Employé désactivé");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

