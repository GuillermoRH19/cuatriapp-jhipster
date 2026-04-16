import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Menu, Modulo } from '@app/shared/models/menu.model';

/**
 * Componente para renderizar el menú jerárquico (menús + módulos).
 * Los menús son grupos, los módulos son items individuales con rutas.
 */
@Component({
  selector: 'app-sidebar-menu',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar-menu.component.html',
  styleUrls: ['./sidebar-menu.component.scss'],
})
export class SidebarMenuComponent implements OnInit {
  @Input() menuItems: Menu[] = [];
  expandedMenu: { [key: number]: boolean } = {};

  ngOnInit(): void {
    // Expandir el primer menú por defecto
    if (this.menuItems.length > 0 && this.menuItems[0].id) {
      this.expandedMenu[this.menuItems[0].id] = true;
    }
    console.log('[SidebarMenu] Menús cargados:', this.menuItems);
  }

  /**
   * Alterna la expansión de un menú.
   * @param menuId ID del menú a alternar
   */
  toggleMenu(menuId: number | undefined): void {
    if (!menuId) return;
    this.expandedMenu[menuId] = !this.expandedMenu[menuId];
  }

  /**
   * Verifica si un menú está expandido.
   * @param menuId ID del menú
   */
  isMenuOpen(menuId: number | undefined): boolean {
    if (!menuId) return false;
    return this.expandedMenu[menuId] === true;
  }

  /**
   * Obtiene el icono para el menú expandible.
   * @param menuId ID del menú
   */
  getToggleIcon(menuId: number | undefined): string {
    return this.isMenuOpen(menuId) ? 'chevron-down' : 'chevron-right';
  }
}
