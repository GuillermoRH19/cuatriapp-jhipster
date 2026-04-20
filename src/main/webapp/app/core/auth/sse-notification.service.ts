import { Injectable, NgZone, inject } from '@angular/core';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { LoginService } from 'app/login/login.service';
import { PermissionService } from 'app/core/auth/permission.service';
import { AccountService } from 'app/core/auth/account.service';
import { AuthServerProvider } from 'app/core/auth/auth-jwt.service';

@Injectable({ providedIn: 'root' })
export class SseNotificationService {
  private eventSource: EventSource | null = null;
  
  private readonly config = inject(ApplicationConfigService);
  private readonly zone = inject(NgZone);
  private readonly loginService = inject(LoginService);
  private readonly permissionService = inject(PermissionService);
  private readonly accountService = inject(AccountService);
  private readonly authServerProvider = inject(AuthServerProvider);

  startListening(): void {
    if (this.eventSource) {
      return;
    }

    const token = this.authServerProvider.getToken();
    const url = this.config.getEndpointFor(`api/sse/subscribe?access_token=${token}`);
    this.eventSource = new EventSource(url);

    this.eventSource.addEventListener('USER_DELETED', (event: any) => {
      this.zone.run(() => {
        // eslint-disable-next-line no-console
        console.warn('Usuario eliminado por el admin. Cerrando sesión.');
        this.loginService.logout();
      });
    });

    this.eventSource.addEventListener('USER_DEACTIVATED', (event: any) => {
      this.zone.run(() => {
        // eslint-disable-next-line no-console
        console.warn('Usuario desactivado por el admin. Cerrando sesión.');
        this.loginService.logout();
      });
    });

    this.eventSource.addEventListener('PERMISSIONS_UPDATED', (event: any) => {
      this.zone.run(() => {
        const affectedPerfilId = parseInt(event.data, 10);
        this.accountService.identity().subscribe(account => {
          if (account && account.perfilId === affectedPerfilId) {
            // eslint-disable-next-line no-console
            console.log('Permisos actualizados por el admin. Recargando perfil.');
            this.permissionService.loadPermissions(account.perfilId);
            // Pequeño retardo para asegurar que la re-carga finaliza antes de recargar la GUI sutilmente
            setTimeout(() => {
              window.location.reload();
            }, 500);
          }
        });
      });
    });

    this.eventSource.onerror = () => {
      if (this.eventSource && this.eventSource.readyState === EventSource.CLOSED) {
        this.stopListening();
      }
    };
  }

  stopListening(): void {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
    }
  }
}
