import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./perfil.component').then(m => m.PerfilComponent),
    title: 'Gestión de Perfiles',
  },
];

export default routes;
