import { Routes } from '@angular/router';
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { HomeComponent } from './home/home.component';
import { MiErrorComponent } from './mi-error/mi-error.component';
import { DynamicLayoutComponent } from './layouts/layout-app/dynamic-layout.component';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

@Component({
  selector: 'jhi-login-layout',
  standalone: true,
  imports: [RouterOutlet],
  template: `<router-outlet></router-outlet>`,
})
class LoginLayoutComponent {}

export const routes: Routes = [
  {
    path: '',
    component: HomeComponent,
    title: 'Inicio',
    data: { pageTitle: 'home.title' },
  },
  {
    path: 'mi-error',
    component: MiErrorComponent,
    title: 'Error',
    data: { pageTitle: 'error.title' },
  },
  {
    path: 'login',
    component: LoginLayoutComponent,
    children: [
      {
        path: '',
        loadComponent: () => import('./login/login.component').then(m => m.default),
        title: 'Iniciar Sesión',
        data: { pageTitle: 'login.title' },
      },
      {
        path: 'terms',
        loadComponent: () => import('./login/terms/terms.component').then(m => m.TermsComponent),
        title: 'Términos y Condiciones',
        data: { pageTitle: 'login.terms' },
      },
    ],
  },

  // 👇 LAYOUT DINÁMICO - Todas las rutas autenticadas van aquí
  {
    path: 'dashboard',
    component: DynamicLayoutComponent,
    canActivate: [UserRouteAccessService],
    children: [
      {
        path: '',
        redirectTo: 'inicio',
        pathMatch: 'full',
      },
      {
        path: 'inicio',
        loadComponent: () => import('./home/home.component').then(m => m.HomeComponent),
        title: 'Inicio',
        data: { pageTitle: 'Inicio' },
      },
      {
        path: 'registro-candidato',
        loadComponent: () => import('./entities/candidato/update/candidato-update.component').then(m => m.CandidatoUpdateComponent),
        title: 'Gestión de Candidatos',
        data: {
          pageTitle: 'Gestión de Candidatos',
          authorities: ['ROLE_USER', 'ROLE_ADMIN'],
        },
      },
      {
        path: 'entities',
        loadChildren: () => import('./entities/entity.routes'),
      },
      {
        path: 'account',
        loadChildren: () => import('./account/account.route'),
      },
      {
        path: 'admin',
        data: { pageTitle: 'Administración' },
        loadChildren: () => import('./admin/admin.routes'),
      },
      {
        path: 'modulo/:nombre',
        loadComponent: () => import('./shared/modulo-crud/modulo-crud.component').then(m => m.ModuloCrudComponent),
        title: 'Módulo',
        data: { pageTitle: 'Módulo' },
      },
    ],
  },

  // 👇 RUTAS LEGADAS (sin layout dinámico)
  {
    path: 'registro-candidato-legacy',
    loadComponent: () => import('./entities/candidato/update/candidato-update.component').then(m => m.CandidatoUpdateComponent),
    title: 'Gestión de Candidatos',
    data: {
      pageTitle: 'Gestión de Candidatos',
      authorities: ['ROLE_USER', 'ROLE_ADMIN'],
    },
    canActivate: [UserRouteAccessService],
  },

  {
    path: 'entities-legacy',
    data: { pageTitle: 'global.menu.entities.main' },
    loadChildren: () => import('./entities/entity.routes'),
  },

  {
    path: '**',
    redirectTo: '/mi-error',
  },
];

export default routes;
