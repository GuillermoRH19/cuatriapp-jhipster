export interface IPerfil {
  id: number | null;
  nombrePerfil: string;
  administrador: boolean;
}

export const NewPerfil: IPerfil = {
  id: null,
  nombrePerfil: '',
  administrador: false,
};
