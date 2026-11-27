import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const authed = auth.token ? req.clone({ setHeaders: { Authorization: `Bearer ${auth.token}` } }) : req;
  return next(authed).pipe(
    catchError(err => {
      // expired or invalid token: back to the login screen
      if (err.status === 401 && !req.url.includes('/api/auth/login')) auth.logout();
      return throwError(() => err);
    }));
};