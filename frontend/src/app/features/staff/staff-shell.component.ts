import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-staff-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatToolbarModule, MatButtonModule],
  template: `
    <mat-toolbar class="bar">
      <span class="brand">Chubb APAC Claims</span>
      <a mat-button routerLink="/staff" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">Inbox</a>
      <a mat-button routerLink="/staff/workload" routerLinkActive="active">Workload</a>
      <span class="spacer"></span>
      <span class="user">{{ auth.session()?.displayName }} · {{ auth.session()?.role }}</span>
      <button mat-button type="button" (click)="auth.logout()">Sign out</button>
    </mat-toolbar>
    <main class="content">
      <router-outlet />
    </main>
  `,
  styles: `
    .bar { background: #102a43; color: #f0f4f8; gap: 0.25rem; }
    .brand { font-family: 'Source Serif 4', Georgia, serif; margin-right: 1rem; font-size: 1.2rem; }
    a { color: inherit; }
    .active { background: rgba(255,255,255,0.12); }
    .spacer { flex: 1; }
    .user { opacity: 0.85; margin-right: 0.5rem; }
    .content { padding: 1.5rem; max-width: 1200px; margin: 0 auto; }
  `
})
export class StaffShellComponent {
  constructor(readonly auth: AuthService) {}
}
