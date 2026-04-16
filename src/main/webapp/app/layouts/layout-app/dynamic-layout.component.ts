import { Component, OnInit, OnDestroy, inject, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { MenuService } from '@app/core/services/menu.service';
import { Menu } from '@app/shared/models/menu.model';
import { AuthServerProvider } from '@app/core/auth/auth-jwt.service';
import { SidebarMenuComponent } from './sidebar-menu/sidebar-menu.component';

/**
 * Layout dinámico principal con menú jerárquico.
 * Reemplaza el layout estático.
 */
@Component({
  selector: 'app-dynamic-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, SidebarMenuComponent],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  templateUrl: './dynamic-layout.component.html',
  styleUrls: ['./dynamic-layout.component.scss'],
})
export class DynamicLayoutComponent implements OnInit, OnDestroy {
  private readonly menuService = inject(MenuService);
  private readonly authServerProvider = inject(AuthServerProvider);
  private readonly router = inject(Router);

  menuItems: Menu[] = [];
  currentUser: any = null;
  sidebarOpen = true;
  showUserMenu = false;

  private destroy$ = new Subject<void>();

  ngOnInit(): void {
    // Cargar el menú dinámico
    this.menuService.menu$
      .pipe(takeUntil(this.destroy$))
      .subscribe((menus: Menu[]) => {
        this.menuItems = menus;
        console.log('[DynamicLayout] Menú cargado:', this.menuItems);
      });

    // Obtener datos del usuario actual
    this.authServerProvider.getIdentity().then((user: any) => {
      this.currentUser = user;
      console.log('[DynamicLayout] Usuario:', this.currentUser);
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Alterna visibilidad del sidebar en mobile.
   */
  toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
  }

  /**
   * Alterna visibilidad del menú de usuario.
   */
  toggleUserMenu(): void {
    this.showUserMenu = !this.showUserMenu;
  }

  /**
   * Cierra sesión y redirige al login.
   */
  logout(): void {
    this.authServerProvider.logout().subscribe(() => {
      this.router.navigate(['/login']);
    });
  }

  /**
   * Navega al perfil del usuario.
   */
  goToProfile(): void {
    this.router.navigate(['/account/settings']);
  }

  /**
   * Obtiene las iniciales del usuario.
   */
  getUserInitials(): string {
    if (!this.currentUser) return 'U';
    const firstName = this.currentUser.firstName || '';
    const lastName = this.currentUser.lastName || '';
    return (firstName.charAt(0) + lastName.charAt(0)).toUpperCase();
  }
}
