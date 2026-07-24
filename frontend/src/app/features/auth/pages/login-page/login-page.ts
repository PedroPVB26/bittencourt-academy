import { Component, inject } from '@angular/core';
import { FormBuilder, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { email } from '@angular/forms/signals';
import { AuthService } from '../../services/auth-service';
import { TokenService } from '../../../../core/services/token-service';
import { Router } from '@angular/router';
import { TokenType } from '../../../../core/services/models/token-type';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiError } from '../../../../core/services/models/api-error';

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
    email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(100)]]
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
          console.log("Login Success");
        },

        error: (error: HttpErrorResponse) => {
          const apiError = error.error as ApiError;
          console.log(apiError.message);
          
          if(apiError.statusCode === 401){
            console.log("Invalid password or email");
          }else if(apiError.statusCode === 403){
            console.log("Please verify your email address before signing in");
          }
        }
      })
  }
}
