import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./modulo.component').then(m => m.ModuloComponent),
    title: 'Gestión de Módulos',
  },
];

export default routes;
