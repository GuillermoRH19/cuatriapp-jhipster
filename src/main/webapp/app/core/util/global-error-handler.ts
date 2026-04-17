import { ErrorHandler, Injectable, Injector, NgZone } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  constructor(
    private injector: Injector,
    private zone: NgZone,
  ) {}

  handleError(error: any): void {
    console.error('🔥 Error detectado por el Manejador Global:', error);

    // HTTP errors are already handled by the interceptors (auth-expired, error-handler).
    // Redirecting here would race with those interceptors and send the user to /mi-error
    // instead of /login on session expiry.
    if (error instanceof HttpErrorResponse) {
      return;
    }

    // Router/navigation errors (like NG04002) are handled by the route config.
    // Redirecting here would create a cascade of errors.
    const errorMessage = error?.message || error?.toString() || '';
    if (errorMessage.includes('NG04002') || errorMessage.includes('Cannot match any routes')) {
      return;
    }

    const router = this.injector.get(Router);
    this.zone.run(() => {
      router.navigate(['/mi-error']);
    });
  }
}

