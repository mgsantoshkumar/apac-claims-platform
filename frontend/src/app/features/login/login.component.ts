import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  username = 'claimant1';
  password = 'password';
  readonly error = signal<string | null>(null);
  readonly loading = signal(false);

  readonly demos = [
    { user: 'claimant1', role: 'Claimant' },
    { user: 'adjuster1', role: 'Adjuster' },
    { user: 'manager1', role: 'Manager' }
  ];

  constructor(private readonly auth: AuthService, private readonly router: Router) {
    if (auth.isLoggedIn()) {
      void router.navigateByUrl(auth.homeForRole(auth.role()!));
    }
  }

  useDemo(user: string): void {
    this.username = user;
    this.password = 'password';
  }

  submit(): void {
    this.loading.set(true);
    this.error.set(null);
    this.auth.login(this.username, this.password).subscribe({
      next: (session) => {
        this.loading.set(false);
        void this.router.navigateByUrl(this.auth.homeForRole(session.role));
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Login failed. Use a demo account (password: password).');
      }
    });
  }
}
