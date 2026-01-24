import { ApplicationConfig, ErrorHandler } from '@angular/core'; // 👈 Asegúrate de importar ErrorHandler
import { provideRouter, withComponentInputBinding, withDebugTracing } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';

// 👇 IMPORTA TU NUEVA CLASE
import { GlobalErrorHandler } from 'app/core/util/global-error-handler';

import routes from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes, withComponentInputBinding(), withDebugTracing()),
    provideHttpClient(withInterceptorsFromDi()),
    provideAnimations(),

    // 👇 AGREGA ESTO: Esto reemplaza el manejo de errores por defecto con el tuyo
    { provide: ErrorHandler, useClass: GlobalErrorHandler },
  ],
};
