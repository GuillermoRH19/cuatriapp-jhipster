import { Component, OnInit, AfterViewInit, ElementRef, ViewChild, inject, signal } from '@angular/core';
import { FormGroup, FormControl, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { LoginService } from 'app/login/login.service';
import { AccountService } from 'app/core/auth/account.service';
import SharedModule from 'app/shared/shared.module';
import dayjs from 'dayjs/esm';

// VALIDADOR PERSONALIZADO: +18 años y no futuro
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
    username: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(50)] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(50)] }),
    rememberMe: new FormControl(false, { nonNullable: true, validators: [Validators.required] }),

    // Validadores aplicados
    fechaNacimiento: new FormControl('', [Validators.required, validarEdad]),
    salario: new FormControl(null, [Validators.required, Validators.min(0.01), Validators.max(15000)]),
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
    this.loginService.login({ username: rawData.username, password: rawData.password, rememberMe: rawData.rememberMe }).subscribe({
      next: () => {
        this.authenticationError.set(false);
        this.showSuccessModal.set(true);
      },
      error: () => this.authenticationError.set(true),
    });
  }

  cerrarModalYRedirigir(): void {
    this.showSuccessModal.set(false);
    if (!this.router.getCurrentNavigation()) this.router.navigate(['']);
  }

  enviarDatosBD(): void {
    this.accountService.guardarDatosEstaticos().subscribe({
      next: () => alert('✅ Datos insertados.'),
      // eslint-disable-next-line @typescript-eslint/restrict-plus-operands
      error: err => alert('❌ Error: ' + err.status),
    });
  }
}
