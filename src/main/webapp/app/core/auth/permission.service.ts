import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ApplicationConfigService } from 'app/core/config/application-config.service';

export interface ModulePermissions {
  idModulo: number;
  strNombreModulo: string;
  bitAgregar: number;
  bitEditar: number;
  bitEliminar: number;
  bitConsulta: number;
  bitDetalle: number;
}

@Injectable({ providedIn: 'root' })
export class PermissionService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(ApplicationConfigService);

  private permissions = signal<ModulePermissions[]>([]);
  private loaded = false;

  /** Carga los permisos del usuario autenticado. Llamar al hacer login. */
  loadPermissions(perfilId: number): void {
    const url = this.config.getEndpointFor(`api/permisos_perfil/${perfilId}`);
    this.http.get<ModulePermissions[]>(url).subscribe({
      next: data => {
        this.permissions.set(data);
        this.loaded = true;
      },
    });
  }

  /** Limpia los permisos al hacer logout. */
  clearPermissions(): void {
    this.permissions.set([]);
    this.loaded = false;
  }

  /** Verifica si el usuario tiene un permiso específico en un módulo (por nombre de módulo). */
  hasPermission(moduleName: string, permission: 'consulta' | 'agregar' | 'editar' | 'eliminar' | 'detalle'): boolean {
    const mod = this.permissions().find(p =>
      p.strNombreModulo.toLowerCase() === moduleName.toLowerCase(),
    );
    if (!mod) return false;
    const key = `bit${permission.charAt(0).toUpperCase()}${permission.slice(1)}` as keyof ModulePermissions;
    return (mod[key] as number) === 1;
  }

  /** Verifica acceso a un módulo por su ID. */
  hasAccessById(moduloId: number): boolean {
    const mod = this.permissions().find(p => p.idModulo === moduloId);
    return !!mod && mod.bitConsulta === 1;
  }

  getPermissions(): ModulePermissions[] {
    return this.permissions();
  }

  isLoaded(): boolean {
    return this.loaded;
  }
}
