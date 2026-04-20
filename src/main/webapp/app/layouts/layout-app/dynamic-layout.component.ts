import { Component, OnInit, OnDestroy, HostListener, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, RouterModule, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';
import { MenuService } from '@app/core/services/menu.service';
import { Menu } from '@app/shared/models/menu.model';
import { AccountService } from '@app/core/auth/account.service';
import { AuthServerProvider } from '@app/core/auth/auth-jwt.service';
import { SidebarMenuComponent } from './sidebar-menu/sidebar-menu.component';
import SharedModule from 'app/shared/shared.module';

export interface Breadcrumb {
  label: string;
  url: string;
}

const ROUTE_LABELS: Record<string, string> = {
  inicio: 'Inicio',
  dashboard: 'Dashboard',
  admin: 'Administración',
  'user-management': 'Usuarios',
  perfil: 'Perfiles',
  modulo: 'Módulos',
  'permisos-perfil': 'Permisos',
  'registro-candidato': 'Candidatos',
  account: 'Mi Cuenta',
  settings: 'Configuración',
  entities: 'Entidades',
  new: 'Nuevo',
  edit: 'Editar',
  view: 'Detalle',
};

@Component({
  selector: 'app-dynamic-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, SidebarMenuComponent, SharedModule],
  templateUrl: './dynamic-layout.component.html',
  styleUrls: ['./dynamic-layout.component.scss'],
})
export class DynamicLayoutComponent implements OnInit, OnDestroy {
  private readonly menuService = inject(MenuService);
  private readonly accountService = inject(AccountService);
  private readonly authServerProvider = inject(AuthServerProvider);
  private readonly router = inject(Router);

  menuItems: Menu[] = [];
  currentUser: any = null;
  sidebarOpen = true;
  showUserMenu = false;
  breadcrumbs = signal<Breadcrumb[]>([]);

  private destroy$ = new Subject<void>();

  ngOnInit(): void {
    this.menuService.reloadMenu();

    this.menuService.menu$
      .pipe(takeUntil(this.destroy$))
      .subscribe((menus: Menu[]) => (this.menuItems = menus));

    this.accountService.getAuthenticationState()
      .pipe(takeUntil(this.destroy$))
      .subscribe((user: any) => {
        this.currentUser = user;
      });

    this.router.events
      .pipe(
        filter(e => e instanceof NavigationEnd),
        takeUntil(this.destroy$),
      )
      .subscribe(() => this.breadcrumbs.set(this.buildBreadcrumbs()));

    this.breadcrumbs.set(this.buildBreadcrumbs());
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!(event.target as HTMLElement).closest('.user-menu-wrapper')) {
      this.showUserMenu = false;
    }
  }

  private buildBreadcrumbs(): Breadcrumb[] {
    const url = this.router.url.split('?')[0];
    const segments = url.split('/').filter(s => s && s !== 'dashboard');
    const crumbs: Breadcrumb[] = [];
    let path = '/dashboard';

    for (const seg of segments) {
      path += `/${seg}`;
      if (/^\d+$/.test(seg)) continue;
      crumbs.push({
        label: ROUTE_LABELS[seg] ?? seg.charAt(0).toUpperCase() + seg.slice(1).replace(/-/g, ' '),
        url: path,
      });
    }
    return crumbs;
  }

  get currentPageTitle(): string {
    const crumbs = this.breadcrumbs();
    return crumbs.length > 0 ? crumbs[crumbs.length - 1].label : 'Dashboard';
  }

  toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
  }

  toggleUserMenu(): void {
    this.showUserMenu = !this.showUserMenu;
  }

  logout(): void {
    this.authServerProvider.logout().subscribe({
      complete: () => {
        this.accountService.authenticate(null);
        this.router.navigate(['/']);
      },
    });
  }

  goToProfile(): void {
    this.showUserMenu = false;
    this.router.navigate(['/dashboard/account/settings']);
  }

  getUserInitials(): string {
    if (!this.currentUser) return 'U';
    const first = (this.currentUser.firstName ?? '').charAt(0);
    const last = (this.currentUser.lastName ?? '').charAt(0);
    return (first + last).toUpperCase() || (this.currentUser.login ?? 'U').charAt(0).toUpperCase();
  }

  isAdmin(): boolean {
    return this.accountService.hasAnyAuthority('ROLE_ADMIN');
  }
}
