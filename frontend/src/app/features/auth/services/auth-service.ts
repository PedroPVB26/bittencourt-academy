import { inject, Service } from '@angular/core';
import { environment } from '../../../../enviroments/enviroment';
import { HttpClient, HttpParams } from '@angular/common/http';
import { LoginRequest } from '../models/login-request';
import { LoginResponse } from '../models/login-response';
import { UserRegistration } from '../models/user-registration';

@Service()
export class AuthService {
    private api = environment.apiUrl;

    private http: HttpClient = inject(HttpClient);

    login(data: LoginRequest){
        return this.http.post<LoginResponse>(
            `${this.api}/auth/login`, data
        );
    }

    register(data: UserRegistration){
        return this.http.post(
            `${this.api}/auth/register`, data
        );
    }

    continueWithGoogle(): void{
        window.location.href = `${this.api}/oauth2/authorization/google`;
    }

    exchangeCodeForTokens(code: string){
        const params = new HttpParams()
            .set('code', code);
            
        return this.http.post<LoginResponse>(
            `${this.api}/auth/oauth2/exchange`, {}, {params}
        );
    }

    verifyEmail(token: string){
        const params = new HttpParams()
            .set('token', token);

        return this.http.post<MessageResponse>(
            `${this.api}/auth/verify-email`, {}, {params}
        );
    }
}

export interface MessageResponse {
  message: string;
}
