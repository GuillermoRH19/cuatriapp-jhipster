export interface IPermisoModulo {
  idModulo: number;
  strNombreModulo: string;
  idPermiso: number | null;
  bitAgregar: number;
  bitEditar: number;
  bitEliminar: number;
  bitConsulta: number;
  bitDetalle: number;
}
