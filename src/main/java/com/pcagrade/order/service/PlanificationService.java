package com.pcagrade.order.service;


import com.pcagrade.order.entity.Employe;
import com.pcagrade.order.entity.Planification;
import com.pcagrade.order.repository.PlanificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Service
@Transactional
public class PlanificationService {

    @Autowired
    PlanificationRepository planificationRepository;

    @Autowired
    private CommandeService commandeService;

    @Autowired
    private EmployeService employeService;

    public List<Planification> getPlanificationsByDate(LocalDate date) {
        return planificationRepository.findByDatePlanifiee(date);
    }

    public List<Planification> getPlanificationsByEmployeEtPeriode(Long employeId, LocalDate debut, LocalDate fin) {
        return planificationRepository.findByEmployeAndPeriode(employeId, debut, fin);
    }

    public Integer getChargeEmployeParJour(Long employeId, LocalDate date) {
        Integer totalMinutes = planificationRepository.getTotalMinutesParEmployeEtDate(employeId, date);
        return totalMinutes != null ? totalMinutes : 0;
    }

    public Planification creerPlanification(Planification planification) {
        // Vérifier que l'employé n'est pas surchargé
        Integer chargeActuelle = getChargeEmployeParJour(
                planification.getEmploye().getId(),
                planification.getDatePlanifiee()
        );

        Employe employe = planification.getEmploye();
        int capaciteJournaliere = employe.getHeuresTravailParJour() * 60; // en minutes

        if (chargeActuelle + planification.getDureeMinutes() > capaciteJournaliere) {
            throw new RuntimeException("L'employé n'a pas assez de capacité pour cette date");
        }

        return planificationRepository.save(planification);
    }

    public void marquerPlanificationTerminee(Long planificationId) {
        Planification planification = planificationRepository.findById(planificationId)
                .orElseThrow(() -> new RuntimeException("Planification non trouvée"));
        planification.setTerminee(true);
        planificationRepository.save(planification);
    }

    public List<Planification> getPlanificationsByPeriode(LocalDate debut, LocalDate fin) {
        List<Planification> planifications = planificationRepository.findPlanificationsByPeriode(debut, fin);

        // Forcer le chargement des relations
        planifications.forEach(p -> {
            p.getCommande().getNumeroCommande(); // Force le chargement
            p.getEmploye().getNom(); // Force le chargement
        });

        return planifications;
    }

    // Méthode pour obtenir la charge de travail par employé et date
    public Map<String, Object> getChargeParEmploye(LocalDate debut, LocalDate fin) {
        List<Employe> employes = employeService.getEmployesActifs();
        List<Planification> planifications = getPlanificationsByPeriode(debut, fin);

        Map<String, Object> resultat = new HashMap<>();
        List<Map<String, Object>> chargesEmployes = new ArrayList<>();

        for (Employe employe : employes) {
            Map<String, Object> chargeEmploye = new HashMap<>();
            chargeEmploye.put("employe", employe);

            List<Map<String, Object>> chargesParJour = new ArrayList<>();
            LocalDate dateActuelle = debut;

            while (!dateActuelle.isAfter(fin)) {
                Integer chargeMinutes = getChargeEmployeParJour(employe.getId(), dateActuelle);
                Integer capaciteMinutes = employe.getHeuresTravailParJour() * 60;

                Map<String, Object> chargeJour = new HashMap<>();
                chargeJour.put("date", dateActuelle);
                chargeJour.put("chargeMinutes", chargeMinutes);
                chargeJour.put("capaciteMinutes", capaciteMinutes);
                chargeJour.put("pourcentage", capaciteMinutes > 0 ? (chargeMinutes * 100.0 / capaciteMinutes) : 0);

                chargesParJour.add(chargeJour);
                dateActuelle = dateActuelle.plusDays(1);
            }

            chargeEmploye.put("chargesParJour", chargesParJour);
            chargesEmployes.add(chargeEmploye);
        }

        resultat.put("employes", chargesEmployes);
        resultat.put("planifications", planifications);

        return resultat;
    }
}