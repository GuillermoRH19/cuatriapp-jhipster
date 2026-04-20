/* eslint-disable @typescript-eslint/member-ordering */
import { Component, OnInit, AfterViewInit, ElementRef, ViewChild, inject } from '@angular/core';
import { FormGroup, FormControl, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

import { LoginService } from 'app/login/login.service';
import { AccountService } from 'app/core/auth/account.service';
import SharedModule from 'app/shared/shared.module';
import { RegistroPublicoComponent } from './registro-publico/registro-publico.component';

@Component({
  selector: 'jhi-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, RegistroPublicoComponent, SharedModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export default class LoginComponent implements OnInit, AfterViewInit {
  @ViewChild('username', { static: false }) username!: ElementRef;

  private readonly loginService: LoginService = inject(LoginService);
  private readonly accountService: AccountService = inject(AccountService);
  private readonly router: Router = inject(Router);

  authenticationError = false;
  isLoadingFetch = false;

  loginForm = new FormGroup({
    username: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    rememberMe: new FormControl(false, { nonNullable: true }),
  });

  ngOnInit(): void {
    this.accountService.identity().subscribe(() => {
      if (this.accountService.isAuthenticated()) {
        this.router.navigate(['/dashboard/inicio']);
      }
    });
  }

  ngAfterViewInit(): void {
    if (this.username) {
      this.username.nativeElement.focus();
    }
  }

  login(): void {
    this.authenticationError = false;
    
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoadingFetch = true;
    const { username, password, rememberMe } = this.loginForm.getRawValue();

    this.loginService.login({ username, password, rememberMe }).subscribe({
      next: () => {
        this.isLoadingFetch = false;
        this.authenticationError = false;
        this.router.navigate(['/dashboard/inicio']);
      },
      error: () => {
        this.authenticationError = true;
        this.isLoadingFetch = false;
      },
    });
  }
}
