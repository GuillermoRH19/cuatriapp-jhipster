import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { Menu } from '@app/shared/models/menu.model';
import { ApplicationConfigService } from '@app/core/config/application-config.service';

/**
 * Servicio para gestionar menús dinámicos.
 * Consume el endpoint REST /api/menus/sidebar
 */
@Injectable({
  providedIn: 'root',
})
export class MenuService {
  private readonly httpClient = inject(HttpClient);
  private readonly applicationConfigService = inject(ApplicationConfigService);

  private menuSubject = new BehaviorSubject<Menu[]>([]);
  public menu$ = this.menuSubject.asObservable();

  constructor() {
    this.loadMenu();
  }

  /**
   * Carga el menú dinámico del servidor.
   */
  loadMenu(): void {
    this.getSidebarMenu().subscribe({
      next: (menus: Menu[]) => {
        this.menuSubject.next(menus);
      },
      error: (error: any) => {
        console.error('[MenuService] Error al cargar menú:', error);
        this.menuSubject.next([]);
      },
    });
  }

  /**
   * Obtiene el menú del usuario actual.
   * @returns Observable con la lista de menús
   */
  getSidebarMenu(): Observable<Menu[]> {
    return this.httpClient.get<Menu[]>(
      `${this.applicationConfigService.getEndpointFor('api')}/menus/sidebar`
    );
  }

  /**
   * Obtiene el menú para un perfil específico (útil para admin).
   * @param perfilId ID del perfil
   * @returns Observable con la lista de menús
   */
  getSidebarMenuByPerfil(perfilId: number): Observable<Menu[]> {
    return this.httpClient.get<Menu[]>(
      `${this.applicationConfigService.getEndpointFor('api')}/menus/sidebar/${perfilId}`
    );
  }

  /**
   * Obtiene el menú actual desde el BehaviorSubject.
   * @returns Array de menús
   */
  getCurrentMenu(): Menu[] {
    return this.menuSubject.value;
  }

  /**
   * Recarga el menú (útil cuando cambia de usuario o perfil).
   */
  reloadMenu(): void {
    this.loadMenu();
  }
}
