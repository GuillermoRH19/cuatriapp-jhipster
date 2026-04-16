import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { IPerfil } from './perfil.model';

@Injectable({ providedIn: 'root' })
export class PerfilService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(ApplicationConfigService);
  private readonly resourceUrl = this.config.getEndpointFor('api/perfil');

  getAll(): Observable<IPerfil[]> {
    return this.http.get<IPerfil[]>(this.resourceUrl);
  }

  getById(id: number): Observable<IPerfil> {
    return this.http.get<IPerfil>(`${this.resourceUrl}/${id}`);
  }

  create(data: { nombrePerfil: string; administrador: boolean }): Observable<any> {
    return this.http.post<any>(this.resourceUrl, data);
  }

  update(id: number, data: { nombrePerfil: string; administrador: boolean }): Observable<any> {
    return this.http.put<any>(`${this.resourceUrl}/${id}`, data);
  }

  delete(id: number): Observable<any> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`);
  }
}
