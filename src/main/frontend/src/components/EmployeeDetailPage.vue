<template>
  <div class="max-w-7xl mx-auto p-6 bg-gray-100 min-h-screen">

    <!-- Liste des employés -->
    <div v-if="showEmployeeList" class="bg-white rounded-lg shadow-lg p-6">
      <div class="flex items-center justify-between mb-6">
        <h1 class="text-2xl font-bold text-gray-900 flex items-center gap-2">
          👥 Sélectionner un employé
        </h1>
        <div class="flex items-center gap-4">
          <input
            v-model="selectedDate"
            type="date"
            class="border border-gray-300 rounded-lg px-3 py-2"
          />
          <div class="text-sm text-gray-600">
            📅 {{ formatDate(selectedDate) }}
          </div>
        </div>
      </div>

      <div v-if="loading" class="flex justify-center items-center py-12">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div
          v-for="emp in employeeList"
          :key="emp.id"
          @click="selectEmployee(emp.id)"
          :class="[
            'border-l-4 bg-white rounded-lg shadow hover:shadow-lg transition-all cursor-pointer p-6',
            getEmployeeStatusColor(emp.status)
          ]"
        >
          <div class="flex items-center justify-between mb-3">
            <h3 class="text-lg font-bold text-gray-900">{{ emp.name }}</h3>
            <span :class="[
              'px-2 py-1 rounded text-xs font-medium',
              emp.status === 'overloaded' ? 'bg-red-100 text-red-800' : 'bg-green-100 text-green-800'
            ]">
              {{ getEmployeeStatusText(emp) }}
            </span>
          </div>

          <div class="space-y-2 text-sm text-gray-600">
            <div class="flex justify-between">
              <span>⏱️ Temps:</span>
              <span>{{ formatTime(emp.totalMinutes) }} / {{ formatTime(emp.maxMinutes) }}</span>
            </div>
            <div class="flex justify-between">
              <span>📋 Tâches:</span>
              <span>{{ emp.taskCount }}</span>
            </div>
            <div class="flex justify-between">
              <span>🃏 Cartes:</span>
              <span>{{ emp.cardCount }}</span>
            </div>
          </div>

          <div v-if="emp.status === 'overloaded'" class="mt-3 text-right">
            <div class="text-red-600 font-semibold text-sm">
              +{{ emp.totalMinutes - emp.maxMinutes }} min de dépassement
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Détail employé -->
    <div v-else-if="employee" class="space-y-6">
      <!-- En-tête employé -->
      <div class="bg-white rounded-lg shadow-lg p-6">
        <div class="flex items-center justify-between mb-6">
          <div class="flex items-center gap-4">
            <button
              @click="backToList"
              class="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600 transition-colors"
            >
              ← Retour
            </button>
            <h1 class="text-2xl font-bold text-gray-900 flex items-center gap-2">
              👤 {{ employee.name }}
            </h1>
            <span :class="[
              'px-3 py-1 rounded',
              employee.status === 'overloaded' ? 'bg-red-100 text-red-800' : 'bg-green-100 text-green-800'
            ]">
              {{ getEmployeeStatusText(employee) }}
            </span>
          </div>

          <div class="text-sm text-gray-600">
            📅 {{ formatDate(selectedDate) }}
          </div>
        </div>

        <!-- Métriques employé -->
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div class="bg-blue-100 border border-blue-200 rounded-lg p-4">
            <div class="flex items-center gap-2 text-blue-800">
              📋 <span class="font-semibold">Tâches totales</span>
            </div>
            <div class="text-2xl font-bold text-blue-900">{{ totalTasks }}</div>
          </div>
          <div class="bg-purple-100 border border-purple-200 rounded-lg p-4">
            <div class="flex items-center gap-2 text-purple-800">
              🃏 <span class="font-semibold">Cartes totales</span>
            </div>
            <div class="text-2xl font-bold text-purple-900">{{ totalCards }}</div>
          </div>
          <div class="bg-green-100 border border-green-200 rounded-lg p-4">
            <div class="flex items-center gap-2 text-green-800">
              ✅ <span class="font-semibold">Terminées</span>
            </div>
            <div class="text-2xl font-bold text-green-900">{{ completedTasks }}</div>
          </div>
          <div class="bg-yellow-100 border border-yellow-200 rounded-lg p-4">
            <div class="flex items-center gap-2 text-yellow-800">
              🔄 <span class="font-semibold">En cours</span>
            </div>
            <div class="text-2xl font-bold text-yellow-900">{{ inProgressTasks }}</div>
          </div>
        </div>

        <!-- Action si surchargé -->
        <div v-if="employee.status === 'overloaded'" class="mt-4 p-3 bg-red-50 border border-red-200 rounded-lg">
          <div class="flex items-center justify-between">
            <span class="text-sm text-red-700 font-medium">
              ⚠️ Employé surchargé de {{ employee.totalMinutes - employee.maxMinutes }} minutes
            </span>
            <button
              @click="requestOvertime(employee.id)"
              class="px-4 py-2 bg-blue-600 text-white text-sm rounded hover:bg-blue-700 transition-colors"
            >
              Demander heures supplémentaires
            </button>
          </div>
        </div>
      </div>

      <!-- Liste des tâches -->
      <div class="bg-white rounded-lg shadow-lg p-6">
        <h2 class="text-xl font-bold text-gray-900 mb-4">📋 Tâches du jour</h2>

        <div class="space-y-3">
          <div
            v-for="task in employee.tasks"
            :key="task.id"
            class="border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <!-- En-tête de la tâche -->
            <div class="p-4">
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-4">
                  <div class="flex items-center gap-2">
                    🕐
                    <span class="font-mono text-sm bg-gray-100 px-2 py-1 rounded">
                      {{ task.startTime }} → {{ task.endTime }}
                    </span>
                    <span class="text-sm text-gray-600">
                      ({{ formatTime(task.duration) }})
                    </span>
                  </div>

                  <span :class="[
                    'px-2 py-1 rounded border text-xs font-medium',
                    getPriorityColor(task.priority)
                  ]">
                    {{ task.priority }}
                  </span>

                  <span :class="[
                    'px-2 py-1 rounded text-xs font-medium',
                    getStatusColor(task.status)
                  ]">
                    {{ task.status }}
                  </span>
                </div>

                <div class="flex items-center gap-4 text-sm text-gray-600">
                  <span>🃏 {{ task.cardCount }} cartes</span>
                  <span>💰 {{ task.amount.toFixed(2) }}€</span>
                  <button
                    @click="editTask(task.id)"
                    class="text-blue-600 hover:text-blue-800 font-medium"
                  >
                    ✏️ Modifier
                  </button>
                  <button
                    @click="toggleTaskCards(task.id)"
                    class="text-purple-600 hover:text-purple-800 font-medium"
                  >
                    {{ task.expanded ? '📁 Masquer' : '📂 Voir cartes' }}
                  </button>
                </div>
              </div>

              <div class="mt-2">
                <span class="font-mono text-sm text-gray-700 bg-gray-100 px-2 py-1 rounded">
                  ID: {{ task.id }}
                </span>
              </div>
            </div>

            <!-- Liste des cartes (accordéon) -->
            <div v-if="task.expanded" class="border-t border-gray-200 bg-gray-50">
              <div class="p-4">
                <h4 class="text-sm font-semibold text-gray-700 mb-3">
                  🃏 Cartes de cette commande ({{ task.cards.length }})
                </h4>
                <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-3">
                  <div
                    v-for="card in task.cards"
                    :key="card.id"
                    class="bg-white border border-gray-200 rounded p-3 text-sm"
                  >
                    <div class="flex items-center justify-between mb-2">
                      <div>
                        <div class="font-medium text-gray-900">{{ card.name }}</div>
                        <div class="text-gray-500 text-xs">{{ card.label_name }}</div>
                      </div>
                    </div>
                    <div class="flex justify-between text-xs text-gray-600">
                      <span>⏱️ {{ formatTime(card.duration) }}</span>
                      <span>💰 {{ card.amount.toFixed(2) }}€</span>
                    </div>
                    <div class="mt-1 text-xs text-gray-400 truncate">
                      {{ card.id }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import {useEmployeeDetail} from "../composables/useEmployeeSchedule";

// Copiez ici le contenu TypeScript de ce fichier (types et composable)
// OU importez depuis un fichier séparé :
// import { useEmployeeDetail } from '@/composables/useEmployeeDetail'

const {
  selectedDate,
  selectedEmployeeId,
  employee,
  employeeList,
  loading,
  showEmployeeList,
  totalTasks,
  totalCards,
  completedTasks,
  inProgressTasks,
  formatTime,
  formatDate,
  getEmployeeStatusText,
  getPriorityColor,
  getStatusColor,
  getEmployeeStatusColor,
  fetchEmployeeList,
  selectEmployee,
  backToList,
  editTask,
  toggleTaskCards,
  requestOvertime
} = useEmployeeDetail()

// Lifecycle
onMounted(() => {
  fetchEmployeeList(selectedDate.value)
})

// Watchers
watch(selectedDate, (newDate) => {
  if (showEmployeeList.value) {
    fetchEmployeeList(newDate)
  } else if (selectedEmployeeId.value) {
    fetchEmployeeDetail(selectedEmployeeId.value, newDate)
  }
})
</script>

<style scoped>
/* Styles personnalisés si nécessaire */
</style>
