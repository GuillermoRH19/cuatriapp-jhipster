/**
 * Modelo para un módulo dentro de un menú.
 */
export interface Modulo {
  id?: number;
  nombre?: string;
  ruta?: string;
}

/**
 * Modelo para la estructura jerárquica del menú.
 */
export interface Menu {
  id?: number;
  titulo?: string;
  idHtml?: string;
  submodulos?: Modulo[];
}
