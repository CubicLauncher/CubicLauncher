export interface NavItem {
  id: number;
  name: string;
  icon: string;
  path: string;
}

export const navItems: NavItem[] = [
  { id: 1, name: 'Inicio', icon: '', path: '/home' },
  { id: 2, name: 'Jugar', icon: '', path: '/play' },
  { id: 3, name: 'Mods', icon: '', path: '/mods' },
  { id: 4, name: 'Ajustes', icon: '', path: '/settings' },
  { id: 5, name: 'Tienda', icon: '', path: '/store' },
];
