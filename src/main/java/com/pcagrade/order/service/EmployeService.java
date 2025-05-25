package com.pcagrade.order.service;

import com.pcagrade.order.entity.Employe;
import com.pcagrade.order.repository.EmployeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class EmployeService {

    @Autowired
    private EmployeRepository employeRepository;

    public Employe creerEmploye(Employe employe) {
        if (employeRepository.existsByEmail(employe.getEmail())) {
            throw new RuntimeException("Un employé avec cet email existe déjà");
        }
        return employeRepository.save(employe);
    }

    public List<Employe> getTousEmployes() {
        return employeRepository.findAll();
    }

    public List<Employe> getEmployesActifs() {
        return employeRepository.findEmployesActifsOrdonnes();
    }

    public Employe getEmployeById(Long id) {
        return employeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));
    }

    public Employe modifierEmploye(Employe employe) {
        Employe existant = getEmployeById(employe.getId());
        existant.setNom(employe.getNom());
        existant.setPrenom(employe.getPrenom());
        existant.setEmail(employe.getEmail());
        existant.setHeuresTravailParJour(employe.getHeuresTravailParJour());
        existant.setActif(employe.getActif());
        return employeRepository.save(existant);
    }

    public void desactiverEmploye(Long id) {
        Employe employe = getEmployeById(id);
        employe.setActif(false);
        employeRepository.save(employe);
    }
}
