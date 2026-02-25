import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { ICandidato, NewCandidato } from '../candidato.model';

export type PartialUpdateCandidato = Partial<ICandidato> & Pick<ICandidato, 'id'>;
export type EntityResponseType = HttpResponse<ICandidato>;
export type EntityArrayResponseType = HttpResponse<ICandidato[]>;

@Injectable({ providedIn: 'root' })
export class CandidatoService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/candidatoes');

  constructor(
    protected http: HttpClient,
    protected applicationConfigService: ApplicationConfigService,
  ) {}

  create(candidato: NewCandidato | ICandidato): Observable<EntityResponseType> {
    // Convertimos fechas si es necesario
    const copy = this.convertDateFromClient(candidato);
    return this.http.post<ICandidato>(this.resourceUrl, copy, { observe: 'response' }).pipe(map(res => this.convertDateFromServer(res)));
  }

  // --- AQUÍ FALTABA ESTE MÉTODO (Error 1) ---
  update(candidato: ICandidato): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(candidato);
    // Asumimos que la URL es /api/candidatoes/{id}
    return this.http
      .put<ICandidato>(`${this.resourceUrl}/${this.getCandidatoIdentifier(candidato)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertDateFromServer(res)));
  }

  partialUpdate(candidato: PartialUpdateCandidato): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(candidato);
    return this.http
      .patch<ICandidato>(`${this.resourceUrl}/${this.getCandidatoIdentifier(candidato)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<ICandidato>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<ICandidato[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertDateArrayFromServer(res)));
  }

  // --- AQUÍ FALTABA EL 'return' (Error 2) ---
  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getCandidatoIdentifier(candidato: Pick<ICandidato, 'id'>): number {
    return candidato.id;
  }

  compareCandidato(o1: Pick<ICandidato, 'id'> | null, o2: Pick<ICandidato, 'id'> | null): boolean {
    return o1 && o2 ? this.getCandidatoIdentifier(o1) === this.getCandidatoIdentifier(o2) : o1 === o2;
  }

  addCandidatoToCollectionIfMissing<Type extends Pick<ICandidato, 'id'>>(
    candidatoCollection: Type[],
    ...candidatoesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const candidatoes: Type[] = candidatoesToCheck.filter(isPresent);
    if (candidatoes.length > 0) {
      const candidatoCollectionIdentifiers = candidatoCollection.map(candidatoItem => this.getCandidatoIdentifier(candidatoItem));
      const candidatoesToAdd = candidatoes.filter(candidatoItem => {
        const candidatoIdentifier = this.getCandidatoIdentifier(candidatoItem);
        if (candidatoCollectionIdentifiers.includes(candidatoIdentifier)) {
          return false;
        }
        candidatoCollectionIdentifiers.push(candidatoIdentifier);
        return true;
      });
      return [...candidatoesToAdd, ...candidatoCollection];
    }
    return candidatoCollection;
  }

  protected convertDateFromClient<T extends ICandidato | NewCandidato | PartialUpdateCandidato>(candidato: T): T {
    return {
      ...candidato,
      fechaNacimiento:
        candidato.fechaNacimiento && dayjs.isDayjs(candidato.fechaNacimiento)
          ? candidato.fechaNacimiento.format('YYYY-MM-DD')
          : candidato.fechaNacimiento,
    };
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.fechaNacimiento = res.body.fechaNacimiento ? dayjs(res.body.fechaNacimiento) : undefined;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((candidato: ICandidato) => {
        candidato.fechaNacimiento = candidato.fechaNacimiento ? dayjs(candidato.fechaNacimiento) : undefined;
      });
    }
    return res;
  }
}
