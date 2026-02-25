import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';
import dayjs from 'dayjs/esm';

import { CandidatoService } from '../service/candidato.service';
import { ICandidato } from '../candidato.model';

@Component({
  selector: 'jhi-candidato-update',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './candidato-update.component.html',
  styleUrls: ['./candidato-update.component.scss'],
})
export class CandidatoUpdateComponent implements OnInit {
  isSaving = false;
  candidatos: ICandidato[] = [];
  candidatosFiltrados: ICandidato[] = [];

  // Paginación
  candidatosPaginados: ICandidato[] = [];
  paginaActual = 1;
  itemsPorPagina = 5;
  totalPaginas = 1;

  // Controles de Filtro Avanzado
  filtroBusqueda = new FormControl('');
  filtroDepartamento = new FormControl('');
  filtroEdadMin = new FormControl<number | null>(null);
  filtroEdadMax = new FormControl<number | null>(null);
  filtroSalarioMin = new FormControl<number | null>(null);
  filtroSalarioMax = new FormControl<number | null>(null);

  departamentosUnicos: string[] = [];
  mostrarFormulario = false;

  editForm = new FormGroup({
    id: new FormControl<number | null>(null),
    nombre: new FormControl<string>('', { nonNullable: true, validators: [Validators.required] }),
    email: new FormControl<string>('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    fechaNacimiento: new FormControl<string | null>(null, { validators: [Validators.required, mayorDeEdadValidator] }),
    departamento: new FormControl<string>('', { nonNullable: true, validators: [Validators.required] }),
    salario: new FormControl<number | null>(null, { validators: [Validators.required, Validators.min(0), Validators.max(15000)] }),
    tokenAcceso: new FormControl(null),
  });

  protected candidatoService = inject(CandidatoService);
  protected activatedRoute = inject(ActivatedRoute);
  protected router = inject(Router);

  ngOnInit(): void {
    this.cargarCandidatos();

    this.filtroBusqueda.valueChanges.subscribe(() => this.aplicarFiltros());
    this.filtroDepartamento.valueChanges.subscribe(() => this.aplicarFiltros());
    this.filtroEdadMin.valueChanges.subscribe(() => this.aplicarFiltros());
    this.filtroEdadMax.valueChanges.subscribe(() => this.aplicarFiltros());
    this.filtroSalarioMin.valueChanges.subscribe(() => this.aplicarFiltros());
    this.filtroSalarioMax.valueChanges.subscribe(() => this.aplicarFiltros());

    this.activatedRoute.data.subscribe(({ candidato }) => {
      if (candidato?.id) {
        this.editarDeTabla(candidato);
      }
    });
  }

  cerrarSesion(): void {
    // 1. Limpiamos los tokens de sesión de JHipster
    localStorage.removeItem('jhi-authenticationToken');
    sessionStorage.removeItem('jhi-authenticationToken');

    // 2. Redirigimos y forzamos recarga para limpiar memoria
    this.router.navigate(['/login']).then(() => {
      window.location.reload();
    });
  }

  aplicarFiltros(): void {
    let resultado = [...this.candidatos];

    const texto = this.filtroBusqueda.value?.toLowerCase() ?? '';
    const deptoFiltro = this.filtroDepartamento.value?.toLowerCase() ?? '';
    const eMin = this.filtroEdadMin.value;
    const eMax = this.filtroEdadMax.value;
    const sMin = this.filtroSalarioMin.value;
    const sMax = this.filtroSalarioMax.value;

    resultado = resultado.filter(c => {
      const coincideTexto =
        (!texto || (c.nombre?.toLowerCase().includes(texto) ?? c.email?.toLowerCase().includes(texto))) ??
        c.departamento?.toLowerCase().includes(texto);

      const coincideDepto = !deptoFiltro || c.departamento?.toLowerCase() === deptoFiltro;

      const edadResult = this.calcularEdad(c.fechaNacimiento);
      const edad = typeof edadResult === 'number' ? edadResult : -1;
      // eslint-disable-next-line @typescript-eslint/no-unnecessary-condition
      const coincideEdadMin = eMin === null || eMin === undefined || (edad !== -1 && edad >= eMin);
      // eslint-disable-next-line @typescript-eslint/no-unnecessary-condition
      const coincideEdadMax = eMax === null || eMax === undefined || (edad !== -1 && edad <= eMax);

      const salario = c.salario ?? 0;
      // eslint-disable-next-line @typescript-eslint/no-unnecessary-condition
      const coincideSalarioMin = sMin === null || sMin === undefined || salario >= sMin;
      // eslint-disable-next-line @typescript-eslint/no-unnecessary-condition
      const coincideSalarioMax = sMax === null || sMax === undefined || salario <= sMax;

      return coincideTexto && coincideDepto && coincideEdadMin && coincideEdadMax && coincideSalarioMin && coincideSalarioMax;
    });

    this.candidatosFiltrados = resultado;

    this.totalPaginas = Math.ceil(this.candidatosFiltrados.length / this.itemsPorPagina) || 1;
    this.paginaActual = 1;
    this.actualizarPaginacion();
  }

  actualizarPaginacion(): void {
    const inicio = (this.paginaActual - 1) * this.itemsPorPagina;
    const fin = inicio + this.itemsPorPagina;
    this.candidatosPaginados = this.candidatosFiltrados.slice(inicio, fin);
  }

  irAPagina(pagina: number): void {
    if (pagina >= 1 && pagina <= this.totalPaginas) {
      this.paginaActual = pagina;
      this.actualizarPaginacion();
    }
  }

  primeraPagina(): void {
    this.irAPagina(1);
  }
  ultimaPagina(): void {
    this.irAPagina(this.totalPaginas);
  }
  paginaAnterior(): void {
    this.irAPagina(this.paginaActual - 1);
  }
  paginaSiguiente(): void {
    this.irAPagina(this.paginaActual + 1);
  }

  calcularEdad(fechaNacimiento: any): number | string {
    if (!fechaNacimiento) return 'N/A';
    return dayjs().diff(dayjs(fechaNacimiento), 'year');
  }

  extraerDepartamentosUnicos(): void {
    const deptos = this.candidatos.map(c => c.departamento?.trim()).filter(d => !!d) as string[];
    this.departamentosUnicos = [...new Set(deptos)].sort();
  }

  cargarCandidatos(): void {
    this.candidatoService.query().subscribe({
      next: (res: HttpResponse<ICandidato[]>) => {
        this.candidatos = res.body ?? [];
        this.extraerDepartamentosUnicos();
        this.aplicarFiltros();
      },
      error: () => console.error('Error al cargar candidatos'),
    });
  }

  save(): void {
    this.isSaving = true;
    if (this.editForm.invalid) {
      this.isSaving = false;
      return;
    }

    const candidato = this.createFromForm();

    // eslint-disable-next-line @typescript-eslint/no-unnecessary-condition
    if (candidato.id !== null && candidato.id !== undefined) {
      this.subscribeToSaveResponse(this.candidatoService.update(candidato));
    } else {
      this.subscribeToSaveResponse(this.candidatoService.create(candidato));
    }
  }

  previousState(): void {
    window.history.back();
  }

  crearNuevo(): void {
    this.mostrarFormulario = true;
    this.editForm.reset();
    setTimeout(() => {
      const formElement = document.getElementById('formulario-candidato');
      if (formElement) formElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }, 100);
  }

  editarDeTabla(candidato: ICandidato): void {
    this.mostrarFormulario = true;
    this.updateForm(candidato);
    setTimeout(() => {
      const formElement = document.getElementById('formulario-candidato');
      if (formElement) formElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }, 100);
  }

  cancelarEdicion(): void {
    this.mostrarFormulario = false;
    this.editForm.reset();
    window.scroll({ top: 0, behavior: 'smooth' });
  }

  eliminar(id: number): void {
    if (confirm('¿Estás seguro de que deseas eliminar este registro de candidato? Esta acción no se puede deshacer.')) {
      this.candidatoService.delete(id).subscribe({
        next: () => {
          this.cargarCandidatos();
        },
        error() {
          alert('Ocurrió un error al intentar eliminar el candidato.');
        },
      });
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ICandidato>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.mostrarFormulario = false;
    this.editForm.reset();
    this.cargarCandidatos();
    window.scroll({ top: 0, behavior: 'smooth' });
  }

  protected onSaveError(): void {
    alert('Error al guardar. Por favor, verifica la consola o contacta a soporte.');
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(candidato: ICandidato): void {
    this.editForm.patchValue({
      id: candidato.id,
      nombre: candidato.nombre ?? '',
      email: candidato.email ?? '',
      departamento: candidato.departamento ?? '',
      fechaNacimiento: candidato.fechaNacimiento ? dayjs(candidato.fechaNacimiento).format('YYYY-MM-DD') : null,
      salario: candidato.salario,
    });
  }

  protected createFromForm(): ICandidato {
    const rawValue = this.editForm.getRawValue();
    return {
      ...rawValue,
      fechaNacimiento: rawValue.fechaNacimiento ? dayjs(rawValue.fechaNacimiento) : null,
    } as ICandidato;
  }
}

function mayorDeEdadValidator(control: AbstractControl): ValidationErrors | null {
  if (!control.value) {
    return null;
  }
  const fechaNacimiento = dayjs(control.value);
  const hoy = dayjs();
  const edad = hoy.diff(fechaNacimiento, 'year');
  return edad < 18 ? { menorDeEdad: true } : null;
}
