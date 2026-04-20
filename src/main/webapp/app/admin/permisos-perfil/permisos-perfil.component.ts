import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import SharedModule from 'app/shared/shared.module';
import { IPerfil } from '../perfil/perfil.model';
import { PerfilService } from '../perfil/perfil.service';
import { IPermisoModulo } from './permisos-perfil.model';
import { PermisosPerfilService } from './permisos-perfil.service';
import { AccountService } from 'app/core/auth/account.service';
import { PermissionService } from 'app/core/auth/permission.service';

@Component({
  selector: 'jhi-permisos-perfil',
  templateUrl: './permisos-perfil.component.html',
  imports: [SharedModule, FormsModule],
})
export class PermisosPerfilComponent implements OnInit {
  perfiles = signal<IPerfil[]>([]);
  permisos = signal<IPermisoModulo[]>([]);
  selectedPerfilId: number | null = null;
  isLoading = false;
  isSaving = false;
  savedOk = false;

  private readonly perfilService = inject(PerfilService);
  private readonly permisosService = inject(PermisosPerfilService);
  private readonly accountService = inject(AccountService);
  private readonly permissionService = inject(PermissionService);

  get isAdmin(): boolean {
    return this.accountService.hasAnyAuthority('ROLE_ADMIN');
  }

  can(accion: 'consulta' | 'agregar' | 'editar' | 'eliminar' | 'detalle'): boolean {
    if (this.isAdmin) return true;
    return this.permissionService.hasPermission('Permisos Perfil', accion);
  }

  ngOnInit(): void {
    this.perfilService.getAll().subscribe(p => this.perfiles.set(p));
  }

  onPerfilChange(): void {
    if (this.selectedPerfilId == null) {
      this.permisos.set([]);
      return;
    }
    this.isLoading = true;
    this.permisosService.getViewByPerfil(this.selectedPerfilId).subscribe({
      next: data => {
        this.permisos.set(data);
        this.isLoading = false;
        this.savedOk = false;
      },
      error: () => (this.isLoading = false),
    });
  }

  toggleAll(field: keyof IPermisoModulo, value: boolean): void {
    this.permisos.update(list =>
      list.map(p => ({ ...p, [field]: value ? 1 : 0 })),
    );
  }

  setPermiso(idModulo: number, field: keyof IPermisoModulo, value: boolean): void {
    this.permisos.update(list =>
      list.map(p => p.idModulo === idModulo ? { ...p, [field]: value ? 1 : 0 } : p),
    );
  }

  save(): void {
    if (this.selectedPerfilId == null) return;
    this.isSaving = true;
    const payload = this.permisos().map(p => ({
      idModulo: p.idModulo,
      bitAgregar: p.bitAgregar,
      bitEditar: p.bitEditar,
      bitEliminar: p.bitEliminar,
      bitConsulta: p.bitConsulta,
      bitDetalle: p.bitDetalle,
    }));
    this.permisosService.bulkUpdate(this.selectedPerfilId, payload).subscribe({
      next: () => {
        this.isSaving = false;
        this.savedOk = true;
      },
      error: () => (this.isSaving = false),
    });
  }

  trackByModulo(index: number, item: IPermisoModulo): number {
    return item.idModulo;
  }
}
