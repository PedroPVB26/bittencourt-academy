import { Component, inject } from '@angular/core';
import { AuthService } from '../../services/auth-service';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiError } from '../../../../core/services/models/api-error';

@Component({
  selector: 'app-email-verification-page',
  imports: [],
  templateUrl: './email-verification-page.html',
  styleUrl: './email-verification-page.scss',
})
export class EmailVerificationPage {
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (token) {
      this.authService.verifyEmail(token).subscribe({
        next: (response) => {
          console.log(response.message);
          this.router.navigate(['/login'])
        },
        error: (error: HttpErrorResponse) => {
          const apiError = error.error as ApiError;
          console.log(apiError.message);

          if(apiError.statusCode === 400){
            if(apiError.error === "EMAIL_ALREADY_VERIFIED"){
              console.log("Email already verified");
            }else if(apiError.error === "TOKEN_INVALID"){
              console.log("Invalid token");
            }
          }else if(apiError.statusCode === 401){
            console.log("Expired token");
          }
        }
      });
    }
  }

}
