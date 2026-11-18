import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { LoginResponse, Role } from '../shared/models/claim.models';

const STORAGE_KEY = 'chubb.claims.session';

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly session = signal<LoginResponse | null>(this.readSession());

  constructor(private readonly http: HttpClient, private readonly router: Router) {}

  login(username: string, password: string) {
    return this.http
      .post<LoginResponse>(`${environment.claimsApiUrl}/api/auth/login`, { username, password })
      .pipe(
        tap((response) => {
          localStorage.setItem(STORAGE_KEY, JSON.stringify(response));
          this.session.set(response);
        })
      );
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.session.set(null);
    void this.router.navigateByUrl('/login');
  }

  token(): string | null {
    return this.session()?.token ?? null;
  }

  role(): Role | null {
    return this.session()?.role ?? null;
  }

  isLoggedIn(): boolean {
    return !!this.session();
  }

  homeForRole(role: Role): string {
    return role === 'CLAIMANT' ? '/claimant' : '/staff';
  }

  private readSession(): LoginResponse | null {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as LoginResponse;
    } catch {
      return null;
    }
  }
}
