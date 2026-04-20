import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import dayjs from 'dayjs/esm';

import { CandidatoService } from 'app/entities/candidato/service/candidato.service';
import { ICandidato } from 'app/entities/candidato/candidato.model';

// Validador de edad
function mayorDeEdadValidator(control: AbstractControl): ValidationErrors | null {
  if (!control.value) return null;
  const fechaNacimiento = dayjs(control.value as string);
  const hoy = dayjs();
  const edad = hoy.diff(fechaNacimiento, 'year');
  return edad < 18 ? { menorDeEdad: true } : null;
}

@Component({
  selector: 'jhi-registro-publico',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './registro-publico.component.html',
})
export class RegistroPublicoComponent {
  isSaving = false;
  successMessage = false;

  editForm = new FormGroup({
    nombre: new FormControl<string>('', { nonNullable: true, validators: [Validators.required] }),
    email: new FormControl<string>('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    fechaNacimiento: new FormControl<string | null>(null, { validators: [Validators.required, mayorDeEdadValidator] }),
    departamento: new FormControl<string>('', { nonNullable: true, validators: [Validators.required] }),
    salario: new FormControl<number | null>(null, { validators: [Validators.required, Validators.min(0), Validators.max(15000)] }),
  });

  private readonly candidatoService: CandidatoService = inject(CandidatoService);

  save(): void {
    if (this.editForm.get('salario')?.hasError('max')) {
      alert('⚠️ No puedes postularte con un salario mayor a $15,000. Por favor, ajusta la cantidad.');
      return;
    }

    if (this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      return;
    }

    this.isSaving = true;
    const candidato = this.createFromForm();

    this.candidatoService.create(candidato).subscribe({
      next: () => {
        this.isSaving = false;
        this.successMessage = true;
        this.editForm.reset();
        setTimeout(() => (this.successMessage = false), 5000);
      },
      error: () => {
        this.isSaving = false;
        alert('Hubo un error al registrar. Verifica tu conexión o contacta a soporte.');
      },
    });
  }

  private createFromForm(): ICandidato {
    const rawValue = this.editForm.getRawValue();
    return {
      nombre: rawValue.nombre,
      email: rawValue.email,
      departamento: rawValue.departamento,
      salario: rawValue.salario,
      fechaNacimiento: rawValue.fechaNacimiento ? dayjs(rawValue.fechaNacimiento) : null,
    } as ICandidato;
  }
}
