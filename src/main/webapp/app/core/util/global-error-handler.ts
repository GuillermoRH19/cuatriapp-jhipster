import { ErrorHandler, Injectable, Injector, NgZone } from '@angular/core';
import { Router } from '@angular/router';

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  constructor(
    private injector: Injector,
    private zone: NgZone,
  ) {}

  handleError(error: any): void {
    // 1. Logueamos el error en consola para que tú (el desarrollador) lo veas
    console.error('🔥 Error detectado por el Manejador Global:', error);

    // 2. Obtenemos el Router manualmente (usando Injector para evitar ciclos de dependencia)
    const router = this.injector.get(Router);

    // 3. Usamos NgZone para asegurar que Angular detecte la navegación aunque el error haya ocurrido fuera
    this.zone.run(() => {
      router.navigate(['/mi-error']);
    });
  }
}
