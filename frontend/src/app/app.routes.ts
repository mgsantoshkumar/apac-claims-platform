import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/auth.guard';
import { LoginComponent } from './features/login/login.component';
import { ClaimantShellComponent } from './features/claimant/claimant-shell.component';
import { MyClaimsComponent } from './features/claimant/my-claims.component';
import { ReportClaimComponent } from './features/claimant/report-claim.component';
import { ClaimDetailComponent } from './features/claimant/claim-detail.component';
import { StaffShellComponent } from './features/staff/staff-shell.component';
import { StaffInboxComponent } from './features/staff/staff-inbox.component';
import { StaffClaimComponent } from './features/staff/staff-claim.component';
import { WorkloadComponent } from './features/staff/workload.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: 'login', component: LoginComponent },
  {
    path: 'claimant',
    canActivate: [authGuard, roleGuard(['CLAIMANT'])],
    component: ClaimantShellComponent,
    children: [
      { path: '', component: MyClaimsComponent },
      { path: 'new', component: ReportClaimComponent },
      { path: 'claims/:id', component: ClaimDetailComponent }
    ]
  },
  {
    path: 'staff',
    canActivate: [authGuard, roleGuard(['ADJUSTER', 'MANAGER'])],
    component: StaffShellComponent,
    children: [
      { path: '', component: StaffInboxComponent },
      { path: 'workload', component: WorkloadComponent },
      { path: 'claims/:id', component: StaffClaimComponent }
    ]
  },
  { path: '**', redirectTo: 'login' }
];
