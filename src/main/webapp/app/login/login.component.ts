import { Component, OnInit, AfterViewInit, ElementRef, ViewChild, inject, signal } from '@angular/core';
import { FormGroup, FormControl, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { LoginService } from 'app/login/login.service';
import { AccountService } from 'app/core/auth/account.service';
import SharedModule from 'app/shared/shared.module';
import dayjs from 'dayjs/esm';

// Función para validar edad fuera de la clase
function validarEdad(control: AbstractControl): ValidationErrors | null {
  if (!control.value) return null;
  const fechaNacimiento = dayjs(control.value);
  const hoy = dayjs();

  if (fechaNacimiento.isAfter(hoy)) return { fechaFutura: true };
  if (hoy.diff(fechaNacimiento, 'year') < 18) return { menorDeEdad: true };
  return null;
}

@Component({
  standalone: true,
  selector: 'jhi-login',
  imports: [SharedModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export default class LoginComponent implements OnInit, AfterViewInit {
  @ViewChild('username', { static: false }) username?: ElementRef;

  authenticationError = signal(false);
  showSuccessModal = signal(false);

  loginForm = new FormGroup({
    // Campos básicos
    username: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(50)] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(50)] }),
    rememberMe: new FormControl(false, { nonNullable: true, validators: [Validators.required] }),

    // Campos adicionales con validaciones
    fechaNacimiento: new FormControl('', [Validators.required, validarEdad]),

    // Sueldo: Mínimo 0 (no negativos), Máximo 15000
    salario: new FormControl(null, [Validators.required, Validators.min(0), Validators.max(15000)]),

    contactoEmail: new FormControl('', [Validators.required, Validators.email, Validators.maxLength(100)]),
    departamento: new FormControl(null, [Validators.required]),
  });

  private readonly loginService = inject(LoginService);
  private readonly accountService = inject(AccountService);
  private readonly router = inject(Router);

  ngOnInit(): void {
    this.authenticationError.set(false);
  }

  ngAfterViewInit(): void {
    this.username?.nativeElement?.focus();
  }

  login(): void {
    const rawData = this.loginForm.getRawValue();
    this.loginService
      .login({
        username: rawData.username,
        password: rawData.password,
        rememberMe: rawData.rememberMe,
      })
      .subscribe({
        next: () => {
          this.authenticationError.set(false);
          this.showSuccessModal.set(true); // Mostrar modal en lugar de redirigir directo
        },
        error: () => this.authenticationError.set(true),
      });
  }

  cerrarModalYRedirigir(): void {
    this.showSuccessModal.set(false);
    // Redirigir al home si no hay navegación pendiente
    if (!this.router.getCurrentNavigation()) {
      this.router.navigate(['']);
    }
  }

  enviarDatosBD(): void {
    this.accountService.guardarDatosEstaticos().subscribe({
      next: () => alert('✅ Datos insertados correctamente en la BD.'),
      // eslint-disable-next-line @typescript-eslint/restrict-plus-operands
      error: err => alert('❌ Error al insertar: ' + err.status),
    });
  }
}
