<template>
  <div class="space-y-6">
    <!-- En-tête -->
    <div class="flex justify-between items-center">
      <h2 class="text-2xl font-bold text-gray-900">👥 Gestion des Employés</h2>
      <button
        @click="showCreateModal = true"
        class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
      >
        ➕ Nouvel Employé
      </button>
    </div>

    <!-- Statistiques rapides -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <StatCard
        title="Total Employés"
        :value="employes.length"
        icon="👥"
        color="blue"
      />
      <StatCard
        title="Employés Actifs"
        :value="employesActifs.length"
        icon="✅"
        color="green"
      />
      <StatCard
        title="Employés Inactifs"
        :value="employes.length - employesActifs.length"
        icon="⏸️"
        color="gray"
      />
    </div>

    <!-- Tableau des employés -->
    <div class="bg-white rounded-lg shadow overflow-hidden">
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Employé
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Email
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Heures/Jour
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Statut
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Date Création
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Actions
            </th>
          </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="employe in employes" :key="employe.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="flex items-center">
                <div class="flex-shrink-0 h-10 w-10">
                  <div class="h-10 w-10 rounded-full bg-gray-300 flex items-center justify-center">
                      <span class="text-sm font-medium text-gray-700">
                        {{ employe.prenom?.charAt(0) || '' }}{{ employe.nom?.charAt(0) || '' }}
                      </span>
                  </div>
                </div>
                <div class="ml-4">
                  <div class="text-sm font-medium text-gray-900">
                    {{ employe.prenom }} {{ employe.nom }}
                  </div>
                </div>
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
              {{ employe.email }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
              {{ employe.heuresTravailParJour }}h
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
                <span :class="[
                  'inline-flex px-2 py-1 text-xs font-semibold rounded-full',
                  employe.actif
                    ? 'bg-green-100 text-green-800'
                    : 'bg-red-100 text-red-800'
                ]">
                  {{ employe.actif ? 'Actif' : 'Inactif' }}
                </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
              {{ formatDate(employe.dateCreation) }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
              <button
                @click="modifierEmploye(employe)"
                class="text-indigo-600 hover:text-indigo-900"
              >
                ✏️ Modifier
              </button>
              <button
                v-if="employe.actif"
                @click="desactiverEmploye(employe.id!)"
                class="text-red-600 hover:text-red-900"
              >
                ⏸️ Désactiver
              </button>
              <button
                @click="voirPlannings(employe)"
                class="text-blue-600 hover:text-blue-900"
              >
                📅 Planning
              </button>
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <div v-if="loading" class="text-center py-8">
        <div class="text-gray-500">🔄 Chargement...</div>
      </div>

      <div v-if="!loading && employes.length === 0" class="text-center py-8">
        <div class="text-gray-500">Aucun employé trouvé</div>
      </div>
    </div>

    <!-- Modal Création/Modification Employé -->
    <div v-if="showCreateModal || showEditModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 w-full max-w-md">
        <h3 class="text-lg font-semibold mb-4">
          {{ showCreateModal ? '➕ Nouvel Employé' : '✏️ Modifier Employé' }}
        </h3>
        <form @submit.prevent="showCreateModal ? creerEmploye() : sauvegarderModification()">
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Prénom</label>
              <input
                v-model="formEmploye.prenom"
                type="text"
                required
                class="w-full border border-gray-300 rounded-md px-3 py-2"
                placeholder="Jean"
              >
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Nom</label>
              <input
                v-model="formEmploye.nom"
                type="text"
                required
                class="w-full border border-gray-300 rounded-md px-3 py-2"
                placeholder="Dupont"
              >
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
              <input
                v-model="formEmploye.email"
                type="email"
                required
                class="w-full border border-gray-300 rounded-md px-3 py-2"
                placeholder="jean.dupont@example.com"
              >
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Heures de travail par jour</label>
              <input
                v-model.number="formEmploye.heuresTravailParJour"
                type="number"
                required
                min="1"
                max="12"
                class="w-full border border-gray-300 rounded-md px-3 py-2"
                placeholder="8"
              >
            </div>
            <div v-if="showEditModal">
              <label class="flex items-center">
                <input
                  v-model="formEmploye.actif"
                  type="checkbox"
                  class="rounded border-gray-300 text-blue-600 shadow-sm focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50"
                >
                <span class="ml-2 text-sm text-gray-700">Employé actif</span>
              </label>
            </div>
          </div>
          <div class="flex justify-end space-x-3 mt-6">
            <button
              type="button"
              @click="fermerModales"
              class="bg-gray-300 text-gray-700 px-4 py-2 rounded-lg hover:bg-gray-400 transition-colors"
            >
              Annuler
            </button>
            <button
              type="submit"
              :disabled="loading"
              class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors"
            >
              {{ showCreateModal ? 'Créer' : 'Sauvegarder' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal Planning Employé -->
    <div v-if="showPlanningModal && selectedEmploye" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 w-full max-w-4xl max-h-[80vh] overflow-y-auto">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-lg font-semibold">
            📅 Planning de {{ selectedEmploye.prenom }} {{ selectedEmploye.nom }}
          </h3>
          <button
            @click="showPlanningModal = false"
            class="text-gray-400 hover:text-gray-600"
          >
            ✕
          </button>
        </div>

        <!-- Sélection de période -->
        <div class="flex space-x-4 mb-6">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Du</label>
            <input
              v-model="planningPeriode.debut"
              type="date"
              class="border border-gray-300 rounded-md px-3 py-2"
            >
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Au</label>
            <input
              v-model="planningPeriode.fin"
              type="date"
              class="border border-gray-300 rounded-md px-3 py-2"
            >
          </div>
          <button
            @click="chargerPlanningEmploye"
            class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors mt-6"
          >
            🔍 Charger
          </button>
        </div>

        <!-- Planning -->
        <div v-if="planningsEmploye.length > 0" class="space-y-4">
          <div
            v-for="planning in planningsEmploye"
            :key="planning.id"
            class="bg-gray-50 p-4 rounded-lg border"
          >
            <div class="flex justify-between items-start">
              <div>
                <div class="font-medium">{{ planning.commande.numeroCommande }}</div>
                <div class="text-sm text-gray-600">
                  📅 {{ formatDate(planning.datePlanifiee) }} à {{ planning.heureDebut }}h
                </div>
                <div class="text-sm text-gray-600">
                  ⏱️ {{ planning.dureeMinutes }} minutes
                </div>
                <div class="text-sm text-gray-600">
                  🃏 {{ planning.commande.nombreCartes }} cartes
                </div>
              </div>
              <span :class="[
                'inline-flex px-2 py-1 text-xs font-semibold rounded-full',
                planning.terminee
                  ? 'bg-green-100 text-green-800'
                  : 'bg-yellow-100 text-yellow-800'
              ]">
                {{ planning.terminee ? '✅ Terminé' : '⏳ En cours' }}
              </span>
            </div>
          </div>
        </div>

        <div v-else-if="!loadingPlanning" class="text-center py-8">
          <div class="text-gray-500">Aucune planification trouvée pour cette période</div>
        </div>

        <div v-if="loadingPlanning" class="text-center py-8">
          <div class="text-gray-500">🔄 Chargement du planning...</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, inject } from 'vue'
import { apiService, type Employe, type Planification } from '../services/api'
import StatCard from './StatCard.vue'

// État local
const employes = ref<Employe[]>([])
const planningsEmploye = ref<Planification[]>([])
const loading = ref(false)
const loadingPlanning = ref(false)
const showCreateModal = ref(false)
const showEditModal = ref(false)
const showPlanningModal = ref(false)
const selectedEmploye = ref<Employe | null>(null)

// Formulaire employé
const formEmploye = ref({
  id: undefined as number | undefined,
  prenom: '',
  nom: '',
  email: '',
  heuresTravailParJour: 8,
  actif: true
})

// Période planning
const planningPeriode = ref({
  debut: new Date().toISOString().split('T')[0],
  fin: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
})

// Injection de la fonction de notification
const showNotification = inject('showNotification') as (message: string, type?: 'success' | 'error') => void

// Employés actifs
const employesActifs = computed(() => {
  return employes.value.filter(e => e.actif)
})

// Chargement des employés
const loadEmployes = async () => {
  loading.value = true
  try {
    employes.value = await apiService.getEmployes()
  } catch (error) {
    console.error('Erreur lors du chargement des employés:', error)
    showNotification?.('Erreur lors du chargement des employés', 'error')
    // Données de test en cas d'erreur
    employes.value = [
      {
        id: 1,
        prenom: 'Jean',
        nom: 'Dupont',
        email: 'jean.dupont@example.com',
        heuresTravailParJour: 8,
        actif: true,
        dateCreation: new Date().toISOString()
      },
      {
        id: 2,
        prenom: 'Marie',
        nom: 'Martin',
        email: 'marie.martin@example.com',
        heuresTravailParJour: 7,
        actif: true,
        dateCreation: new Date().toISOString()
      }
    ]
  } finally {
    loading.value = false
  }
}

// Création d'un employé
const creerEmploye = async () => {
  loading.value = true
  try {
    await apiService.creerEmploye({
      prenom: formEmploye.value.prenom,
      nom: formEmploye.value.nom,
      email: formEmploye.value.email,
      heuresTravailParJour: formEmploye.value.heuresTravailParJour,
      actif: true
    })

    showNotification?.('Employé créé avec succès')
    fermerModales()
    await loadEmployes()
  } catch (error) {
    console.error('Erreur lors de la création de l\'employé:', error)
    showNotification?.('Erreur lors de la création de l\'employé', 'error')
  } finally {
    loading.value = false
  }
}

// Modification d'un employé
const modifierEmploye = (employe: Employe) => {
  formEmploye.value = {
    id: employe.id,
    prenom: employe.prenom,
    nom: employe.nom,
    email: employe.email,
    heuresTravailParJour: employe.heuresTravailParJour,
    actif: employe.actif
  }
  showEditModal.value = true
}

// Sauvegarde modification
const sauvegarderModification = async () => {
  if (!formEmploye.value.id) return

  loading.value = true
  try {
    await apiService.modifierEmploye(formEmploye.value.id, {
      id: formEmploye.value.id,
      prenom: formEmploye.value.prenom,
      nom: formEmploye.value.nom,
      email: formEmploye.value.email,
      heuresTravailParJour: formEmploye.value.heuresTravailParJour,
      actif: formEmploye.value.actif
    })

    showNotification?.('Employé modifié avec succès')
    fermerModales()
    await loadEmployes()
  } catch (error) {
    console.error('Erreur lors de la modification de l\'employé:', error)
    showNotification?.('Erreur lors de la modification de l\'employé', 'error')
  } finally {
    loading.value = false
  }
}

// Désactivation d'un employé
const desactiverEmploye = async (id: number) => {
  if (!confirm('Êtes-vous sûr de vouloir désactiver cet employé ?')) return

  try {
    await apiService.desactiverEmploye(id)
    showNotification?.('Employé désactivé')
    await loadEmployes()
  } catch (error) {
    console.error('Erreur lors de la désactivation:', error)
    showNotification?.('Erreur lors de la désactivation de l\'employé', 'error')
  }
}

// Voir planning
const voirPlannings = (employe: Employe) => {
  selectedEmploye.value = employe
  showPlanningModal.value = true
  chargerPlanningEmploye()
}

// Charger planning employé
const chargerPlanningEmploye = async () => {
  if (!selectedEmploye.value?.id) return

  loadingPlanning.value = true
  try {
    planningsEmploye.value = await apiService.getPlanificationsByEmploye(
      selectedEmploye.value.id,
      planningPeriode.value.debut,
      planningPeriode.value.fin
    )
  } catch (error) {
    console.error('Erreur lors du chargement du planning:', error)
    showNotification?.('Erreur lors du chargement du planning', 'error')
    planningsEmploye.value = [] // Tableau vide en cas d'erreur
  } finally {
    loadingPlanning.value = false
  }
}

// Fermer modales
const fermerModales = () => {
  showCreateModal.value = false
  showEditModal.value = false
  resetForm()
}

// Reset formulaire
const resetForm = () => {
  formEmploye.value = {
    id: undefined,
    prenom: '',
    nom: '',
    email: '',
    heuresTravailParJour: 8,
    actif: true
  }
}

// Formatage date
const formatDate = (dateString?: string) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('fr-FR')
}

// Lifecycle
onMounted(() => {
  loadEmployes()
})
</script>
