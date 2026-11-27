import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs';

export interface Session { token: string; userId: string; role: string; expiresInSeconds: number; }

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  readonly user = signal<Session | null>(JSON.parse(sessionStorage.getItem('session') ?? 'null'));

  get token(): string | null { return this.user()?.token ?? null; }

  login(username: string, password: string) {
    return this.http.post<Session>('/api/auth/login', { username, password }).pipe(
      tap(s => { sessionStorage.setItem('session', JSON.stringify(s)); this.user.set(s); }));
  }

  logout() {
    sessionStorage.removeItem('session');
    this.user.set(null);
    this.router.navigate(['/login']);
  }
}