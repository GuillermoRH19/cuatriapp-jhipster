import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common'; // Importante si usas directivas comunes

@Component({
  selector: 'jhi-mi-error',
  standalone: true,
  imports: [RouterModule, CommonModule],
  templateUrl: './mi-error.component.html',
  styleUrl: './mi-error.component.scss', // Asegúrate de vincular el SCSS
})
export class MiErrorComponent {
  // Función simple para recargar la página actual
  recargar(): void {
    window.location.reload();
  }
}
