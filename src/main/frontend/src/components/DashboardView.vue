<template>
  <div class="space-y-6">
    <!-- En-tête -->
    <div class="flex justify-between items-center">
      <h2 class="text-2xl font-bold text-gray-900">📊 Dashboard</h2>
      <button
        @click="refreshData"
        :disabled="loading"
        class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors"
      >
        {{ loading ? '🔄 Chargement...' : '🔄 Actualiser' }}
      </button>
    </div>

    <!-- Message de bienvenue -->
    <div class="bg-blue-50 border border-blue-200 rounded-lg p-6">
      <h3 class="text-lg font-semibold text-blue-900 mb-2">
        🎉 Bienvenue dans votre application de gestion Pokemon !
      </h3>
      <p class="text-blue-700">
        Cette application vous permet de gérer vos commandes de cartes Pokemon avec une planification automatique intelligente.
      </p>
    </div>

    <!-- Statistiques de base -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div class="bg-white p-6 rounded-lg shadow border-l-4 border-blue-500">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">Commandes Totales</p>
            <p class="text-2xl font-bold text-blue-600">{{ stats.totalCommandes || 0 }}</p>
          </div>
          <div class="text-3xl text-blue-600">📦</div>
        </div>
      </div>

      <div class="bg-white p-6 rounded-lg shadow border-l-4 border-yellow-500">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">En Attente</p>
            <p class="text-2xl font-bold text-yellow-600">{{ stats.enAttente || 0 }}</p>
          </div>
          <div class="text-3xl text-yellow-600">⏳</div>
        </div>
      </div>

      <div class="bg-white p-6 rounded-lg shadow border-l-4 border-green-500">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">Terminées</p>
            <p class="text-2xl font-bold text-green-600">{{ stats.terminees || 0 }}</p>
          </div>
          <div class="text-3xl text-green-600">✅</div>
        </div>
      </div>

      <div class="bg-white p-6 rounded-lg shadow border-l-4 border-purple-500">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">Employés Actifs</p>
            <p class="text-2xl font-bold text-purple-600">{{ stats.employesActifs || 0 }}</p>
          </div>
          <div class="text-3xl text-purple-600">👥</div>
        </div>
      </div>
    </div>

    <!-- Actions rapides -->
    <div class="bg-white p-6 rounded-lg shadow">
      <h3 class="text-lg font-semibold mb-4">⚡ Actions rapides</h3>
      <div class="flex flex-wrap gap-3">
        <button
          @click="goToCommandes"
          class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
        >
          📋 Voir les Commandes
        </button>
        <button
          @click="goToEmployes"
          class="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition-colors"
        >
          👤 Gérer les Employés
        </button>
        <button
          @click="goToPlanification"
          class="bg-purple-600 text-white px-4 py-2 rounded-lg hover:bg-purple-700 transition-colors"
        >
          📅 Voir la Planification
        </button>
      </div>
    </div>

    <!-- Status de connexion API -->
    <div class="bg-white p-6 rounded-lg shadow">
      <h3 class="text-lg font-semibold mb-4">🔗 État de la connexion</h3>
      <div class="flex items-center space-x-2">
        <div :class="[
          'w-3 h-3 rounded-full',
          apiConnected ? 'bg-green-500' : 'bg-red-500'
        ]"></div>
        <span :class="[
          'text-sm font-medium',
          apiConnected ? 'text-green-700' : 'text-red-700'
        ]">
          {{ apiConnected ? 'API Backend connectée' : 'API Backend déconnectée' }}
        </span>
      </div>
      <p class="text-sm text-gray-600 mt-2">
        Backend: <code class="bg-gray-100 px-2 py-1 rounded">http://localhost:8080</code>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import { apiService } from '../services/api'

// État local
const loading = ref(false)
const apiConnected = ref(false)
const stats = ref({
  totalCommandes: 0,
  enAttente: 0,
  terminees: 0,
  employesActifs: 0
})

// Injection des fonctions partagées
const showNotification = inject('showNotification') as (message: string, type?: 'success' | 'error') => void
const changeTab = inject('changeTab') as (tabId: string) => void

// Fonctions de navigation
const goToCommandes = () => {
  console.log('Navigation vers commandes') // Debug
  changeTab('commandes')
}

const goToEmployes = () => {
  console.log('Navigation vers employes') // Debug
  changeTab('employes')
}

const goToPlanification = () => {
  console.log('Navigation vers planification') // Debug
  changeTab('planification')
}

// Test de connexion API
const testApiConnection = async () => {
  try {
    await apiService.getDashboardStats()
    apiConnected.value = true
    showNotification?.('Connexion API établie avec succès!')
  } catch (error) {
    apiConnected.value = false
    console.log('API non disponible - mode démo')
  }
}

// Chargement des données
const loadData = async () => {
  loading.value = true
  try {
    if (apiConnected.value) {
      const dashboardStats = await apiService.getDashboardStats()
      stats.value = {
        totalCommandes: dashboardStats.totalOrders || 0,
        enAttente: dashboardStats.pendingOrders || 0,
        terminees: dashboardStats.completedOrders || 0,
        employesActifs: dashboardStats.activeEmployees || 0
      }
    } else {
      // Données de démonstration
      stats.value = {
        totalCommandes: 15,
        enAttente: 8,
        terminees: 7,
        employesActifs: 3
      }
    }
  } catch (error) {
    console.error('Erreur lors du chargement:', error)
    showNotification?.('Erreur lors du chargement des données', 'error')
  } finally {
    loading.value = false
  }
}

// Actualisation des données
const refreshData = () => {
  testApiConnection()
  loadData()
}

// Lifecycle
onMounted(() => {
  testApiConnection()
  loadData()
})
</script>
