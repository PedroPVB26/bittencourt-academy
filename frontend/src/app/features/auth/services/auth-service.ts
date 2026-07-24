import { inject, Service } from '@angular/core';
import { environment } from '../../../../enviroments/enviroment';
import { HttpClient } from '@angular/common/http';
import { LoginRequest } from '../models/login-request';
import { LoginResponse } from '../models/login-response';

@Service()
export class AuthService {
    private api = environment.apiUrl;

    private http: HttpClient = inject(HttpClient);

    login(data: LoginRequest){
        return this.http.post<LoginResponse>(
            `${this.api}/auth/login`, data
        );
    }
}
