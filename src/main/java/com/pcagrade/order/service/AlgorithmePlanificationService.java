package com.pcagrade.order.service;

import com.pcagrade.order.entity.Order;
import com.pcagrade.order.entity.Employe;
import com.pcagrade.order.entity.Planification;
import com.pcagrade.order.entity.StatutCommande;
import com.pcagrade.order.ulid.Ulid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AlgorithmePlanificationService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private EmployeService employeService;

    @Autowired
    private PlanificationService planificationService;

    /**
     * Planifie automatiquement toutes les commandes en attente
     * @return Rapport de planification
     */
    public RapportPlanification planifierCommandes() {
        List<Order> ordersATraiter = orderService.getOrdersATraiter();
        List<Employe> employesActifs = employeService.getEmployesActifs();

        if (employesActifs.isEmpty()) {
            throw new RuntimeException("Aucun employé actif disponible pour la planification");
        }

        // Trier les commandes par priorité et date limite
        ordersATraiter.sort((c1, c2) -> {
            int prioriteComp = c2.getPrioriteString().compareTo(c1.getPrioriteString());
            if (prioriteComp != 0) return prioriteComp;
            return c1.getDateLimite().compareTo(c2.getDateLimite());
        });

        RapportPlanification rapport = new RapportPlanification();
        LocalDate dateDebut = LocalDate.now();

        // Créer une carte des disponibilités pour chaque employé
        Map<Ulid, Map<LocalDate, Integer>> disponibilites = initialiserDisponibilites(employesActifs, dateDebut);

        for (Order order : ordersATraiter) {
            if (order.getStatus() == 0) { // 0 = En attente
                try {
                    PlanificationResult resultat = planifierCommande(order, employesActifs, disponibilites);
                    if (resultat.isSuccess()) {
                        // Mettre à jour le statut de la commande
                        orderService.marquerCommePlanifie(order.getId());
                        rapport.addCommandePlanifiee(order, resultat.getPlanifications());
                    } else {
                        rapport.addCommandeNonPlanifiee(order, resultat.getRaison());
                    }
                } catch (Exception e) {
                    rapport.addCommandeNonPlanifiee(order, "Erreur: " + e.getMessage());
                }
            }
        }

        return rapport;
    }

    /**
     * Planifie une commande spécifique
     */
    private PlanificationResult planifierCommande(Order order, List<Employe> employes,
                                                  Map<Ulid, Map<LocalDate, Integer>> disponibilites) {

        int tempsNecessaire = order.getTempsEstimeMinutes();
        LocalDate dateLimite = order.getDateLimite().toLocalDate();
        LocalDate dateDebut = LocalDate.now();

        // ✅ CORRECTION : Si la date limite est dans le passé, la mettre dans le futur
        if (dateLimite.isBefore(dateDebut)) {
            dateLimite = dateDebut.plusMonths(1); // Planifier dans le mois qui vient
            System.out.println("🔧 Date limite ajustée pour commande " + order.getNumCommande() + ": " + dateLimite);
        }

        // Stratégie 1: Essayer de planifier sur un seul employé
        PlanificationResult resultatUnSeul = essayerPlanificationUnSeulEmploye(
                order, employes, disponibilites, dateDebut, dateLimite, tempsNecessaire);

        if (resultatUnSeul.isSuccess()) {
            return resultatUnSeul;
        }

        // Stratégie 2: Diviser sur plusieurs employés
        return essayerPlanificationMultipleEmployes(
                order, employes, disponibilites, dateDebut, dateLimite, tempsNecessaire);
    }

    /**
     * Essaie de planifier toute la commande sur un seul employé
     */
    private PlanificationResult essayerPlanificationUnSeulEmploye(Order commande, List<Employe> employes,
                                                                  Map<Ulid, Map<LocalDate, Integer>> disponibilites, LocalDate dateDebut,
                                                                  LocalDate dateLimite, int tempsNecessaire) {

        // Trier les employés par charge de travail actuelle (moins chargé en premier)
        List<Employe> employesTries = employes.stream()
                .sorted((e1, e2) -> {
                    int charge1 = calculerChargeTotale(e1.getId(), disponibilites, dateDebut, dateLimite);
                    int charge2 = calculerChargeTotale(e2.getId(), disponibilites, dateDebut, dateLimite);
                    return Integer.compare(charge1, charge2);
                })
                .collect(Collectors.toList());

        for (Employe employe : employesTries) {
            List<CreneauDisponible> creneaux = trouverCreneauxDisponibles(
                    employe, disponibilites.get(employe.getId()), dateDebut, dateLimite, tempsNecessaire);

            if (!creneaux.isEmpty()) {
                List<Planification> planifications = new ArrayList<>();
                int tempsRestant = tempsNecessaire;

                for (CreneauDisponible creneau : creneaux) {
                    if (tempsRestant <= 0) break;

                    int dureeSlot = Math.min(tempsRestant, creneau.getDureeDisponible());

                    Planification planification = new Planification(
                            commande.getId(),         // ✅ ULID de la commande/order
                            employe.getId(),          // ✅ ULID de l'employé
                            creneau.getDate(),
                            creneau.getHeureDebutAsLocalTime(),
                            dureeSlot
                    );
                    planifications.add(planificationService.creerPlanification(planification));

                    // Mettre à jour les disponibilités
                    mettreAJourDisponibilite(employe.getId(), creneau.getDate(), dureeSlot, disponibilites);

                    tempsRestant -= dureeSlot;
                }

                if (tempsRestant <= 0) {
                    return new PlanificationResult(true, planifications, null);
                }
            }
        }

        return new PlanificationResult(false, null, "Impossible de planifier sur un seul employé");
    }

    /**
     * Essaie de diviser la commande sur plusieurs employés
     */
    private PlanificationResult essayerPlanificationMultipleEmployes(Order order, List<Employe> employes,
                                                                     Map<Ulid, Map<LocalDate, Integer>> disponibilites, LocalDate dateDebut,
                                                                     LocalDate dateLimite, int tempsNecessaire) {

        List<Planification> planifications = new ArrayList<>();
        int tempsRestant = tempsNecessaire;

        // Créer une liste de tous les créneaux disponibles de tous les employés
        List<CreneauEmploye> tousCreneaux = new ArrayList<>();

        for (Employe employe : employes) {
            List<CreneauDisponible> creneauxEmploye = trouverCreneauxDisponibles(
                    employe, disponibilites.get(employe.getId()), dateDebut, dateLimite, 60); // Créneaux d'au moins 1h

            for (CreneauDisponible creneau : creneauxEmploye) {
                tousCreneaux.add(new CreneauEmploye(employe, creneau));
            }
        }

        // Trier par date puis par priorité d'employé (moins chargé en premier)
        tousCreneaux.sort((c1, c2) -> {
            int dateComp = c1.getCreneau().getDate().compareTo(c2.getCreneau().getDate());
            if (dateComp != 0) return dateComp;

            int charge1 = calculerChargeTotale(c1.getEmploye().getId(), disponibilites, dateDebut, dateLimite);
            int charge2 = calculerChargeTotale(c2.getEmploye().getId(), disponibilites, dateDebut, dateLimite);
            return Integer.compare(charge1, charge2);
        });

        for (CreneauEmploye creneauEmploye : tousCreneaux) {
            if (tempsRestant <= 0) break;

            int dureeSlot = Math.min(tempsRestant, creneauEmploye.getCreneau().getDureeDisponible());

            Planification planification = new Planification(
                    creneauEmploye.getEmploye().getId(),  // ✅ ULID
                    order.getId(),                        // ✅ ULID
                    creneauEmploye.getCreneau().getDate(),
                    creneauEmploye.getCreneau().getHeureDebutAsLocalTime(),
                    dureeSlot
            );

            planifications.add(planificationService.creerPlanification(planification));

            // Mettre à jour les disponibilités
            mettreAJourDisponibilite(
                    creneauEmploye.getEmploye().getId(),
                    creneauEmploye.getCreneau().getDate(),
                    dureeSlot,
                    disponibilites
            );

            tempsRestant -= dureeSlot;
        }

        if (tempsRestant <= 0) {
            return new PlanificationResult(true, planifications, null);
        } else {
            // Supprimer les planifications créées si échec
            planifications.forEach(p -> planificationService.planificationRepository.delete(p));
            return new PlanificationResult(false, null,
                    "Impossible de planifier complètement la commande (temps restant: " + tempsRestant + " min)");
        }
    }

    /**
     * Initialise les disponibilités de tous les employés
     */
    private Map<Ulid, Map<LocalDate, Integer>> initialiserDisponibilites(List<Employe> employes, LocalDate dateDebut) {
        Map<Ulid, Map<LocalDate, Integer>> disponibilites = new HashMap<>();
        LocalDate dateFin = dateDebut.plusMonths(2); // Planifier sur 2 mois

        for (Employe employe : employes) {
            Map<LocalDate, Integer> disponibiliteEmploye = new HashMap<>();

            LocalDate dateActuelle = dateDebut;
            while (!dateActuelle.isAfter(dateFin)) {
                // Ne pas planifier les weekends
                if (dateActuelle.getDayOfWeek() != DayOfWeek.SATURDAY &&
                        dateActuelle.getDayOfWeek() != DayOfWeek.SUNDAY) {

                    // Calculer la disponibilité restante pour cette date
                    int capaciteJournaliere = employe.getHeuresTravailParJour() * 60; // en minutes
                    int chargeActuelle = planificationService.getChargeEmployeParJour(employe.getId(), dateActuelle);
                    int disponibiliteRestante = Math.max(0, capaciteJournaliere - chargeActuelle);

                    disponibiliteEmploye.put(dateActuelle, disponibiliteRestante);
                }
                dateActuelle = dateActuelle.plusDays(1);
            }

            disponibilites.put(employe.getId(), disponibiliteEmploye);
        }

        return disponibilites;
    }

    /**
     * Trouve les créneaux disponibles pour un employé
     */
    private List<CreneauDisponible> trouverCreneauxDisponibles(Employe employe,
                                                               Map<LocalDate, Integer> disponibiliteEmploye, LocalDate dateDebut,
                                                               LocalDate dateLimite, int dureeMinimale) {

        List<CreneauDisponible> creneaux = new ArrayList<>();

        for (Map.Entry<LocalDate, Integer> entry : disponibiliteEmploye.entrySet()) {
            LocalDate date = entry.getKey();
            Integer disponibilite = entry.getValue();

            if (date.isAfter(dateLimite)) continue;
            if (date.isBefore(dateDebut)) continue;
            if (disponibilite < dureeMinimale) continue;

            // Pour simplifier, on commence toujours à 9h
            creneaux.add(new CreneauDisponible(date, 9, disponibilite));
        }

        // Trier par date
        creneaux.sort(Comparator.comparing(CreneauDisponible::getDate));

        return creneaux;
    }

    /**
     * Met à jour la disponibilité d'un employé
     */
    private void mettreAJourDisponibilite(Ulid employeId, LocalDate date, int dureeUtilisee,
                                          Map<Ulid, Map<LocalDate, Integer>> disponibilites) {
        Map<LocalDate, Integer> disponibiliteEmploye = disponibilites.get(employeId);
        if (disponibiliteEmploye != null && disponibiliteEmploye.containsKey(date)) {
            int disponibiliteActuelle = disponibiliteEmploye.get(date);
            disponibiliteEmploye.put(date, Math.max(0, disponibiliteActuelle - dureeUtilisee));
        }
    }

    /**
     * Calcule la charge totale d'un employé sur une période
     */
    private int calculerChargeTotale(Ulid employeId, Map<Ulid, Map<LocalDate, Integer>> disponibilites,
                                     LocalDate debut, LocalDate fin) {
        Map<LocalDate, Integer> disponibiliteEmploye = disponibilites.get(employeId);
        if (disponibiliteEmploye == null) return 0;

        return disponibiliteEmploye.entrySet().stream()
                .filter(entry -> !entry.getKey().isBefore(debut) && !entry.getKey().isAfter(fin))
                .mapToInt(Map.Entry::getValue)
                .sum();
    }

    // ============= CLASSES INTERNES =============

    public static class RapportPlanification {
        private List<CommandePlanifiee> commandesPlanifiees = new ArrayList<>();
        private List<CommandeNonPlanifiee> commandesNonPlanifiees = new ArrayList<>();
        private LocalDate dateGeneration = LocalDate.now();

        public void addCommandePlanifiee(Order order, List<Planification> planifications) {
            commandesPlanifiees.add(new CommandePlanifiee(order, planifications));
        }

        public void addCommandeNonPlanifiee(Order order, String raison) {
            commandesNonPlanifiees.add(new CommandeNonPlanifiee(order, raison));
        }

        // Getters
        public List<CommandePlanifiee> getCommandesPlanifiees() { return commandesPlanifiees; }
        public List<CommandeNonPlanifiee> getCommandesNonPlanifiees() { return commandesNonPlanifiees; }
        public LocalDate getDateGeneration() { return dateGeneration; }
        public int getNombreTotalCommandes() { return commandesPlanifiees.size() + commandesNonPlanifiees.size(); }
        public int getNombreCommandesPlanifiees() { return commandesPlanifiees.size(); }
        public int getNombreCommandesNonPlanifiees() { return commandesNonPlanifiees.size(); }
    }

    public static class CommandePlanifiee {
        private Order commande;
        private List<Planification> planifications;

        public CommandePlanifiee(Order commande, List<Planification> planifications) {
            this.commande = commande;
            this.planifications = planifications;
        }

        public Order getCommande() { return commande; }
        public List<Planification> getPlanifications() { return planifications; }
    }

    public static class CommandeNonPlanifiee {
        private Order commande;
        private String raison;

        public CommandeNonPlanifiee(Order commande, String raison) {
            this.commande = commande;
            this.raison = raison;
        }

        public Order getCommande() { return commande; }
        public String getRaison() { return raison; }
    }

    private static class PlanificationResult {
        private boolean success;
        private List<Planification> planifications;
        private String raison;

        public PlanificationResult(boolean success, List<Planification> planifications, String raison) {
            this.success = success;
            this.planifications = planifications;
            this.raison = raison;
        }

        public boolean isSuccess() { return success; }
        public List<Planification> getPlanifications() { return planifications; }
        public String getRaison() { return raison; }
    }

    private static class CreneauDisponible {
        private LocalDate date;
        private int heureDebut; // Heures (ex: 9 pour 9h00)
        private int dureeDisponible;

        public CreneauDisponible(LocalDate date, int heureDebut, int dureeDisponible) {
            this.date = date;
            this.heureDebut = heureDebut;
            this.dureeDisponible = dureeDisponible;
        }

        public LocalDate getDate() { return date; }

        // Retourner l'int tel quel
        public int getHeureDebut() { return heureDebut; }

        // Méthode utilitaire pour convertir en LocalTime
        public LocalTime getHeureDebutAsLocalTime() {
            return LocalTime.of(heureDebut, 0); // heureDebut = heures, 0 = minutes
        }

        public int getDureeDisponible() { return dureeDisponible; }
    }

    private static class CreneauEmploye {
        private Employe employe;
        private CreneauDisponible creneau;

        public CreneauEmploye(Employe employe, CreneauDisponible creneau) {
            this.employe = employe;
            this.creneau = creneau;
        }

        public Employe getEmploye() { return employe; }
        public CreneauDisponible getCreneau() { return creneau; }
    }
}
