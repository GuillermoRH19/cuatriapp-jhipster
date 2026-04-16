import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { IModulo, IMenu } from './modulo.model';

@Injectable({ providedIn: 'root' })
export class ModuloService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(ApplicationConfigService);
  private readonly resourceUrl = this.config.getEndpointFor('api/modulo');

  getAll(): Observable<IModulo[]> {
    return this.http.get<IModulo[]>(this.resourceUrl);
  }

  getMenus(): Observable<IMenu[]> {
    return this.http.get<IMenu[]>(`${this.resourceUrl}/lista/menus`);
  }

  create(data: { strNombreModulo: string; nombreMenu: string; strRuta: string }): Observable<any> {
    return this.http.post<any>(this.resourceUrl, data);
  }

  update(id: number, data: { strNombreModulo: string; nombreMenu: string; strRuta: string }): Observable<any> {
    return this.http.put<any>(`${this.resourceUrl}/${id}`, data);
  }

  delete(id: number): Observable<any> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`);
  }
}
