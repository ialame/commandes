<template>
  <div class="space-y-6">
    <!-- En-tête -->
    <div class="flex justify-between items-center">
      <h2 class="text-2xl font-bold text-gray-900">📋 Gestion des Commandes</h2>
      <button
        @click="showCreateModal = true"
        class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
      >
        ➕ Nouvelle Commande
      </button>
    </div>

    <!-- Filtres -->
    <div class="bg-white p-4 rounded-lg shadow-sm">
      <div class="flex flex-wrap gap-4 items-center">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Statut</label>
          <select
            v-model="filters.statut"
            class="border border-gray-300 rounded-md px-3 py-2 text-sm"
          >
            <option value="">Tous</option>
            <option value="EN_ATTENTE">En Attente</option>
            <option value="PLANIFIEE">Planifiée</option>
            <option value="EN_COURS">En Cours</option>
            <option value="TERMINEE">Terminée</option>
            <option value="ANNULEE">Annulée</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Priorité</label>
          <select
            v-model="filters.priorite"
            class="border border-gray-300 rounded-md px-3 py-2 text-sm"
          >
            <option value="">Toutes</option>
            <option value="HAUTE">Haute</option>
            <option value="MOYENNE">Moyenne</option>
            <option value="BASSE">Basse</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Recherche</label>
          <input
            v-model="filters.search"
            type="text"
            placeholder="Numéro commande..."
            class="border border-gray-300 rounded-md px-3 py-2 text-sm"
          >
        </div>
        <button
          @click="loadCommandes"
          class="bg-gray-600 text-white px-4 py-2 rounded-lg hover:bg-gray-700 transition-colors mt-6"
        >
          🔍 Filtrer
        </button>
      </div>
    </div>

    <!-- Tableau des commandes -->
    <div class="bg-white rounded-lg shadow overflow-hidden">
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Commande
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Cartes
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Prix
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Priorité
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Statut
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Date Limite
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Actions
            </th>
          </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="commande in filteredCommandes" :key="commande.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="text-sm font-medium text-gray-900">{{ commande.numeroCommande }}</div>
              <div class="text-sm text-gray-500">{{ formatDate(commande.dateCreation) }}</div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
              {{ commande.nombreCartes }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
              {{ formatPrice(commande.prixTotal) }}€
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
                <span :class="[
                  'inline-flex px-2 py-1 text-xs font-semibold rounded-full',
                  getPriorityColor(commande.priorite)
                ]">
                  {{ getPriorityLabel(commande.priorite) }}
                </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
                <span :class="[
                  'inline-flex px-2 py-1 text-xs font-semibold rounded-full',
                  getStatusColor(commande.statut)
                ]">
                  {{ getStatusLabel(commande.statut) }}
                </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
              <div :class="[
                  isOverdue(commande.dateLimite) ? 'text-red-600 font-semibold' : 'text-gray-900'
                ]">
                {{ formatDate(commande.dateLimite) }}
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
              <button
                v-if="commande.statut === 'PLANIFIEE'"
                @click="commencerCommande(commande.id!)"
                class="text-green-600 hover:text-green-900"
              >
                ▶️ Commencer
              </button>
              <button
                v-if="commande.statut === 'EN_COURS'"
                @click="terminerCommande(commande.id!)"
                class="text-blue-600 hover:text-blue-900"
              >
                ✅ Terminer
              </button>
              <button
                @click="voirDetails(commande)"
                class="text-indigo-600 hover:text-indigo-900"
              >
                👁️ Voir
              </button>
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <div v-if="loading" class="text-center py-8">
        <div class="text-gray-500">🔄 Chargement...</div>
      </div>

      <div v-if="!loading && filteredCommandes.length === 0" class="text-center py-8">
        <div class="text-gray-500">Aucune commande trouvée</div>
      </div>
    </div>

    <!-- Modal Création Commande -->
    <div v-if="showCreateModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 w-full max-w-md">
        <h3 class="text-lg font-semibold mb-4">➕ Nouvelle Commande</h3>
        <form @submit.prevent="creerCommande">
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Numéro de commande</label>
              <input
                v-model="newCommande.numeroCommande"
                type="text"
                required
                class="w-full border border-gray-300 rounded-md px-3 py-2"
                placeholder="CMD-001"
              >
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Nombre de cartes</label>
              <input
                v-model.number="newCommande.nombreCartes"
                type="number"
                required
                min="1"
                class="w-full border border-gray-300 rounded-md px-3 py-2"
                placeholder="20"
              >
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Prix total (€)</label>
              <input
                v-model.number="newCommande.prixTotal"
                type="number"
                required
                min="0"
                step="0.01"
                class="w-full border border-gray-300 rounded-md px-3 py-2"
                placeholder="150.00"
              >
            </div>
          </div>
          <div class="flex justify-end space-x-3 mt-6">
            <button
              type="button"
              @click="showCreateModal = false"
              class="bg-gray-300 text-gray-700 px-4 py-2 rounded-lg hover:bg-gray-400 transition-colors"
            >
              Annuler
            </button>
            <button
              type="submit"
              :disabled="loading"
              class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors"
            >
              Créer
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal Détails Commande -->
    <div v-if="showDetailsModal && selectedCommande" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 w-full max-w-lg">
        <h3 class="text-lg font-semibold mb-4">📋 Détails de la commande</h3>
        <div class="space-y-3">
          <div><strong>Numéro:</strong> {{ selectedCommande.numeroCommande }}</div>
          <div><strong>Cartes:</strong> {{ selectedCommande.nombreCartes }}</div>
          <div><strong>Prix:</strong> {{ formatPrice(selectedCommande.prixTotal) }}€</div>
          <div><strong>Priorité:</strong> {{ getPriorityLabel(selectedCommande.priorite) }}</div>
          <div><strong>Statut:</strong> {{ getStatusLabel(selectedCommande.statut) }}</div>
          <div><strong>Date création:</strong> {{ formatDate(selectedCommande.dateCreation) }}</div>
          <div><strong>Date limite:</strong> {{ formatDate(selectedCommande.dateLimite) }}</div>
          <div><strong>Temps estimé:</strong> {{ selectedCommande.tempsEstimeMinutes }} minutes</div>
        </div>
        <div class="flex justify-end mt-6">
          <button
            @click="showDetailsModal = false"
            class="bg-gray-300 text-gray-700 px-4 py-2 rounded-lg hover:bg-gray-400 transition-colors"
          >
            Fermer
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, inject } from 'vue'
import { apiService, type Commande } from '../services/api'

// État local
const commandes = ref<Commande[]>([])
const loading = ref(false)
const showCreateModal = ref(false)
const showDetailsModal = ref(false)
const selectedCommande = ref<Commande | null>(null)

// Filtres
const filters = ref({
  statut: '',
  priorite: '',
  search: ''
})

// Nouvelle commande
const newCommande = ref({
  numeroCommande: '',
  nombreCartes: 20,
  prixTotal: 0
})

// Injection de la fonction de notification
const showNotification = inject('showNotification') as (message: string, type?: 'success' | 'error') => void

// Commandes filtrées
const filteredCommandes = computed(() => {
  return commandes.value.filter(commande => {
    const matchStatut = !filters.value.statut || commande.statut === filters.value.statut
    const matchPriorite = !filters.value.priorite || commande.priorite === filters.value.priorite
    const matchSearch = !filters.value.search ||
      commande.numeroCommande.toLowerCase().includes(filters.value.search.toLowerCase())

    return matchStatut && matchPriorite && matchSearch
  })
})

// Chargement des commandes
const loadCommandes = async () => {
  loading.value = true
  try {
    commandes.value = await apiService.getCommandes()
    console.log('Commandes chargées:', commandes.value.length)
  } catch (error) {
    console.error('Erreur lors du chargement des commandes:', error)
    showNotification?.('Erreur lors du chargement des commandes', 'error')
  } finally {
    loading.value = false
  }
}

// Création d'une commande
const creerCommande = async () => {
  loading.value = true
  try {
    await apiService.creerCommande({
      numeroCommande: newCommande.value.numeroCommande,
      nombreCartes: newCommande.value.nombreCartes,
      prixTotal: newCommande.value.prixTotal,
      priorite: calculatePriority(newCommande.value.prixTotal),
      statut: 'EN_ATTENTE',
      dateCreation: new Date().toISOString(),
      dateLimite: calculateDeadline(newCommande.value.prixTotal),
      tempsEstimeMinutes: newCommande.value.nombreCartes * 5
    })

    showNotification?.('Commande créée avec succès')
    showCreateModal.value = false
    resetNewCommande()
    await loadCommandes()
  } catch (error) {
    console.error('Erreur lors de la création de la commande:', error)
    showNotification?.('Erreur lors de la création de la commande', 'error')
  } finally {
    loading.value = false
  }
}

// Commencer une commande
const commencerCommande = async (id: number) => {
  try {
    await apiService.commencerCommande(id)
    showNotification?.('Commande commencée')
    await loadCommandes()
  } catch (error) {
    console.error('Erreur:', error)
    showNotification?.('Erreur lors du démarrage de la commande', 'error')
  }
}

// Terminer une commande
const terminerCommande = async (id: number) => {
  try {
    await apiService.terminerCommande(id)
    showNotification?.('Commande terminée')
    await loadCommandes()
  } catch (error) {
    console.error('Erreur:', error)
    showNotification?.('Erreur lors de la finalisation de la commande', 'error')
  }
}

// Voir détails
const voirDetails = (commande: Commande) => {
  selectedCommande.value = commande
  showDetailsModal.value = true
}

// Fonctions utilitaires
const calculatePriority = (prix: number): 'HAUTE' | 'MOYENNE' | 'BASSE' => {
  if (prix >= 1000) return 'HAUTE'
  if (prix >= 500) return 'MOYENNE'
  return 'BASSE'
}

const calculateDeadline = (prix: number): string => {
  const now = new Date()
  const priority = calculatePriority(prix)

  switch (priority) {
    case 'HAUTE': now.setDate(now.getDate() + 7); break
    case 'MOYENNE': now.setDate(now.getDate() + 14); break
    case 'BASSE': now.setDate(now.getDate() + 28); break
  }

  return now.toISOString()
}

const resetNewCommande = () => {
  newCommande.value = {
    numeroCommande: '',
    nombreCartes: 20,
    prixTotal: 0
  }
}

const formatDate = (dateString: string) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('fr-FR')
}

const formatPrice = (price: number) => {
  return price?.toFixed(2) || '0.00'
}

const isOverdue = (dateLimite: string) => {
  return new Date(dateLimite) < new Date()
}

const getPriorityLabel = (priorite: string) => {
  const labels = {
    'HAUTE': 'Haute',
    'MOYENNE': 'Moyenne',
    'BASSE': 'Basse'
  }
  return labels[priorite as keyof typeof labels] || priorite
}

const getPriorityColor = (priorite: string) => {
  const colors = {
    'HAUTE': 'bg-red-100 text-red-800',
    'MOYENNE': 'bg-yellow-100 text-yellow-800',
    'BASSE': 'bg-green-100 text-green-800'
  }
  return colors[priorite as keyof typeof colors] || 'bg-gray-100 text-gray-800'
}

const getStatusLabel = (statut: string) => {
  const labels = {
    'EN_ATTENTE': 'En Attente',
    'PLANIFIEE': 'Planifiée',
    'EN_COURS': 'En Cours',
    'TERMINEE': 'Terminée',
    'ANNULEE': 'Annulée'
  }
  return labels[statut as keyof typeof labels] || statut
}

const getStatusColor = (statut: string) => {
  const colors = {
    'EN_ATTENTE': 'bg-yellow-100 text-yellow-800',
    'PLANIFIEE': 'bg-blue-100 text-blue-800',
    'EN_COURS': 'bg-indigo-100 text-indigo-800',
    'TERMINEE': 'bg-green-100 text-green-800',
    'ANNULEE': 'bg-gray-100 text-gray-800'
  }
  return colors[statut as keyof typeof colors] || 'bg-gray-100 text-gray-800'
}

// Lifecycle
onMounted(() => {
  loadCommandes()
})
</script>
