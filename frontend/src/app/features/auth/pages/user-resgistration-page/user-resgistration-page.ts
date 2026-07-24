import { Component, inject } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth-service';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiError } from '../../../../core/services/models/api-error';

@Component({
  selector: 'app-user-resgistration-page',
  imports: [ReactiveFormsModule],
  templateUrl: './user-resgistration-page.html',
  styleUrl: './user-resgistration-page.scss',
})
export class UserResgistrationPage {
  private authService = inject(AuthService);
  private router = inject(Router);
  private formBuilder = inject(NonNullableFormBuilder);

  form = this.formBuilder.group({
    fullName: ['', Validators.required, Validators.minLength(3), Validators.maxLength(120)],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(100)]]
  });

  submit(): void {
    if (this.form.invalid) {
      return;
    }

    this.authService
      .register(this.form.getRawValue())
      .subscribe({
        next: () => {
          console.log("Cadastro realizado com sucesso, verifique seu e-mail para ativar a sua conta");
          this.router.navigate(['/login'])
        },

        error: (error: HttpErrorResponse) => {
          const apiError = error.error as ApiError;
          console.log(apiError.message);

          if(apiError.statusCode === 409){
            console.log("Email already in use");
          }
        }
      })
  }
}
