import { Component, OnInit, OnDestroy, inject, signal, ViewChild, TemplateRef } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';
import SharedModule from 'app/shared/shared.module';
import { ImageService } from './image.service';
import { NgHcaptchaModule, CAPTCHA_CONFIG } from 'ng-hcaptcha';
import { NgbCarouselModule, NgbModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  standalone: true,
  selector: 'jhi-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
  imports: [SharedModule, RouterModule, NgHcaptchaModule, NgbCarouselModule],
  providers: [
    {
      provide: CAPTCHA_CONFIG,
      useValue: {
        siteKey: '3b506e74-8bbc-4b56-a81c-4b3e5afe1f90',
      },
    },
  ],
})
export class HomeComponent implements OnInit, OnDestroy {
  account: Account | null = null;
  captchaResuelto = false;
  siteKey = '3b506e74-8bbc-4b56-a81c-4b3e5afe1f90';

  // Configuración del Modal
  modalTitle = '';
  modalMessage = '';
  modalType: 'success' | 'error' = 'success';
  @ViewChild('infoModal') infoModal!: TemplateRef<any>;

  images = signal<any[]>([
    {
      src: 'https://picsum.photos/id/1018/1200/600',
      title: 'Gestión Empresarial',
      desc: 'Administra tus recursos de manera eficiente.',
    },
    {
      src: 'https://picsum.photos/id/1033/1200/600',
      title: 'Seguridad Avanzada',
      desc: 'Protección de datos verificada.',
    },
  ]);

  private readonly destroy$ = new Subject<void>();
  private readonly accountService = inject(AccountService);
  private readonly router = inject(Router);
  private readonly imageService = inject(ImageService);
  private readonly modalService = inject(NgbModal);

  ngOnInit(): void {
    this.accountService
      .getAuthenticationState()
      .pipe(takeUntil(this.destroy$))
      .subscribe(account => (this.account = account));

    this.cargarImagenesBackend();
  }

  cargarImagenesBackend(): void {
    this.imageService.getImages().subscribe({
      next: (data: any[]) => {
        if (data.length > 0) {
          this.images.set(data);
        }
      },
      error() {
        // Silencioso
      },
    });
  }

  // --- MÉTODOS PÚBLICOS (Deben ir antes de los privados) ---

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;

    if (input.files && input.files.length > 0) {
      const file = input.files[0];

      if (!file.type.startsWith('image/')) {
        this.mostrarModal('Archivo Incorrecto', 'Solo se permiten archivos de imagen (JPG, PNG, GIF).', 'error');
        return;
      }

      if (file.size > 5 * 1024 * 1024) {
        this.mostrarModal('Archivo muy pesado', 'La imagen no debe superar los 5MB.', 'error');
        return;
      }

      this.imageService.uploadImage(file).subscribe({
        next: () => {
          this.mostrarModal('¡Excelente!', 'La imagen se ha subido correctamente al servidor.', 'success');
          this.cargarImagenesBackend();
        },
        error: () => {
          this.mostrarModal('Error de Subida', 'No se pudo subir la imagen. Verifica tu conexión.', 'error');
        },
      });
    }
  }

  onCaptchaVerify(): void {
    this.captchaResuelto = true;
  }

  onCaptchaExpired(): void {
    this.captchaResuelto = false;
  }

  login(): void {
    this.router.navigate(['/login']);
  }

  abrirNuevaPestana(): void {
    window.open('https://www.google.com', '_blank');
  }

  provocarError(): void {
    try {
      throw new Error('Test Error');
    } catch (e: unknown) {
      this.router.navigate(['/mi-error']);
    }
  }

  enviarDatosBD(): void {
    this.accountService.guardarDatosEstaticos().subscribe({
      next: () => {
        this.mostrarModal('Operación Exitosa', 'Los datos estáticos se han insertado correctamente en SQL.', 'success');
      },
      error: (err: any) => {
        this.mostrarModal('Fallo en Operación', `No se pudieron insertar los datos. Código: ${String(err.status)}`, 'error');
      },
    });
  }

  // --- MÉTODOS PRIVADOS ---

  private mostrarModal(titulo: string, mensaje: string, tipo: 'success' | 'error'): void {
    this.modalTitle = titulo;
    this.modalMessage = mensaje;
    this.modalType = tipo;
    this.modalService.open(this.infoModal, { centered: true });
  }

  // eslint-disable-next-line @typescript-eslint/member-ordering
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
