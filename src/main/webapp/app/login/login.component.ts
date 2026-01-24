import { Component, OnInit, AfterViewInit, ElementRef, ViewChild, inject, signal } from '@angular/core';
import { FormGroup, FormControl, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { LoginService } from 'app/login/login.service';
import { AccountService } from 'app/core/auth/account.service';
import SharedModule from 'app/shared/shared.module';

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

  // 1. SEÑAL PARA CONTROLAR EL MODAL
  showSuccessModal = signal(false);

  // 2. CONFIGURACIÓN DEL FORMULARIO
  loginForm = new FormGroup({
    // Credenciales
    username: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(50)] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(50)] }),
    rememberMe: new FormControl(false, { nonNullable: true, validators: [Validators.required] }),

    // Datos Adicionales (Corregidos)
    fechaNacimiento: new FormControl('', [Validators.required]),
    salario: new FormControl(null, [Validators.required, Validators.min(0.01), Validators.max(999999999)]),
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
    const credentials = {
      username: rawData.username,
      password: rawData.password,
      rememberMe: rawData.rememberMe,
    };

    this.loginService.login(credentials).subscribe({
      next: () => {
        // 3. EN LUGAR DE REDIRIGIR DE INMEDIATO, MOSTRAMOS EL MODAL
        this.authenticationError.set(false);
        this.showSuccessModal.set(true);
      },
      error: () => this.authenticationError.set(true),
    });
  }

  // 4. ESTA FUNCIÓN CIERRA EL MODAL Y REDIRIGE AL HOME
  cerrarModalYRedirigir(): void {
    this.showSuccessModal.set(false);
    if (!this.router.getCurrentNavigation()) {
      this.router.navigate(['']);
    }
  }

  // Herramientas de desarrollo
  enviarDatosBD(): void {
    this.accountService.guardarDatosEstaticos().subscribe({
      next: () => alert('✅ ¡Éxito! Datos insertados en SQL. Revisa tu base de datos.'),
      error(err) {
        console.error(err);
        // eslint-disable-next-line @typescript-eslint/restrict-plus-operands
        alert('❌ Error: ' + err.status + ' - Revisa la consola o reinicia el backend.');
      },
    });
  }
}
