import { Routes } from '@angular/router';
import { LoginComponent } from './login.component';
import { ClaimantComponent } from './claimant.component';
import { OfficerComponent } from './officer.component';
import { roleGuard } from './core/role.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'claimant', component: ClaimantComponent, canActivate: [roleGuard('CLAIMANT')] },
  { path: 'officer', component: OfficerComponent, canActivate: [roleGuard('OFFICER', 'MANAGER')] },
  { path: '**', redirectTo: 'login' },
];