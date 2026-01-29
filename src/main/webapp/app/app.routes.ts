import { Routes, RouterOutlet } from '@angular/router';
import { Component } from '@angular/core';
import { HomeComponent } from './home/home.component';
import { MiErrorComponent } from './mi-error/mi-error.component';

// 👇 COMPONENTE AUXILIAR (INVISIBLE)
// Este componente actúa como "Padre" para agrupar Login y Términos.
// Gracias a esto, el Breadcrumb entenderá la jerarquía: Home > Login > Hijos
@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'app-login-layout',
  standalone: true,
  imports: [RouterOutlet],
  template: `<router-outlet></router-outlet>`,
})
class LoginLayoutComponent {}

export const routes: Routes = [
  {
    path: '',
    component: HomeComponent,
    pathMatch: 'full',
    data: {
      pageTitle: 'home.title',
      breadcrumb: 'Inicio', // NIVEL 1
    },
  },
  {
    path: 'mi-error',
    component: MiErrorComponent,
    data: {
      pageTitle: 'error.title',
    },
  },

  // 👇 AQUÍ ESTÁ LA MAGIA DE LOS 3 NIVELES
  {
    path: 'login',
    component: LoginLayoutComponent, // Usamos el contenedor padre
    data: {
      breadcrumb: 'Login', // NIVEL 2: El padre define el nombre "Login"
    },
    children: [
      {
        path: '', // Cuando la ruta es exactamente '/login'
        loadComponent: () => import('./login/login.component').then(m => m.default),
        data: { pageTitle: 'login.title' },
      },
      {
        path: 'terms', // Cuando la ruta es '/login/terms'
        loadComponent: () => import('./login/terms/terms.component').then(m => m.TermsComponent),
        data: {
          pageTitle: 'Términos y Condiciones',
          breadcrumb: 'Términos', // NIVEL 3: El hijo define "Términos"
        },
      },
    ],
  },

  /* Rutas de cuenta (descomentar si las usas)
  {
    path: 'account',
    loadChildren: () => import('./account/account.routes'),
  },
  */
  {
    path: '**',
    redirectTo: '/mi-error',
  },
];

export default routes;
