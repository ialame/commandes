# 📦 Gestion des Commandes Pokemon

Application web complète pour la gestion des commandes de cartes Pokemon avec planification automatique des tâches et répartition de charge entre employés.

## 🏗️ Architecture

- **Backend**: Spring Boot 3.2 + MariaDB + JPA
- **Frontend**: Vue.js 3.5 + TypeScript + Tailwind CSS
- **API**: REST avec CORS activé

## 🚀 Installation et Démarrage

### Prérequis
- Java 21+
- Node.js 18+
- MariaDB 10.5+
- Maven 3.8+

### Backend (Spring Boot)

1. **Configuration de la base de données**
   ```sql
   CREATE DATABASE commandes_db;
   CREATE USER 'root'@'localhost' IDENTIFIED BY 'password';
   GRANT ALL PRIVILEGES ON commandes_db.* TO 'root'@'localhost';
   ```

2. **Lancement du backend**
   ```bash
   cd backend
   mvn clean install
   mvn spring-boot:run
   ```

   Le serveur démarre sur `http://localhost:8080`

### Frontend (Vue.js)

1. **Installation des dépendances**
   ```bash
   cd frontend
   npm install
   ```

2. **Lancement en développement**
   ```bash
   npm run dev
   ```

   L'application démarre sur `http://localhost:3000`

## 📊 Fonctionnalités

### ✅ Gestion des Commandes
- Création de commandes avec calcul automatique de priorité
- Suivi des statuts (En attente, Planifiée, En cours, Terminée)
- Calcul automatique des délais selon le prix :
    - **Haute priorité** (≥1000€) : 1 semaine
    - **Moyenne priorité** (≥500€) : 2 semaines
    - **Basse priorité** (<500€) : 4 semaines
- Estimation du temps de traitement (5 min/carte)

### 👥 Gestion des Employés
- Ajout/modification/désactivation d'employés
- Configuration des heures de travail par employé
- Visualisation du planning individuel
- Calcul de la charge de travail

### 🤖 Planification Automatique
- Algorithme intelligent de répartition des tâches
- Respect des priorités et des délais
- Optimisation de la charge de travail
- Support de la planification multi-employés
- Évitement des weekends

### 📈 Dashboard et Reporting
- Statistiques en temps réel
- Graphiques de performance
- Suivi des commandes en retard
- Visualisation de la charge par employé

## 🔧 Configuration

### Backend
Modifier `src/main/resources/application.properties` :
```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/commandes_db
spring.datasource.username=votre_utilisateur
spring.datasource.password=votre_mot_de_passe
```

### Frontend
Modifier `services/api.ts` si nécessaire :
```typescript
const API_BASE_URL = 'http://localhost:8080/api'
```

## 📋 API Endpoints

### Commandes
- `GET /api/commandes` - Liste des commandes
- `POST /api/commandes` - Créer une commande
- `PUT /api/commandes/{id}/commencer` - Commencer une commande
- `PUT /api/commandes/{id}/terminer` - Terminer une commande
- `GET /api/commandes/statistiques` - Statistiques

### Employés
- `GET /api/employes` - Liste des employés
- `POST /api/employes` - Créer un employé
- `PUT /api/employes/{id}` - Modifier un employé
- `DELETE /api/employes/{id}` - Désactiver un employé

### Planifications
- `GET /api/planifications/periode` - Planifications par période
- `POST /api/planifications/planifier-automatique` - Planification auto
- `PUT /api/planifications/{id}/terminer` - Terminer une planification

### Dashboard
- `GET /api/dashboard/stats` - Statistiques complètes
- `GET /api/dashboard/overview` - Vue d'ensemble

## 🎯 Algorithme de Planification

L'algorithme suit cette logique :

1. **Tri des commandes** par priorité puis date limite
2. **Tentative de planification sur un seul employé** (préférable)
3. **Répartition multi-employés** si nécessaire
4. **Respect des capacités** (8h/jour par défaut)
5. **Évitement des weekends**
6. **Optimisation de la charge** (employé le moins chargé en premier)

## 🏷️ Structure du Projet

```
├── backend/
│   ├── src/main/java/com/pcagrade/order/
│   │   ├── entity/          # Entités JPA
│   │   ├── repository/      # Repositories
│   │   ├── service/         # Logique métier
│   │   ├── controller/      # Contrôleurs REST
│   │   └── dto/            # DTOs
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── components/      # Composants Vue
│   │   ├── services/        # Services API
│   │   ├── App.vue         # Composant principal
│   │   └── main.ts         # Point d'entrée
│   ├── package.json
│   └── vite.config.ts
│
└── README.md
```

## 🧪 Tests

Pour tester l'application :

1. **Créer des employés** via l'interface
2. **Ajouter des commandes** avec différents prix
3. **Lancer la planification automatique**
4. **Vérifier la répartition** dans l'onglet Planification
5. **Marquer des tâches comme terminées**

## 🔮 Améliorations Futures

- [ ] Authentification et autorisation
- [ ] Notifications en temps réel (WebSocket)
- [ ] Export PDF des plannings
- [ ] API mobile
- [ ] Système de commentaires sur les commandes
- [ ] Historique des modifications
- [ ] Statistiques avancées avec graphiques interactifs

## 📞 Support

Pour toute question ou problème, vérifiez :
1. La connexion à la base de données
2. Les logs du serveur Spring Boot
3. La console du navigateur pour les erreurs frontend
4. Que les ports 8080 et 3000 sont disponibles

---

**Développé avec ❤️ pour la gestion efficace des commandes Pokemon**
