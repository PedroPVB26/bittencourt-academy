import { Component, HostListener, inject } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth-service';
import { TokenService } from '../../../../core/services/token-service';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TokenType } from '../../../../core/services/models/token-type';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiError } from '../../../../core/services/models/api-error';
import { PrimaryInput } from '../../../../shared/components/primary-input/primary-input';
import { PrimaryButton } from '../../../../shared/components/primary-button/primary-button';
import { AuthLayout } from '../../../../shared/layouts/auth-layout/auth-layout';
import { AuthCard } from '../../../../shared/components/auth-card/auth-card';
import { finalize, take } from 'rxjs';
import { APP_ROUTES } from '../../../../core/constants/routes.constants';

@Component({
  selector: 'app-login-page',
  imports: [ReactiveFormsModule, PrimaryInput, PrimaryButton, RouterLink, AuthLayout, AuthCard],
  templateUrl: './login-page.html',
  styleUrl: './login-page.scss',
})
export class LoginPage {
  private authService = inject(AuthService);
  private tokenService = inject(TokenService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private formBuilder = inject(NonNullableFormBuilder);
  isLoginLoading = false;
  isGoogleLoading = false;

  form = this.formBuilder.group({
    email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
    password: ['', [Validators.required, Validators.maxLength(100)]]
  });

  ngOnInit(): void {
    this.route.queryParams.pipe(take(1)).subscribe(params => {
      const exchangeCode = params['code'];

      if (exchangeCode) {
        this.router.navigate([], {
          queryParams: {},
          replaceUrl: true
        });

        this.isGoogleLoading = true;
        this.authService
          .exchangeCodeForTokens(exchangeCode)
          .pipe(
            finalize(() => {
              // console.log('FINALIZE');
              this.isGoogleLoading = false;
              // console.log(this.isGoogleLoading);
            })
          )
          .subscribe({
            next: response => {
              this.tokenService.save(
                TokenType.ACCESS, response.accessToken
              );

              this.tokenService.save(
                TokenType.REFRESH, response.refreshToken
              );

              this.isGoogleLoading = false;

              this.router.navigate([APP_ROUTES.AUTH.REGISTER])
              // Daí redirecionar o usuário para a página inicial
            },

            error: (error: HttpErrorResponse) => {
              const apiError = error.error as ApiError;

              // Codigo enviado inválido
              if (apiError.statusCode === 400) {
                console.log("Invalid code exchange code");
              } else if (apiError.statusCode === 401) {
                console.log("Code exchange code expired");
              }
            }
          })
      }
    }
    )
  }


  // @HostListener('window:pageshow', ['$event'])
  // onPageShow(event: PageTransitionEvent): void {
  //   if (event.persisted) {
  //     this.isGoogleLoading = false;
  //   }
  // }

  continueWithGoogle(): void {
    if (this.isGoogleLoading) {
      return;
    }
    this.isGoogleLoading = true;
    this.authService.continueWithGoogle();
  }

  submit(): void {
    if (this.form.invalid) {
      return;
    }

    this.isLoginLoading = true;

    this.authService
      .login(this.form.getRawValue())
      .pipe(
        finalize(() => {
          this.isLoginLoading = false;
        })
      )
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

          if (apiError.statusCode === 401) {
            console.log("Invalid password or email");
          } else if (apiError.statusCode === 403) {
            console.log("Please verify your email address before signing in");
          }
        }
      })
  }
}
