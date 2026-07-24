import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { TokenService } from '../services/token-service';
import { TokenType } from '../services/models/token-type';

export const authGuardGuard: CanActivateFn = (route, state) => {
  const tokenService = inject(TokenService);
  const router = inject(Router);

  if(tokenService.get(TokenType.ACCESS)){
    return true;
  }


  return router.createUrlTree(['/login']);
};
