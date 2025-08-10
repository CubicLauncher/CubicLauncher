<script setup lang="ts">
import { ref, computed } from 'vue'
import type { Account, AccountModalProps, AccountModalEmits } from '../../types'

const props = defineProps<AccountModalProps>()
const emit = defineEmits<AccountModalEmits>()

// Account management state
const accounts = ref<Account[]>([
  {
    id: '1',
    username: 'Player123',
    isActive: true,
    lastUsed: '2024-01-15',
    skin: 'https://crafatar.com/avatars/8667ba71-b85a-4004-af54-457a9734eed7?overlay'
  },
  {
    id: '2',
    username: 'MinerPro',
    isActive: false,
    lastUsed: '2024-01-10',
    skin: 'https://crafatar.com/avatars/8667ba71-b85a-4004-af54-457a9734eed8?overlay'
  },
  {
    id: '3',
    username: 'CraftMaster',
    isActive: false,
    lastUsed: '2024-01-05',
    skin: 'https://crafatar.com/avatars/8667ba71-b85a-4004-af54-457a9734eed9?overlay'
  }
])

const newUsername = ref('')
const isAddingAccount = ref(false)

// Computed properties
const activeAccount = computed(() => accounts.value.find(acc => acc.isActive))
const inactiveAccounts = computed(() => accounts.value.filter(acc => !acc.isActive))

// Methods
const closeModal = () => {
  emit('close')
}

const selectAccount = (account: Account) => {
  // Deactivate all accounts
  accounts.value.forEach(acc => acc.isActive = false)
  // Activate selected account
  account.isActive = true
  emit('account-selected', account)
}

const addAccount = () => {
  if (newUsername.value.trim()) {
    const newAccount: Account = {
      id: Date.now().toString(),
      username: newUsername.value.trim(),
      isActive: false,
      lastUsed: new Date().toISOString().split('T')[0]
    }
    accounts.value.push(newAccount)
    newUsername.value = ''
    isAddingAccount.value = false
  }
}

const removeAccount = (accountId: string) => {
  const index = accounts.value.findIndex(acc => acc.id === accountId)
  if (index !== -1) {
    accounts.value.splice(index, 1)
  }
}

const startAddingAccount = () => {
  isAddingAccount.value = true
  newUsername.value = ''
}

const cancelAddingAccount = () => {
  isAddingAccount.value = false
  newUsername.value = ''
}
</script>

<template>
  <!-- Modal Backdrop -->
  <div 
    v-if="isOpen" 
    class="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4"
    @click="closeModal"
  >
         <!-- Modal Content -->
     <div 
       class="bg-stone-800/95 border border-stone-700/50 rounded-lg shadow-2xl w-full max-w-sm sm:max-w-md max-h-[85vh] sm:max-h-[80vh] overflow-hidden mx-2"
       @click.stop
     >
             <!-- Modal Header -->
       <div class="flex items-center justify-between p-4 sm:p-6 border-b border-stone-700/50">
        <div class="flex items-center space-x-3">
          <div class="w-8 h-8 bg-purple-500/20 rounded flex items-center justify-center">
            <svg class="w-4 h-4 text-purple-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
            </svg>
          </div>
          <h2 class="text-white font-medium text-lg">Gestionar Cuentas</h2>
        </div>
        <button 
          @click="closeModal"
          class="text-stone-400 hover:text-white transition-colors"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </button>
      </div>

             <!-- Modal Body -->
       <div class="p-4 sm:p-6 space-y-4 sm:space-y-6 max-h-[60vh] overflow-y-auto">
        <!-- Active Account Section -->
        <div v-if="activeAccount">
          <h3 class="text-white font-medium text-sm mb-3 flex items-center">
            <span class="w-2 h-2 bg-green-400 rounded-full mr-2"></span>
            Cuenta Activa
          </h3>
          <div class="bg-stone-700/30 rounded-lg border border-stone-600/50 p-4">
            <div class="flex items-center space-x-3">
                             <img 
                 :src="activeAccount.skin || `https://crafatar.com/avatars/${activeAccount.id}?overlay`" 
                 :alt="activeAccount.username"
                 class="w-10 h-10 rounded"
                 @error="(event) => { const target = event.target as HTMLImageElement; target.src = 'https://crafatar.com/avatars/8667ba71-b85a-4004-af54-457a9734eed7?overlay'; }"
               />
              <div class="flex-1">
                <div class="text-white font-medium">{{ activeAccount.username }}</div>
                <div class="text-stone-400 text-sm">
                  Último uso: {{ activeAccount.lastUsed }}
                </div>
              </div>
              <div class="text-green-400 text-xs font-medium">ACTIVA</div>
            </div>
          </div>
        </div>

        <!-- Inactive Accounts Section -->
        <div v-if="inactiveAccounts.length > 0">
          <h3 class="text-white font-medium text-sm mb-3">Otras Cuentas</h3>
          <div class="space-y-2">
            <div 
              v-for="account in inactiveAccounts" 
              :key="account.id"
              class="bg-stone-800/50 rounded-lg border border-stone-700/30 p-3 hover:bg-stone-700/50 transition-colors cursor-pointer"
              @click="selectAccount(account)"
            >
              <div class="flex items-center justify-between">
                <div class="flex items-center space-x-3">
                                     <img 
                     :src="account.skin || `https://crafatar.com/avatars/${account.id}?overlay`" 
                     :alt="account.username"
                     class="w-8 h-8 rounded"
                     @error="(event) => { const target = event.target as HTMLImageElement; target.src = 'https://crafatar.com/avatars/8667ba71-b85a-4004-af54-457a9734eed7?overlay'; }"
                   />
                  <div>
                    <div class="text-white font-medium text-sm">{{ account.username }}</div>
                    <div class="text-stone-400 text-xs">
                      Último uso: {{ account.lastUsed }}
                    </div>
                  </div>
                </div>
                <div class="flex items-center space-x-2">
                  <button 
                    @click.stop="selectAccount(account)"
                    class="text-blue-400 hover:text-blue-300 text-xs font-medium"
                  >
                    Activar
                  </button>
                  <button 
                    @click.stop="removeAccount(account.id)"
                    class="text-red-400 hover:text-red-300 text-xs"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Add New Account Section -->
        <div v-if="!isAddingAccount">
          <button 
            @click="startAddingAccount"
            class="w-full bg-stone-700/30 hover:bg-stone-600/30 text-white p-3 rounded-lg border border-stone-600/50 hover:border-stone-500/50 transition-all duration-200 flex items-center justify-center space-x-2"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
            </svg>
            <span class="font-medium">Agregar Nueva Cuenta</span>
          </button>
        </div>

        <!-- Add Account Form -->
        <div v-else class="bg-stone-700/30 rounded-lg border border-stone-600/50 p-4">
          <h3 class="text-white font-medium text-sm mb-3">Nueva Cuenta</h3>
          <div class="space-y-3">
            <div>
              <label class="block text-stone-300 text-xs mb-1">Nombre de usuario</label>
              <input 
                v-model="newUsername"
                type="text"
                placeholder="Ingresa el nombre de usuario"
                class="w-full bg-stone-800/50 border border-stone-600/50 rounded px-3 py-2 text-white placeholder-stone-400 focus:outline-none focus:border-blue-500 transition-colors"
                @keyup.enter="addAccount"
              />
            </div>
            <div class="flex space-x-2">
              <button 
                @click="addAccount"
                :disabled="!newUsername.trim()"
                class="flex-1 bg-blue-600 hover:bg-blue-700 disabled:bg-stone-600 disabled:cursor-not-allowed text-white px-3 py-2 rounded text-sm font-medium transition-colors"
              >
                Agregar
              </button>
              <button 
                @click="cancelAddingAccount"
                class="flex-1 bg-stone-600 hover:bg-stone-500 text-white px-3 py-2 rounded text-sm font-medium transition-colors"
              >
                Cancelar
              </button>
            </div>
          </div>
        </div>

        <!-- Info Section -->
        <div class="bg-blue-500/10 border border-blue-500/20 rounded-lg p-3">
          <div class="flex items-start space-x-2">
            <svg class="w-4 h-4 text-blue-400 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
            <div class="text-blue-300 text-xs">
              <p class="font-medium mb-1">Cuentas No Premium</p>
              <p>Estas cuentas funcionan en servidores que permiten cuentas no premium (cracked). No requieren autenticación oficial de Minecraft.</p>
            </div>
          </div>
        </div>
      </div>

             <!-- Modal Footer -->
       <div class="flex items-center justify-end p-4 sm:p-6 border-t border-stone-700/50">
        <button 
          @click="closeModal"
          class="bg-stone-700 hover:bg-stone-600 text-white px-4 py-2 rounded text-sm font-medium transition-colors"
        >
          Cerrar
        </button>
      </div>
    </div>
  </div>
</template>
