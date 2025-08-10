<script setup lang="ts">
import { injectNavigation } from '../navigation/navigationData'
import { useAccountModal } from '../../composables/useAccountModal'
import AccountModal from '../modals/AccountModal.vue'

// Get navigation functions
const { setActiveView } = injectNavigation()

// Get account modal functions
const { isAccountModalOpen, openAccountModal, closeAccountModal, setCurrentAccount } = useAccountModal()

// Function to handle play now
const handlePlayNow = () => {
  setActiveView('instances')
}

// Function to handle account management
const handleAccountManagement = () => {
  openAccountModal()
}

// Function to handle account selection
const handleAccountSelected = (account: any) => {
  setCurrentAccount(account)
  closeAccountModal()
}
</script> 

<template>
  <div class="min-h-screen bg-stone-900">
    <!-- Main Content -->
    <div class="container mx-auto px-4 sm:px-6 py-6 sm:py-8">
      <!-- Quick Actions -->
      <div class="grid grid-cols-2 sm:grid-cols-4 gap-3 sm:gap-4 mb-6 sm:mb-8">
        <button 
          @click="handlePlayNow"
          class="bg-stone-800/50 hover:bg-stone-700/50 text-white p-4 sm:p-6 rounded-lg transition-all duration-200 border border-stone-700/50 hover:border-stone-600/50"
        >
          <div class="text-center">
            <svg class="w-6 h-6 sm:w-8 sm:h-8 mx-auto mb-2 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.828 14.828a4 4 0 01-5.656 0M9 10h1m4 0h1m-6 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
            <div class="font-medium text-sm sm:text-base">Jugar Ahora</div>
            <div class="text-xs sm:text-sm text-stone-400">Iniciar Minecraft</div>
          </div>
        </button>

        <button class="bg-stone-800/50 hover:bg-stone-700/50 text-white p-4 sm:p-6 rounded-lg transition-all duration-200 border border-stone-700/50 hover:border-stone-600/50">
          <div class="text-center">
            <svg class="w-6 h-6 sm:w-8 sm:h-8 mx-auto mb-2 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
            </svg>
            <div class="font-medium text-sm sm:text-base">Instalar</div>
            <div class="text-xs sm:text-sm text-stone-400">Nuevas versiones</div>
          </div>
        </button>

        <button 
          @click="handleAccountManagement"
          class="bg-stone-800/50 hover:bg-stone-700/50 text-white p-4 sm:p-6 rounded-lg transition-all duration-200 border border-stone-700/50 hover:border-stone-600/50"
        >
          <div class="text-center">
            <svg class="w-6 h-6 sm:w-8 sm:h-8 mx-auto mb-2 text-purple-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
            </svg>
            <div class="font-medium text-sm sm:text-base">Cuenta</div>
            <div class="text-xs sm:text-sm text-stone-400">Gestionar Cuenta no premium</div>
          </div>
        </button>

        <button class="bg-stone-800/50 hover:bg-stone-700/50 text-white p-4 sm:p-6 rounded-lg transition-all duration-200 border border-stone-700/50 hover:border-stone-600/50">
          <div class="text-center">
            <svg class="w-6 h-6 sm:w-8 sm:h-8 mx-auto mb-2 text-amber-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path>
            </svg>
            <div class="font-medium text-sm sm:text-base">Guías</div>
            <div class="text-xs sm:text-sm text-stone-400">Tutoriales</div>
          </div>
        </button>
      </div>

      <!-- Main Content Grid -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 lg:gap-8">
        <!-- Recent Games -->
        <div class="lg:col-span-2">
          <div class="bg-stone-800/30 rounded-lg border border-stone-700/50 p-4 sm:p-6">
            <h2 class="text-white font-medium text-lg mb-4">Juegos Recientes</h2>
            
            <div class="space-y-3">
              <div class="flex items-center justify-between p-3 bg-stone-800/50 rounded-lg border border-stone-700/30 hover:bg-stone-700/50 transition-colors cursor-pointer">
                <div class="flex items-center space-x-3">
                  <div class="w-8 h-8 bg-green-500/20 rounded flex items-center justify-center">
                    <svg class="w-4 h-4 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.828 14.828a4 4 0 01-5.656 0M9 10h1m4 0h1m-6 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                    </svg>
                  </div>
                  <div>
                    <div class="text-white font-medium">Minecraft 1.20.4</div>
                    <div class="text-stone-400 text-sm">Vanilla</div>
                  </div>
                </div>
                <button class="text-blue-400 hover:text-blue-300 text-sm">Jugar</button>
              </div>

              <div class="flex items-center justify-between p-3 bg-stone-800/50 rounded-lg border border-stone-700/30 hover:bg-stone-700/50 transition-colors cursor-pointer">
                <div class="flex items-center space-x-3">
                  <div class="w-8 h-8 bg-purple-500/20 rounded flex items-center justify-center">
                    <svg class="w-4 h-4 text-purple-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"></path>
                    </svg>
                  </div>
                  <div>
                    <div class="text-white font-medium">All the Mods 9</div>
                    <div class="text-stone-400 text-sm">Modpack</div>
                  </div>
                </div>
                <button class="text-blue-400 hover:text-blue-300 text-sm">Jugar</button>
              </div>
            </div>
          </div>

          <!-- Useful Links -->
          <div class="bg-stone-800/30 rounded-lg border border-stone-700/50 p-4 sm:p-6 mt-6">
            <h2 class="text-white font-medium text-lg mb-4">Enlaces Útiles</h2>
            
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <a href="https://www.minecraft.net" target="_blank" rel="noopener noreferrer" class="flex items-center space-x-3 p-3 bg-stone-800/50 rounded-lg border border-stone-700/30 hover:bg-stone-700/50 transition-colors">
                <div class="w-8 h-8 bg-green-500/20 rounded flex items-center justify-center">
                  <svg class="w-4 h-4 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9v-9m0-9v9"></path>
                  </svg>
                </div>
                <div>
                  <div class="text-white font-medium text-sm">Minecraft.net</div>
                  <div class="text-stone-400 text-xs">Sitio oficial</div>
                </div>
              </a>

              <a href="https://docs.minecraft.net" target="_blank" rel="noopener noreferrer" class="flex items-center space-x-3 p-3 bg-stone-800/50 rounded-lg border border-stone-700/30 hover:bg-stone-700/50 transition-colors">
                <div class="w-8 h-8 bg-blue-500/20 rounded flex items-center justify-center">
                  <svg class="w-4 h-4 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path>
                  </svg>
                </div>
                <div>
                  <div class="text-white font-medium text-sm">Documentación</div>
                  <div class="text-stone-400 text-xs">Guías oficiales</div>
                </div>
              </a>

              <a href="https://github.com" target="_blank" rel="noopener noreferrer" class="flex items-center space-x-3 p-3 bg-stone-800/50 rounded-lg border border-stone-700/30 hover:bg-stone-700/50 transition-colors">
                <div class="w-8 h-8 bg-gray-500/20 rounded flex items-center justify-center">
                  <svg class="w-4 h-4 text-gray-400" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
                  </svg>
                </div>
                <div>
                  <div class="text-white font-medium text-sm">GitHub</div>
                  <div class="text-stone-400 text-xs">Código fuente</div>
                </div>
              </a>

              <a href="https://discord.gg" target="_blank" rel="noopener noreferrer" class="flex items-center space-x-3 p-3 bg-stone-800/50 rounded-lg border border-stone-700/30 hover:bg-stone-700/50 transition-colors">
                <div class="w-8 h-8 bg-indigo-500/20 rounded flex items-center justify-center">
                  <svg class="w-4 h-4 text-indigo-400" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M20.317 4.3698a19.7913 19.7913 0 00-4.8851-1.5152.0741.0741 0 00-.0785.0371c-.211.3753-.4447.8648-.6083 1.2495-1.8447-.2762-3.68-.2762-5.4868 0-.1636-.3933-.4058-.8742-.6177-1.2495a.077.077 0 00-.0785-.037 19.7363 19.7363 0 00-4.8852 1.515.0699.0699 0 00-.0321.0277C.5334 9.0458-.319 13.5799.0992 18.0578a.0824.0824 0 00.0312.0561c2.0528 1.5076 4.0413 2.4228 5.9929 3.0294a.0777.0777 0 00.0842-.0276c.4616-.6304.8731-1.2952 1.226-1.9942a.076.076 0 00-.0416-.1057c-.6528-.2476-1.2743-.5495-1.8722-.8923a.077.077 0 01-.0076-.1277c.1258-.0943.2517-.1923.3718-.2914a.0743.0743 0 01.0776-.0105c3.9278 1.7933 8.18 1.7933 12.0614 0a.0739.0739 0 01.0785.0095c.1202.099.246.1981.3728.2924a.077.077 0 01-.0066.1276 12.2986 12.2986 0 01-1.873.8914.0766.0766 0 00-.0407.1067c.3604.698.7719 1.3628 1.225 1.9932a.076.076 0 00.0842.0286c1.961-.6067 3.9495-1.5219 6.0023-3.0294a.077.077 0 00.0313-.0552c.5004-5.177-.8382-9.6739-3.5485-13.6604a.061.061 0 00-.0312-.0286zM8.02 15.3312c-1.1825 0-2.1569-1.0857-2.1569-2.419 0-1.3332.9555-2.4189 2.157-2.4189 1.2108 0 2.1757 1.0952 2.1568 2.419-.019 1.3332-.9555 2.4189-2.1569 2.4189zm7.9748 0c-1.1825 0-2.1569-1.0857-2.1569-2.419 0-1.3332.9554-2.4189 2.1569-2.4189 1.2108 0 2.1757 1.0952 2.1568 2.419 0 1.3332-.9555 2.4189-2.1568 2.4189Z"/>
                  </svg>
                </div>
                <div>
                  <div class="text-white font-medium text-sm">Discord</div>
                  <div class="text-stone-400 text-xs">Comunidad</div>
                </div>
              </a>
            </div>
          </div>
        </div>

        <!-- Quick Stats -->
        <div class="lg:self-start">
          <div class="bg-stone-800/30 rounded-lg border border-stone-700/50 p-4 sm:p-6">
            <h3 class="text-white font-medium text-lg mb-4">Estadísticas</h3>
            <div class="space-y-3">
              <div class="flex justify-between items-center">
                <span class="text-stone-400 text-sm">Tiempo jugado</span>
                <span class="text-white font-medium">127h</span>
              </div>
              <div class="flex justify-between items-center">
                <span class="text-stone-400 text-sm">Versiones</span>
                <span class="text-white font-medium">8</span>
              </div>
              <div class="flex justify-between items-center">
                <span class="text-stone-400 text-sm">Modpacks</span>
                <span class="text-white font-medium">3</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Account Modal -->
    <AccountModal 
      :is-open="isAccountModalOpen" 
      @close="closeAccountModal"
      @account-selected="handleAccountSelected"
    />
  </div>
</template>