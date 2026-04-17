import { Component, OnInit, inject } from '@angular/core';
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
  moduloId = 0;
  moduloNombre = '';
  filaSeleccionada: number | null = null;

  private readonly route = inject(ActivatedRoute);
  readonly permissionService = inject(PermissionService);
  private readonly accountService = inject(AccountService);
  private readonly moduloService = inject(ModuloService);

  get isAdmin(): boolean {
    return this.accountService.hasAnyAuthority('ROLE_ADMIN');
  }

  can(accion: 'consulta' | 'agregar' | 'editar' | 'eliminar' | 'detalle'): boolean {
    if (this.permissionService.isLoaded()) {
      const hasPerm = this.permissionService.hasPermissionById(this.moduloId, accion);
      // console.log(`Permiso ${accion} para módulo ${this.moduloId}: ${hasPerm}`);
      return hasPerm;
    }
    return this.isAdmin;
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.moduloId = idParam ? parseInt(idParam, 10) : 0;

    if (this.isAdmin) {
      // Admin: buscar el nombre del módulo desde el servicio
      this.moduloService.getAll().subscribe(modulos => {
        const mod = modulos.find(m => m.id === this.moduloId);
        this.moduloNombre = mod?.strNombreModulo ?? `Módulo ${this.moduloId}`;
      });
    } else {
      // Usuario normal: el nombre viene de los permisos ya cargados
      this.moduloNombre =
        this.permissionService.getModuleNameById(this.moduloId) || `Módulo ${this.moduloId}`;
    }
  }

  seleccionar(idx: number): void {
    this.filaSeleccionada = this.filaSeleccionada === idx ? null : idx;
  }
}
