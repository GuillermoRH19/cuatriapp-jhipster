import { Routes } from '@angular/router';
/* jhipster-needle-add-admin-module-import - JHipster will add admin modules imports here */

const routes: Routes = [
  {
    path: 'user-management',
    loadChildren: () => import('./user-management/user-management.route'),
    title: 'userManagement.home.title',
  },
  {
    path: 'perfil',
    loadChildren: () => import('./perfil/perfil.routes'),
    title: 'Gestión de Perfiles',
  },
  {
    path: 'modulo',
    loadChildren: () => import('./modulo/modulo.routes'),
    title: 'Gestión de Módulos',
  },
  {
    path: 'permisos-perfil',
    loadChildren: () => import('./permisos-perfil/permisos-perfil.routes'),
    title: 'Gestión de Permisos',
  },
  {
    path: 'docs',
    loadComponent: () => import('./docs/docs.component'),
    title: 'global.menu.admin.apidocs',
  },
  {
    path: 'configuration',
    loadComponent: () => import('./configuration/configuration.component'),
    title: 'configuration.title',
  },
  {
    path: 'health',
    loadComponent: () => import('./health/health.component'),
    title: 'health.title',
  },
  {
    path: 'logs',
    loadComponent: () => import('./logs/logs.component'),
    title: 'logs.title',
  },
  {
    path: 'metrics',
    loadComponent: () => import('./metrics/metrics.component'),
    title: 'metrics.title',
  },
  /* jhipster-needle-add-admin-route - JHipster will add admin routes here */
];

export default routes;
