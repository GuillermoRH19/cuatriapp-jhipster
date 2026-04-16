import { Directive, Input, OnInit, TemplateRef, ViewContainerRef, inject } from '@angular/core';
import { PermissionService } from 'app/core/auth/permission.service';

/**
 * Directiva estructural que muestra/oculta elementos según permisos del módulo.
 *
 * Uso:
 *   <button *appHasPermission="{ modulo: 'Candidatos', permiso: 'editar' }">Editar</button>
 *
 * Permisos disponibles: 'consulta' | 'agregar' | 'editar' | 'eliminar' | 'detalle'
 */
@Directive({
  selector: '[appHasPermission]',
  standalone: true,
})
export class HasPermissionDirective implements OnInit {
  @Input('appHasPermission') config!: {
    modulo: string;
    permiso: 'consulta' | 'agregar' | 'editar' | 'eliminar' | 'detalle';
  };

  private readonly templateRef = inject(TemplateRef<any>);
  private readonly viewContainer = inject(ViewContainerRef);
  private readonly permissionService = inject(PermissionService);

  ngOnInit(): void {
    const hasAccess = this.permissionService.hasPermission(this.config.modulo, this.config.permiso);
    if (hasAccess) {
      this.viewContainer.createEmbeddedView(this.templateRef);
    } else {
      this.viewContainer.clear();
    }
  }
}
