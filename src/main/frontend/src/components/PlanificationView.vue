<template>
  <div class="space-y-6">
    <!-- En-tête -->
    <div class="flex justify-between items-center">
      <h2 class="text-2xl font-bold text-gray-900">📅 Planification</h2>
      <div class="flex space-x-3">
        <button
          @click="planifierAutomatiquement"
          :disabled="loading"
          class="bg-purple-600 text-white px-4 py-2 rounded-lg hover:bg-purple-700 disabled:opacity-50 transition-colors"
        >
          🤖 Planification Auto
        </button>

        <button
          @click="rafraichirDonnees"
          :disabled="loading"
          class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors"
        >

          {{ loading ? '🔄 Chargement...' : '🔄 Actualiser' }}
        </button>
        <!-- Dans la section des boutons (ligne ~18) -->
        <button
          @click="viderPlanifications"
          class="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 transition-colors"
        >
          🗑️ Vider planifications
        </button>
      </div>
    </div>

    <!-- Sélection de période -->
    <div class="bg-white p-4 rounded-lg shadow-sm">
      <div class="flex flex-wrap gap-4 items-center">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Date de début</label>
          <input
            v-model="periode.debut"
            type="date"
            class="border border-gray-300 rounded-md px-3 py-2"
          >
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Date de fin</label>
          <input
            v-model="periode.fin"
            type="date"
            class="border border-gray-300 rounded-md px-3 py-2"
          >
        </div>
        <button
          @click="chargerPlanifications"
          class="bg-gray-600 text-white px-4 py-2 rounded-lg hover:bg-gray-700 transition-colors mt-6"
        >
          🔍 Charger Période
        </button>
        <div class="flex space-x-2 mt-6">
          <button
            @click="setCurrentWeek"
            class="bg-green-600 text-white px-3 py-2 rounded-lg hover:bg-green-700 transition-colors text-sm"
          >
            Cette semaine
          </button>
          <button
            @click="setNextWeek"
            class="bg-blue-600 text-white px-3 py-2 rounded-lg hover:bg-blue-700 transition-colors text-sm"
          >
            Semaine prochaine
          </button>
        </div>
      </div>
    </div>

    <!-- Vue calendrier -->
    <div class="bg-white rounded-lg shadow">
      <div class="p-6 border-b border-gray-200">
        <h3 class="text-lg font-semibold">📅 Planning des commandes</h3>
      </div>

      <div class="p-6">
        <div v-if="planificationsGroupees.length > 0" class="space-y-6">
          <div
            v-for="groupe in planificationsGroupees"
            :key="groupe.planifications.date"
            class="border border-gray-200 rounded-lg overflow-hidden"
          >
            <div class="bg-gray-50 px-6 py-3 border-b border-gray-200">
              <h4 class="font-medium text-gray-900">
                📅 {{ formatDateComplete(groupe.date) }}
                <span class="text-sm text-gray-600 ml-2">
                  ({{ groupe.planifications.length }} tâche{{ groupe.planifications.length > 1 ? 's' : '' }})
                </span>
              </h4>
            </div>

            <div class="divide-y divide-gray-200">
              <div
                v-for="planification in groupe.planifications"
                :key="planification.id"
                class="px-6 py-4 hover:bg-gray-50"
              >
                <div class="flex justify-between items-start">
                  <div class="flex-1">
                    <div class="flex items-center space-x-3">
                      <div class="font-medium text-gray-900">
                        {{ planification.commande?.numeroCommande || 'N/A' }}
                      </div>
                      <span :class="[
                        'inline-flex px-2 py-1 text-xs font-semibold rounded-full',
                        getPriorityColor(planification.commande?.priorite || 'BASSE')
                      ]">
                        {{ getPriorityLabel(planification.commande?.priorite || 'BASSE') }}
                      </span>
                      <span :class="[
                        'inline-flex px-2 py-1 text-xs font-semibold rounded-full',
                        planification.terminee
                          ? 'bg-green-100 text-green-800'
                          : 'bg-yellow-100 text-yellow-800'
                      ]">
                        {{ planification.terminee ? '✅ Terminé' : '⏳ En cours' }}
                      </span>
                    </div>

                    <div class="mt-2 text-sm text-gray-600">
                      <div class="flex flex-wrap gap-4">
                        <span>👤 {{ planification.employe?.prenom || '' }} {{ planification.employe?.nom || '' }}</span>
                        <span>⏰ {{ planification.heureDebut }}h00</span>
                        <span>⏱️ {{ planification.dureeMinutes }} min</span>
                        <span>🃏 {{ planification.commande?.nombreCartes || 0 }} cartes</span>
                        <span>💰 {{ formatPrice(planification.commande?.prixTotal || 0) }}€</span>
                      </div>
                    </div>
                  </div>

                  <div class="flex items-center space-x-2">
                    <button
                      v-if="!planification.terminee"
                      @click="terminerPlanification(planification.id!)"
                      class="text-green-600 hover:text-green-900 text-sm"
                    >
                      ✅ Terminer
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="!loading" class="text-center py-12">
          <div class="text-gray-500">
            <div class="text-4xl mb-4">📅</div>
            <div>Aucune planification trouvée pour cette période</div>
            <button
              @click="planifierAutomatiquement"
              class="mt-4 bg-purple-600 text-white px-4 py-2 rounded-lg hover:bg-purple-700 transition-colors"
            >
              🤖 Lancer la planification automatique
            </button>
          </div>
        </div>

        <div v-if="loading" class="text-center py-12">
          <div class="text-gray-500">🔄 Chargement des planifications...</div>
        </div>
      </div>
    </div>

    <!-- Modal de résultat de planification -->
    <div v-if="showResultModal && rapportPlanification" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 w-full max-w-2xl max-h-[80vh] overflow-y-auto">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-lg font-semibold">🤖 Résultat de la planification automatique</h3>
          <button
            @click="showResultModal = false"
            class="text-gray-400 hover:text-gray-600"
          >
            ✕
          </button>
        </div>

        <!-- Statistiques du rapport -->
        <div class="grid grid-cols-2 gap-4 mb-6">
          <div class="bg-green-50 p-4 rounded-lg">
            <div class="text-2xl font-bold text-green-600">
              {{ rapportPlanification.nombreCommandesPlanifiees || 0 }}
            </div>
            <div class="text-sm text-green-700">Commandes planifiées</div>
          </div>
          <div class="bg-red-50 p-4 rounded-lg">
            <div class="text-2xl font-bold text-red-600">
              {{ rapportPlanification.nombreCommandesNonPlanifiees || 0 }}
            </div>
            <div class="text-sm text-red-700">Commandes non planifiées</div>
          </div>
        </div>

        <div class="flex justify-end">
          <button
            @click="showResultModal = false"
            class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
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
import { apiService, type Planification } from '../services/api'

// État local
const planifications = ref<Planification[]>([])
const loading = ref(false)
const showResultModal = ref(false)
const rapportPlanification = ref<any>(null)

// Période sélectionnée
const periode = ref({
  debut: new Date().toISOString().split('T')[0],
  fin: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
})


const viderPlanifications = async () => {
  if (!confirm('⚠️ Êtes-vous sûr de vouloir supprimer TOUTES les planifications ?')) {
    return;
  }

  loading.value = true;
  try {
    const result = await apiService.viderPlanifications();

    if (result.success) {
      showNotification?.(
        `${result.planificationsSupprimees} planifications supprimées`,
        'success'
      );
      // Recharger les données
      await chargerPlanifications();
    } else {
      showNotification?.('Erreur lors de la suppression', 'error');
    }
  } catch (error) {
    console.error('Erreur:', error);
    showNotification?.('Erreur lors de la suppression', 'error');
  } finally {
    loading.value = false;
  }
}

// Injection de la fonction de notification
const showNotification = inject('showNotification') as (message: string, type?: 'success' | 'error') => void

// Planifications groupées par date
const planificationsGroupees = computed(() => {
  const groupes: Record<string, Planification[]> = {}

  planifications.value.forEach(planification => {
    const date = planification.datePlanifiee
    if (!groupes[date]) {
      groupes[date] = []
    }
    groupes[date].push(planification)
  })

  return Object.entries(groupes)
    .map(([date, planifs]) => ({
      date,
      planifications: planifs.sort((a, b) => a.heureDebut - b.heureDebut)
    }))
    .sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime())
})

// Chargement des planifications
const chargerPlanifications = async () => {
  loading.value = true
  try {
    planifications.value = await apiService.getPlanificationsByPeriode(periode.value.debut, periode.value.fin)
    console.log('Planifications chargées:', planifications.value.length)
  } catch (error) {
    console.error('Erreur lors du chargement des planifications:', error)
    showNotification?.('Erreur lors du chargement des planifications', 'error')
    // Données de test en cas d'erreur
    planifications.value = []
  } finally {
    loading.value = false
  }
}

// Planification automatique
const planifierAutomatiquement = async () => {
  loading.value = true
  try {
    const rapport = await apiService.planifierAutomatique()
    rapportPlanification.value = rapport
    showResultModal.value = true

    showNotification?.(
      `Planification terminée: ${rapport.nombreCommandesPlanifiees || 0} commandes planifiées`,
      (rapport.nombreCommandesPlanifiees || 0) > 0 ? 'success' : 'error'
    )

    // Recharger les données
    await chargerPlanifications()
  } catch (error) {
    console.error('Erreur lors de la planification automatique:', error)
    showNotification?.('Erreur lors de la planification automatique', 'error')
  } finally {
    loading.value = false
  }
}

// Terminer une planification
const terminerPlanification = async (id: number) => {
  try {
    await apiService.terminerPlanification(id)
    showNotification?.('Planification terminée')
    await chargerPlanifications()
  } catch (error) {
    console.error('Erreur:', error)
    showNotification?.('Erreur lors de la finalisation de la planification', 'error')
  }
}

// Actualiser les données
const rafraichirDonnees = () => {
  chargerPlanifications()
}

// Définir la semaine courante
const setCurrentWeek = () => {
  const today = new Date()
  const monday = new Date(today.setDate(today.getDate() - today.getDay() + 1))
  const sunday = new Date(monday)
  sunday.setDate(monday.getDate() + 6)

  periode.value.debut = monday.toISOString().split('T')[0]
  periode.value.fin = sunday.toISOString().split('T')[0]
  chargerPlanifications()
}

// Définir la semaine prochaine
const setNextWeek = () => {
  const today = new Date()
  const nextMonday = new Date(today.setDate(today.getDate() - today.getDay() + 8))
  const nextSunday = new Date(nextMonday)
  nextSunday.setDate(nextMonday.getDate() + 6)

  periode.value.debut = nextMonday.toISOString().split('T')[0]
  periode.value.fin = nextSunday.toISOString().split('T')[0]
  chargerPlanifications()
}

// Fonctions utilitaires
const formatDateComplete = (dateString: string) => {
  if (!dateString) return 'Date inconnue'
  const date = new Date(dateString)
  const options: Intl.DateTimeFormatOptions = {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  }
  return date.toLocaleDateString('fr-FR', options)
}

const formatPrice = (price: number) => {
  return price?.toFixed(2) || '0.00'
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

// Lifecycle
onMounted(() => {
  setCurrentWeek()
})
</script>
