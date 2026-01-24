import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { MiErrorComponent } from './mi-error/mi-error.component';

const routes: Routes = [
  {
    path: '',
    component: HomeComponent,
    pathMatch: 'full',
    data: {
      pageTitle: 'home.title',
    },
  },
  {
    path: 'mi-error',
    component: MiErrorComponent,
    data: {
      pageTitle: 'error.title',
    },
  },
  {
    path: 'login',
    // 👇👇👇 CORRECCIÓN IMPORTANTE 👇👇👇
    // Como tu componente es 'export default', debemos usar 'm.default'
    loadComponent: () => import('./login/login.component').then(m => m.default),
    data: { pageTitle: 'login.title' },
  },
  /* 👇 He comentado la ruta de 'account' porque te daba error de "Cannot find module".
     Probablemente el archivo 'account.routes.ts' no existe o tiene otro nombre.
     Descoméntalo solo si estás seguro de que el archivo existe.
  */
  /*
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
