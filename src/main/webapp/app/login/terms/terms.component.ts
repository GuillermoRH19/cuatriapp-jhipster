import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import SharedModule from 'app/shared/shared.module';

@Component({
  standalone: true,
  selector: 'jhi-terms',
  imports: [SharedModule, RouterModule],
  template: `
    <div class="container py-5 mt-5">
      <div class="row justify-content-center">
        <div class="col-md-8">
          <div class="card shadow rounded-4 border-0">
            <div class="card-header bg-primary text-white p-4 rounded-top-4">
              <h3 class="mb-0 fw-bold">📜 Términos y Condiciones</h3>
            </div>
            <div class="card-body p-5">
              <h5 class="text-primary">Breadcrumbs Funcionando</h5>
              <p class="text-muted">Si miras arriba, ahora deberías ver la ruta completa:</p>
              <div class="alert alert-info fw-bold text-center">🏠 Inicio / Login / Términos</div>
              <hr class="my-4" />
              <div class="d-flex justify-content-end">
                <a routerLink="/login" class="btn btn-outline-dark px-4 rounded-pill">
                  <i class="fa fa-arrow-left me-2"></i> Volver al Login
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class TermsComponent {}
