import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./permisos-perfil.component').then(m => m.PermisosPerfilComponent),
    title: 'Gestión de Permisos',
  },
];

export default routes;
