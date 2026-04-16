import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { IPermisoModulo } from './permisos-perfil.model';

@Injectable({ providedIn: 'root' })
export class PermisosPerfilService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(ApplicationConfigService);
  private readonly resourceUrl = this.config.getEndpointFor('api/permisos_perfil');

  /** Retorna TODOS los módulos con sus permisos actuales para un perfil (vista de administración) */
  getViewByPerfil(idPerfil: number): Observable<IPermisoModulo[]> {
    return this.http.get<IPermisoModulo[]>(`${this.resourceUrl}/view/${idPerfil}`);
  }

  /** Actualización masiva de permisos para un perfil */
  bulkUpdate(idPerfil: number, permisos: Omit<IPermisoModulo, 'strNombreModulo' | 'idPermiso'>[]): Observable<any> {
    return this.http.post<any>(`${this.resourceUrl}/bulk`, { idPerfil, permisos });
  }
}
