import { ref, provide, inject } from 'vue'
import HomeIcon from '../../assets/icons/UI/home.vue'
import InstancesIcon from '../../assets/icons/UI/instances.vue'

export const navigationItems = [
  {
    id: 'home',
    label: 'Home',
    icon: HomeIcon
  },
  {
    id: 'instances',
    label: 'Instances',
    icon: InstancesIcon
  }
]

// Shared navigation state
const activeView = ref('home')

export const setActiveView = (viewId: string) => {
  activeView.value = viewId
}

export const useNavigation = () => {
  return {
    activeView,
    setActiveView
  }
}

// Provide navigation state to parent components
export const provideNavigation = () => {
  provide('activeView', activeView)
  provide('setActiveView', setActiveView)
}

// Inject navigation state in child components
export const injectNavigation = () => {
  const activeView = inject('activeView', ref('home'))
  const setActiveView = inject('setActiveView', (_viewId: string) => {})
  
  return {
    activeView,
    setActiveView
  }
} 