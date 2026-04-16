import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { PermissionService } from 'app/core/auth/permission.service';
import { AccountService } from 'app/core/auth/account.service';

@Component({
  selector: 'jhi-modulo-crud',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './modulo-crud.component.html',
})
export class ModuloCrudComponent implements OnInit {
  moduloNombre = '';
  filaSeleccionada: number | null = null;

  private readonly route = inject(ActivatedRoute);
  readonly permissionService = inject(PermissionService);
  private readonly accountService = inject(AccountService);

  /** Devuelve true si el usuario es admin (acceso total) */
  get isAdmin(): boolean {
    return this.accountService.hasAnyAuthority('ROLE_ADMIN');
  }

  /** Verifica permiso en el módulo actual, los admins siempre tienen todo */
  can(accion: 'consulta' | 'agregar' | 'editar' | 'eliminar' | 'detalle'): boolean {
    if (this.isAdmin) return true;
    return this.permissionService.hasPermission(this.moduloNombre, accion);
  }

  ngOnInit(): void {
    this.moduloNombre = this.route.snapshot.paramMap.get('nombre') ?? '';
  }

  seleccionar(idx: number): void {
    this.filaSeleccionada = this.filaSeleccionada === idx ? null : idx;
  }
}
