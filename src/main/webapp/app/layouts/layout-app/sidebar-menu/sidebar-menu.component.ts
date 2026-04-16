import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { Menu } from '@app/shared/models/menu.model';

const MENU_ICONS: Record<string, string> = {
  // Keywords en el nombre del menú → icono FA
  gestión: 'cogs',
  administración: 'users-cog',
  reportes: 'list',
  rrhh: 'users',
  recursos: 'users',
  candidatos: 'user-plus',
  finanzas: 'road',
  configuración: 'wrench',
  seguridad: 'lock',
  sistema: 'cogs',
  ventas: 'tachometer-alt',
  inventario: 'th-list',
};

function menuIcon(nombre: string): string {
  const lower = nombre.toLowerCase();
  for (const [kw, icon] of Object.entries(MENU_ICONS)) {
    if (lower.includes(kw)) return icon;
  }
  return 'folder';
}

@Component({
  selector: 'app-sidebar-menu',
  standalone: true,
  imports: [CommonModule, RouterModule, FontAwesomeModule],
  templateUrl: './sidebar-menu.component.html',
  styleUrls: ['./sidebar-menu.component.scss'],
})
export class SidebarMenuComponent implements OnChanges {
  @Input() menuItems: Menu[] = [];
  expandedMenu: Record<number, boolean> = {};

  ngOnChanges(): void {
    if (this.menuItems.length > 0 && this.menuItems[0].id != null) {
      this.expandedMenu[this.menuItems[0].id!] = true;
    }
  }

  toggleMenu(menuId: number | undefined): void {
    if (menuId == null) return;
    this.expandedMenu[menuId] = !this.expandedMenu[menuId];
  }

  isOpen(menuId: number | undefined): boolean {
    return menuId != null && this.expandedMenu[menuId] === true;
  }

  getMenuIcon(titulo: string): string {
    return menuIcon(titulo);
  }
}
