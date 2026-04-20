import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import SharedModule from 'app/shared/shared.module';
import { IPerfil } from './perfil.model';
import { PerfilService } from './perfil.service';

@Component({
  selector: 'jhi-perfil',
  templateUrl: './perfil.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class PerfilComponent implements OnInit {
  perfiles = signal<IPerfil[]>([]);
  isLoading = false;
  isSaving = false;
  editingId: number | null = null;

  editForm = new FormGroup({
    nombrePerfil: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(100)] }),
    administrador: new FormControl(false, { nonNullable: true }),
  });

  private readonly perfilService = inject(PerfilService);
  private readonly modalService = inject(NgbModal);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.isLoading = true;
    this.perfilService.getAll().subscribe({
      next: data => {
        this.perfiles.set(data);
        this.isLoading = false;
      },
      error: () => (this.isLoading = false),
    });
  }

  openCreate(content: any): void {
    this.editingId = null;
    this.editForm.reset({ nombrePerfil: '', administrador: false });
    this.modalService.open(content, { backdrop: 'static' });
  }

  openEdit(content: any, perfil: IPerfil): void {
    this.editingId = perfil.id;
    this.editForm.reset({ nombrePerfil: perfil.nombrePerfil, administrador: perfil.administrador });
    this.modalService.open(content, { backdrop: 'static' });
  }

  save(modal: any): void {
    if (this.editForm.invalid) return;
    this.isSaving = true;
    const { nombrePerfil, administrador } = this.editForm.getRawValue();

    const obs =
      this.editingId !== null
        ? this.perfilService.update(this.editingId, { nombrePerfil, administrador })
        : this.perfilService.create({ nombrePerfil, administrador });

    obs.subscribe({
      next: () => {
        this.isSaving = false;
        modal.close();
        this.load();
      },
      error: () => (this.isSaving = false),
    });
  }

  delete(id: number): void {
    if (!confirm('¿Eliminar este perfil?')) return;
    this.perfilService.delete(id).subscribe({
      next: () => this.load(),
      error: (err: any) => {
        const msg = err.error?.msg || 'Error al eliminar el perfil';
        alert(msg);
      },
    });
  }
}
