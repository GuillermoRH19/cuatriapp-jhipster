import { ApplicationConfig, ErrorHandler } from '@angular/core';
import { provideRouter, withComponentInputBinding, withDebugTracing } from '@angular/router';
// 👇 Importamos HTTP_INTERCEPTORS y withInterceptorsFromDi
import { provideHttpClient, HTTP_INTERCEPTORS, withInterceptorsFromDi } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';

import { GlobalErrorHandler } from 'app/core/util/global-error-handler';
import routes from './app.routes';

// 👇 Importamos la clase AuthInterceptor (con mayúscula, como nos indicó el error)
import { AuthInterceptor } from 'app/core/interceptor/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes, withComponentInputBinding(), withDebugTracing()),

    // 👇 Le decimos a Angular que habilite los interceptores basados en clases
    provideHttpClient(withInterceptorsFromDi()),

    // 👇 Registramos tu AuthInterceptor para que inyecte el token JWT en las peticiones
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },

    provideAnimations(),

    { provide: ErrorHandler, useClass: GlobalErrorHandler },
  ],
};
