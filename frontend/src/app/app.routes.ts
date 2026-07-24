import { Routes } from '@angular/router';
import { LoginPage } from './features/auth/pages/login-page/login-page';
import { UserResgistrationPage } from './features/auth/pages/user-resgistration-page/user-resgistration-page';

export const routes: Routes = [
    {
        path: "login",
        component: LoginPage
    },
    {
        path: "register",
        component: UserResgistrationPage
    }
];
