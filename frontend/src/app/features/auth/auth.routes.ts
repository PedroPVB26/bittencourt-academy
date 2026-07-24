import { Routes } from '@angular/router';

import { LoginPage } from './pages/login-page/login-page';
import { UserResgistrationPage } from './pages/user-resgistration-page/user-resgistration-page';
import { EmailVerificationPage } from './pages/email-verification-page/email-verification-page';

export const authRoutes: Routes = [
  {
    path: 'login',
    component: LoginPage
  },
  {
    path: 'register',
    component: UserResgistrationPage
  },
  {
    path: 'verify-email',
    component: EmailVerificationPage
  }
];