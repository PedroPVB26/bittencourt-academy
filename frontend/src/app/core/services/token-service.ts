import { Service } from '@angular/core';
import { TokenType } from './models/token-type';

@Service()
export class TokenService {

    save(type: TokenType, token: string): void {
        localStorage.setItem(type, token);
    }

    get(type: TokenType): string | null {
        return localStorage.getItem(type);
    }

    clear(type: TokenType): void {
        localStorage.removeItem(type);
    }

    clearAll(): void {
        Object.values(TokenType).forEach(type => {
            localStorage.removeItem(type);
        });
    }
}
