import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <header class="bar">
      <strong>Claims Platform</strong>
      @if (auth.user(); as u) {
        <span>{{ u.userId }} ({{ u.role }})</span>
        <button (click)="auth.logout()">Log out</button>
      }
    </header>
    <main><router-outlet /></main>
  `,
})
export class AppComponent {
  auth = inject(AuthService);
}