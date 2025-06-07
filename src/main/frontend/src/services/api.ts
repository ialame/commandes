// services/api.ts
const API_BASE_URL = 'http://localhost:8080/api'

// Types TypeScript
export interface Commande {
  id?: string  // Changé de number à string pour ULID
  numeroCommande: string
  nombreCartes: number
  prixTotal: number
  priorite: 'HAUTE' | 'MOYENNE' | 'BASSE'
  statut: 'EN_ATTENTE' | 'PLANIFIEE' | 'EN_COURS' | 'TERMINEE' | 'ANNULEE'
  dateCreation: string
  dateLimite: string
  dateDebutTraitement?: string
  dateFinTraitement?: string
  tempsEstimeMinutes: number
}

export interface Employe {
  id?: number
  nom: string
  prenom: string
  email: string
  heuresTravailParJour: number
  actif: boolean
  dateCreation?: string
}

export interface Planification {
  id?: number
  commande: Commande
  employe: Employe
  datePlanifiee: string
  heureDebut: number
  dureeMinutes: number
  dateCreation?: string
  terminee: boolean
}

export interface DashboardStats {
  totalOrders: number
  pendingOrders: number
  scheduledOrders: number
  completedOrders: number
  overdueOrders: number
  activeEmployees: number
  averageProcessingTimeHours: number
  dailyOrdersChart: Record<string, number>
  ordersByPriority: Record<string, number>
  employeeWorkload: Record<string, number>
}

// Classe API Service
class ApiService {
  private async request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    const url = `${API_BASE_URL}${endpoint}`

    const config: RequestInit = {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      ...options,
    }

    try {
      const response = await fetch(url, config)

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      return await response.json()
    } catch (error) {
      console.error('API Error:', error)
      throw error
    }
  }

  // ======= COMMANDES =======
  async getCommandes(): Promise<Commande[]> {
    return this.request<Commande[]>('/commandes')
  }

  async creerCommande(commande: Omit<Commande, 'id'>): Promise<Commande> {
    return this.request<Commande>('/commandes', {
      method: 'POST',
      body: JSON.stringify(commande),
    })
  }

  async commencerCommande(id: number): Promise<string> {
    return this.request<string>(`/commandes/${id}/commencer`, {
      method: 'PUT',
    })
  }

  async terminerCommande(id: number): Promise<string> {
    return this.request<string>(`/commandes/${id}/terminer`, {
      method: 'PUT',
    })
  }

  // ======= EMPLOYÉS =======
  async getEmployes(): Promise<Employe[]> {
    return this.request<Employe[]>('/employes')
  }

  async creerEmploye(employe: Omit<Employe, 'id'>): Promise<Employe> {
    return this.request<Employe>('/employes', {
      method: 'POST',
      body: JSON.stringify(employe),
    })
  }

  async modifierEmploye(id: number, employe: Employe): Promise<Employe> {
    return this.request<Employe>(`/employes/${id}`, {
      method: 'PUT',
      body: JSON.stringify(employe),
    })
  }

  async desactiverEmploye(id: number): Promise<string> {
    return this.request<string>(`/employes/${id}`, {
      method: 'DELETE',
    })
  }

  // ======= PLANIFICATIONS =======
  async getPlanificationsByEmploye(employeId: number, debut: string, fin: string): Promise<Planification[]> {
    return this.request<Planification[]>(`/planifications/employe/${employeId}?debut=${debut}&fin=${fin}`)
  }

  async getPlanificationsByPeriode(debut: string, fin: string): Promise<Planification[]> {
    return this.request<Planification[]>(`/planifications/periode?debut=${debut}&fin=${fin}`)
  }

  async terminerPlanification(id: number): Promise<string> {
    return this.request<string>(`/planifications/${id}/terminer`, {
      method: 'PUT',
    })
  }

  async getChargeParEmploye(debut: string, fin: string): Promise<any> {
    return this.request<any>(`/planifications/charge?debut=${debut}&fin=${fin}`)
  }

  async planifierAutomatique(): Promise<any> {
    return this.request<any>('/planifications/planifier-automatique', {
      method: 'POST',
    })
  }

  // ======= DASHBOARD =======
  async getDashboardStats(): Promise<DashboardStats> {
    return this.request<DashboardStats>('/dashboard/stats')
  }

  // Dans ApiService
  async getCartesCommande(commandeId: string) {
    return this.request<{
      nombreCartes: number;
      nomsCartes: string[];
      resumeCartes: Record<string, number>;
    }>(`/commandes/${commandeId}/cartes`);
  }

  // Dans la classe ApiService
  async viderPlanifications() {
    return this.request<{
      success: boolean;
      message: string;
      planificationsSupprimees: number;
    }>('/planifications/vider', {
      method: 'DELETE'
    });
  }
}

export const apiService = new ApiService()
