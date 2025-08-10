import { ref, provide, inject } from 'vue'
import type { Account } from '../types'

// Account modal state
const isAccountModalOpen = ref(false)
const currentAccount = ref<Account | null>({
  id: '1',
  username: 'Player123',
  isActive: true,
  lastUsed: '2024-01-15',
  skin: 'https://crafatar.com/avatars/8667ba71-b85a-4004-af54-457a9734eed7?overlay'
})

// Methods
const openAccountModal = () => {
  isAccountModalOpen.value = true
}

const closeAccountModal = () => {
  isAccountModalOpen.value = false
}

const setCurrentAccount = (account: Account) => {
  currentAccount.value = account
}

// Provide account modal state to parent components
export const provideAccountModal = () => {
  provide('isAccountModalOpen', isAccountModalOpen)
  provide('currentAccount', currentAccount)
  provide('openAccountModal', openAccountModal)
  provide('closeAccountModal', closeAccountModal)
  provide('setCurrentAccount', setCurrentAccount)
}

// Inject account modal state in child components
export const useAccountModal = () => {
  const isAccountModalOpen = inject('isAccountModalOpen', ref(false))
  const currentAccount = inject('currentAccount', ref<Account | null>(null))
  const openAccountModal = inject('openAccountModal', () => {})
  const closeAccountModal = inject('closeAccountModal', () => {})
  const setCurrentAccount = inject('setCurrentAccount', (account: Account) => {})
  
  return {
    isAccountModalOpen,
    currentAccount,
    openAccountModal,
    closeAccountModal,
    setCurrentAccount
  }
}
