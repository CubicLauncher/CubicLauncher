// ━━━━━━━ TYPES ACCOUNT ━━━━━━━

// Account related types
export interface Account {
  id: string
  username: string
  isActive: boolean
  lastUsed?: string
  skin?: string
}

// Modal props and emits
export interface AccountModalProps {
  isOpen: boolean
}

export interface AccountModalEmits {
  (e: 'close'): void
  (e: 'account-selected', account: Account): void
}

// ━━━━━━━ TYPES ACCOUNT END ━━━━━━━