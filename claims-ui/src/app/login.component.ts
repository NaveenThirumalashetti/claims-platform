import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from './core/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  template: `
    <section>
      <h2>Sign in</h2>
      <form (ngSubmit)="submit()">
        <label>Username <input name="u" [(ngModel)]="username" required placeholder="e.g. claimant-1, officer-1, manager-1"></label>
        <label>Password <input name="p" type="password" [(ngModel)]="password" required></label>
        <button type="submit">Sign in</button>
        @if (error()) { <p class="error">{{ error() }}</p> }
      </form>
    </section>
  `,
})
export class LoginComponent {
  private auth = inject(AuthService);
  private router = inject(Router);
  username = '';
  password = 'password';
  error = signal('');

  submit() {
    this.auth.login(this.username, this.password).subscribe({
      next: s => this.router.navigate([s.role === 'CLAIMANT' ? '/claimant' : '/officer']),
      error: () => this.error.set('Invalid username or password'),
    });
  }
}