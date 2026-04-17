import { Component, OnInit, inject, computed, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { PermissionService } from 'app/core/auth/permission.service';
import { AccountService } from 'app/core/auth/account.service';
import { ModuloService } from 'app/admin/modulo/modulo.service';

@Component({
  selector: 'jhi-modulo-crud',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './modulo-crud.component.html',
})
export class ModuloCrudComponent implements OnInit {
  moduloId = signal(0);
  filaSeleccionada: number | null = null;

  private readonly route = inject(ActivatedRoute);
  readonly permissionService = inject(PermissionService);
  private readonly accountService = inject(AccountService);
  private readonly moduloService = inject(ModuloService);

  moduloNombre = computed(() => {
    const id = this.moduloId();
    if (id === 0) return 'Módulo';
    if (this.isAdmin) return `Módulo ${id}`; // Admin logic handled in loadModulo for now
    return this.permissionService.getModuleNameById(id) || `Módulo ${id}`;
  });

  get isAdmin(): boolean {
    return this.accountService.hasAnyAuthority('ROLE_ADMIN');
  }

  can(accion: 'consulta' | 'agregar' | 'editar' | 'eliminar' | 'detalle'): boolean {
    if (this.permissionService.isLoaded()) {
      const hasPerm = this.permissionService.hasPermissionById(this.moduloId(), accion);
      // console.log(`Permiso ${accion} para módulo ${this.moduloId()}: ${hasPerm}`);
      return hasPerm;
    }
    return this.isAdmin;
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const idParam = params.get('id');
      this.moduloId.set(idParam ? parseInt(idParam, 10) : 0);
      this.loadModulo();
    });
  }

  loadModulo(): void {
    if (this.isAdmin) {
      this.moduloService.getAll().subscribe(modulos => {
        const mod = modulos.find(m => m.id === this.moduloId());
        if (mod) {
          // Si somos admin, necesitamos una forma de sobreescribir el computed o simplemente usar un signal para el nombre
          // Para no complicar con WritableSignal + Computed, solo lo manejamos así:
          (this as any)._adminNombre = mod.strNombreModulo;
        }
      });
    }
  }

  get displayNombre(): string {
    return (this as any)._adminNombre || this.moduloNombre();
  }

  seleccionar(idx: number): void {
    this.filaSeleccionada = this.filaSeleccionada === idx ? null : idx;
  }
}
