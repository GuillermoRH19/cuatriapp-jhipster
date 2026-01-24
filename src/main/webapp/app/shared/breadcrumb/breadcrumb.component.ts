import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';

export interface Breadcrumb {
  label: string;
  url: string;
}

@Component({
  standalone: true,
  selector: 'jhi-breadcrumb',
  imports: [CommonModule, RouterModule],
  templateUrl: './breadcrumb.component.html',
  styles: [
    `
      .breadcrumb-container {
        background-color: #f8f9fa;
        padding: 10px 15px;
        border-radius: 5px;
        margin-bottom: 20px;
        border: 1px solid #e9ecef;
      }
      .breadcrumb {
        margin-bottom: 0;
        padding: 0;
        background-color: transparent;
      }
      .breadcrumb-item a {
        text-decoration: none;
        color: #3e8acc;
      }
      .breadcrumb-item a:hover {
        text-decoration: underline;
      }
      .breadcrumb-item.active {
        color: #6c757d;
      }
    `,
  ],
})
export class BreadcrumbComponent implements OnInit {
  breadcrumbs: Breadcrumb[] = [];

  private router = inject(Router);
  private activatedRoute = inject(ActivatedRoute);

  ngOnInit(): void {
    // Escuchar cambios de navegación
    this.router.events.pipe(filter(event => event instanceof NavigationEnd)).subscribe(() => {
      this.breadcrumbs = this.buildBreadCrumb(this.activatedRoute.root);
    });

    // Carga inicial
    this.breadcrumbs = this.buildBreadCrumb(this.activatedRoute.root);
  }

  /**
   * Recorre recursivamente las rutas para construir el breadcrumb
   */
  private buildBreadCrumb(route: ActivatedRoute, url = '', breadcrumbs: Breadcrumb[] = []): Breadcrumb[] {
    // Si la ruta no tiene configuración, retornamos lo que llevamos
    const children: ActivatedRoute[] = route.children;
    if (children.length === 0) {
      return breadcrumbs;
    }

    for (const child of children) {
      const routeURL: string = child.snapshot.url.map(segment => segment.path).join('/');

      if (routeURL !== '') {
        url += `/${routeURL}`;
      }

      // Intentamos obtener el título de la página (data: { pageTitle: ... })
      // Si no existe, usamos el path capitalizado
      let label = child.snapshot.data['pageTitle'];
      if (!label && routeURL) {
        label = routeURL.charAt(0).toUpperCase() + routeURL.slice(1);
      }

      // Caso especial: Traducción manual de rutas comunes (opcional)
      if (label === 'home' || url === '/') label = 'Inicio';
      if (routeURL === 'admin') label = 'Administración';

      // Agregamos al breadcrumb solo si tiene label y url válida
      if (label) {
        breadcrumbs.push({ label, url });
      }

      return this.buildBreadCrumb(child, url, breadcrumbs);
    }

    return breadcrumbs;
  }
}
