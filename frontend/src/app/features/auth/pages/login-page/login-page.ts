import { Component, inject } from '@angular/core';
import { FormBuilder, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { email } from '@angular/forms/signals';
import { AuthService } from '../../services/auth-service';
import { TokenService } from '../../../../core/services/token-service';
import { Router } from '@angular/router';
import { TokenType } from '../../../../core/services/models/token-type';

@Component({
  selector: 'app-login-page',
  imports: [ReactiveFormsModule],
  templateUrl: './login-page.html',
  styleUrl: './login-page.scss',
})
export class LoginPage {
  private authService = inject(AuthService);
  private tokenService = inject(TokenService);
  private router = inject(Router);
  private formBuilder = inject(NonNullableFormBuilder);

  form = this.formBuilder.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  submit(): void {
    if (this.form.invalid) {
      return;
    }

    this.authService
      .login(this.form.getRawValue())
      .subscribe({
        next: response => {
          this.tokenService.save(
            TokenType.ACCESS, response.accessToken
          );

          this.tokenService.save(
            TokenType.REFRESH, response.refreshToken
          );

          // Para onde direcionar o usuário depois que ele tiver logado com sucesso
        },

        error: () => {
          console.log("Email ou senha incorretos")
        }
      })
  }
}
