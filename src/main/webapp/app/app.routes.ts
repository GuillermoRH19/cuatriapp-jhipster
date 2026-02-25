import { Routes } from '@angular/router';
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { HomeComponent } from './home/home.component';
import { MiErrorComponent } from './mi-error/mi-error.component';
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

  // 👇 RUTA DE LA VISTA PRIVADA (Protegida por login)
  {
    path: 'registro-candidato',
    loadComponent: () => import('./entities/candidato/update/candidato-update.component').then(m => m.CandidatoUpdateComponent),
    title: 'Gestión de Candidatos', // Nombre actualizado para reflejar su nueva función
    data: {
      pageTitle: 'Gestión de Candidatos',
      authorities: ['ROLE_USER', 'ROLE_ADMIN'], // Permisos requeridos
    },
    canActivate: [UserRouteAccessService], // Asegura que bloquee a usuarios no logueados
  },

  {
    path: 'entities',
    data: { pageTitle: 'global.menu.entities.main' },
    loadChildren: () => import('./entities/entity.routes'),
  },

  {
    path: '**',
    redirectTo: '/mi-error',
  },
];

export default routes;
