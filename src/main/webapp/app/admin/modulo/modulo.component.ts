import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import SharedModule from 'app/shared/shared.module';
import { IModulo, IMenu } from './modulo.model';
import { ModuloService } from './modulo.service';

@Component({
  selector: 'jhi-modulo',
  templateUrl: './modulo.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class ModuloComponent implements OnInit {
  modulos = signal<IModulo[]>([]);
  menus = signal<IMenu[]>([]);
  isLoading = false;
  isSaving = false;
  editingId: number | null = null;

  editForm = new FormGroup({
    strNombreModulo: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(100)] }),
    nombreMenu: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    strRuta: new FormControl('', { nonNullable: true }),
  });

  private readonly moduloService = inject(ModuloService);
  private readonly modalService = inject(NgbModal);

  ngOnInit(): void {
    this.load();
    this.loadMenus();
  }

  loadMenus(): void {
    this.moduloService.getMenus().subscribe({
      next: menus => this.menus.set(menus),
      error: () => {},
    });
  }

  load(): void {
    this.isLoading = true;
    this.moduloService.getAll().subscribe({
      next: data => {
        this.modulos.set(data);
        this.isLoading = false;
      },
      error: () => (this.isLoading = false),
    });
  }

  openCreate(content: any): void {
    this.editingId = null;
    this.editForm.reset({ strNombreModulo: '', nombreMenu: '', strRuta: '' });
    this.modalService.open(content, { backdrop: 'static' });
  }

  openEdit(content: any, modulo: IModulo): void {
    this.editingId = modulo.id;
    this.editForm.reset({
      strNombreModulo: modulo.strNombreModulo,
      nombreMenu: modulo.strNombreMenu,
      strRuta: modulo.strRuta ?? '',
    });
    this.modalService.open(content, { backdrop: 'static' });
  }

  save(modal: any): void {
    if (this.editForm.invalid) return;
    this.isSaving = true;
    const { strNombreModulo, nombreMenu, strRuta } = this.editForm.getRawValue();

    const obs =
      this.editingId !== null
        ? this.moduloService.update(this.editingId, { strNombreModulo, nombreMenu, strRuta })
        : this.moduloService.create({ strNombreModulo, nombreMenu, strRuta });

    obs.subscribe({
      next: (res: any) => {
        this.isSaving = false;
        if (res?.success === false) {
          alert(res.msg ?? 'Error al guardar.');
          return;
        }
        // Si es creación nueva, mostrar la ruta auto-generada
        if (this.editingId === null && res?.ruta) {
          alert(`✅ Módulo creado exitosamente.\n\nRuta asignada: ${res.ruta}\n\nEsta ruta se usará automáticamente en el menú del usuario.`);
        }
        modal.close();
        this.load();
        this.loadMenus();
      },
      error: () => (this.isSaving = false),
    });
  }

  delete(id: number): void {
    if (!confirm('¿Eliminar este módulo?')) return;
    this.moduloService.delete(id).subscribe({
      next: (res: any) => {
        if (res?.success === false) {
          alert(res.msg ?? 'No se puede eliminar.');
          return;
        }
        this.load();
      },
      error: () => alert('Error al eliminar el módulo.'),
    });
  }
}
