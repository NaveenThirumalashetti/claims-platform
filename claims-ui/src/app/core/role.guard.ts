import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const roleGuard = (...roles: string[]): CanActivateFn => () => {
  const user = inject(AuthService).user();
  return user && roles.includes(user.role) ? true : inject(Router).parseUrl('/login');
};