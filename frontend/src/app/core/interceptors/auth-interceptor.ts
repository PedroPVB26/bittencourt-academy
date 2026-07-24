import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { TokenService } from '../services/token-service';
import { TokenType } from '../services/models/token-type';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  
  const tokenService = inject(TokenService);
  const accessToken = tokenService.get(TokenType.ACCESS);

  if(!accessToken){
    return next(req);
  }

  const authReq = req.clone({
    setHeaders: {
      Authorization: `Bearer ${accessToken}`
    }
  });

  return next(req);
};
