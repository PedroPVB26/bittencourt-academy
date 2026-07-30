import { Component, inject } from '@angular/core';
import { AbstractControl, NonNullableFormBuilder, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth-service';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiError } from '../../../../core/services/models/api-error';
import { APP_ROUTES } from '../../../../core/constants/routes.constants';
import { PrimaryInput } from "../../../../shared/components/primary-input/primary-input";
import { PrimaryButton } from '../../../../shared/components/primary-button/primary-button';

@Component({
  selector: 'app-user-resgistration-page',
  imports: [ReactiveFormsModule, PrimaryInput, PrimaryButton, RouterLink],
  templateUrl: './user-resgistration-page.html',
  styleUrl: './user-resgistration-page.scss',
})
export class UserResgistrationPage {
  private authService = inject(AuthService);
  private router = inject(Router);
  private formBuilder = inject(NonNullableFormBuilder);

  form = this.formBuilder.group(
    {
      fullName: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(120)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
      password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(100), Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/)]],
      confirmPassword: ['', [Validators.required]]
    },
    {
      validators: passwordMatchValidator()
    }
  );

  submit(): void {
    if (this.form.invalid) {
      return;
    }

    const { confirmPassword, ...userData } = this.form.getRawValue();

    this.authService
      .register(userData)
      .subscribe({
        next: () => {
          console.log("Cadastro realizado com sucesso, verifique seu e-mail para ativar a sua conta");
          this.router.navigate([APP_ROUTES.AUTH.LOGIN])
        },

        error: (error: HttpErrorResponse) => {
          const apiError = error.error as ApiError;
          console.log(apiError.message);

          if (apiError.statusCode === 409) {
            const emailControl = this.form.controls.email;
            emailControl.setErrors({ emailAlreadyExists: true });
            emailControl.markAsTouched();
          }
        }
      })
  }

}

function passwordMatchValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');

    if (!password || !confirmPassword) {
      return null;
    }

    if (password.value !== confirmPassword.value) {
      confirmPassword.setErrors({...confirmPassword.errors, passwordMismatch: true});
      return { passwordMismatch: true };
    }

    return null;
  };
}