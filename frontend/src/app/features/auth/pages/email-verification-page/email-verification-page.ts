import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth-service';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiError } from '../../../../core/services/models/api-error';
import { APP_ROUTES } from '../../../../core/constants/routes.constants';
import { PrimaryButton } from '../../../../shared/components/primary-button/primary-button';
import { PrimaryInput } from '../../../../shared/components/primary-input/primary-input';
import { AuthCard } from '../../../../shared/components/auth-card/auth-card';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-email-verification-page',
  imports: [ReactiveFormsModule, PrimaryButton, PrimaryInput, AuthCard],
  templateUrl: './email-verification-page.html',
  styleUrl: './email-verification-page.scss',
})
export class EmailVerificationPage {
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private formBuilder = inject(NonNullableFormBuilder);

  status = signal<VerificationStatus>('loading');
  isResending = signal(false);
  resendMessage = signal('');

  form = this.formBuilder.group({
    email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]]
  });

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (!token) {
      this.status.set('invalid');
      return;
    }

    this.authService.verifyEmail(token).subscribe({
      next: () => this.router.navigate([APP_ROUTES.AUTH.LOGIN]),
      error: (error: HttpErrorResponse) => this.handleVerificationError(error)
    });
  }

  goToLogin(): void {
    this.router.navigate([APP_ROUTES.AUTH.LOGIN]);
  }

  resendVerificationEmail(): void {
    if (this.form.invalid || this.isResending()) {
      this.form.markAllAsTouched();
      return;
    }

    this.isResending.set(true);
    this.resendMessage.set('');

    this.authService.resendVerificationEmail(this.form.getRawValue().email).pipe(
      finalize(() => this.isResending.set(false))
    ).subscribe({
      next: () => this.resendMessage.set('A new verification link has been sent to your email.'),
      error: () => this.resendMessage.set('We could not resend the verification link. Please try again.')
    });
  }

  private handleVerificationError(error: HttpErrorResponse): void {
    const apiError = error.error as ApiError;

    if (apiError?.statusCode === 400 && apiError.error === 'EMAIL_ALREADY_VERIFIED') {
      this.status.set('already-verified');
    } else if (apiError?.statusCode === 400 && apiError.error === 'TOKEN_INVALID') {
      this.status.set('invalid');
    } else if (apiError?.statusCode === 401) {
      this.status.set('expired');
    } else {
      this.status.set('invalid');
    }
  }

}

type VerificationStatus = 'loading' | 'already-verified' | 'invalid' | 'expired';
