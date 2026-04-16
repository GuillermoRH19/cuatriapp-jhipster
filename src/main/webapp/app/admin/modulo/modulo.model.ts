export interface IModulo {
  id: number | null;
  strNombreModulo: string;
  idMenu: number | null;
  strNombreMenu: string;
  strRuta: string | null;
}

export interface IMenu {
  id: number;
  strNombreMenu: string;
}
