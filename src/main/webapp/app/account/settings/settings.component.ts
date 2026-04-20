import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';

import SharedModule from 'app/shared/shared.module';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';

const initialAccount: Account = {} as Account;

@Component({
  selector: 'jhi-settings',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
  templateUrl: './settings.component.html',
})
export default class SettingsComponent implements OnInit {
  success = signal(false);

  settingsForm = new FormGroup({
    firstName: new FormControl(initialAccount.firstName, {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(1), Validators.maxLength(50)],
    }),
    lastName: new FormControl(initialAccount.lastName, {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(1), Validators.maxLength(50)],
    }),
    email: new FormControl(initialAccount.email, {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(5), Validators.maxLength(254), Validators.email],
    }),
    langKey: new FormControl(initialAccount.langKey, { nonNullable: true }),

    activated: new FormControl(initialAccount.activated, { nonNullable: true }),
    authorities: new FormControl(initialAccount.authorities, { nonNullable: true }),
    imageUrl: new FormControl(initialAccount.imageUrl, { nonNullable: true }),
    login: new FormControl(initialAccount.login, { nonNullable: true }),
  });

  private readonly accountService = inject(AccountService);

  ngOnInit(): void {
    this.accountService.identity().subscribe({
      next: (account) => {
        if (account) {
          this.settingsForm.patchValue(account);
          this.settingsForm.get('email')?.disable();
        }
      },
      error: (err) => {
        console.error('Error al cargar identidad:', err);
      }
    });
  }

  save(): void {
    this.success.set(false);

    const account = this.settingsForm.getRawValue();
    
    // Asegurar que login y email estén presentes
    if (!account.login || !account.email) {
      console.warn('Login o Email ausentes en el formulario. Abortando guardado.');
      return;
    }

    this.accountService.save(account).subscribe({
      next: () => {
        this.success.set(true);
        this.accountService.authenticate(account);
      },
      error: (err) => {
        console.error('Error al guardar perfil:', err);
      }
    });
  }

  onFileSelected(event: Event): void {
    const target = event.target as HTMLInputElement;
    if (target.files && target.files.length > 0) {
      const file = target.files[0];
      
      // Validación básica de tamaño (5MB)
      if (file.size > 5 * 1024 * 1024) {
        alert('La imagen es demasiado grande. Máximo 5MB.');
        return;
      }

      const reader = new FileReader();
      reader.onload = () => {
        this.settingsForm.patchValue({
          imageUrl: reader.result as string,
        });
      };
      reader.readAsDataURL(file);
    }
  }
}
