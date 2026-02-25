import dayjs from 'dayjs/esm';

export interface ICandidato {
  id: number;
  nombre?: string | null;
  email?: string | null;
  fechaNacimiento?: dayjs.Dayjs | null;
  departamento?: string | null;
  salario?: number | null;
  tokenAcceso?: string | null;
}

export type NewCandidato = Omit<ICandidato, 'id'> & { id: null };
