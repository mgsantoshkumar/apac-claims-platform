import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([]), AuthService]
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('stores session on login and routes claimants home', () => {
    let roleHome = '';
    service.login('claimant1', 'password').subscribe((session) => {
      roleHome = service.homeForRole(session.role);
    });

    const req = http.expectOne(`${environment.claimsApiUrl}/api/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush({
      token: 'tok',
      userId: 'u-1',
      username: 'claimant1',
      displayName: 'Aisha Tan',
      role: 'CLAIMANT'
    });

    expect(service.isLoggedIn()).toBeTrue();
    expect(service.token()).toBe('tok');
    expect(roleHome).toBe('/claimant');
    expect(service.homeForRole('ADJUSTER')).toBe('/staff');
  });

  it('clears session on logout', () => {
    service.login('claimant1', 'password').subscribe();
    http.expectOne(`${environment.claimsApiUrl}/api/auth/login`).flush({
      token: 'tok',
      userId: 'u-1',
      username: 'claimant1',
      displayName: 'Aisha Tan',
      role: 'CLAIMANT'
    });

    expect(service.isLoggedIn()).toBeTrue();
    service.logout();
    expect(service.isLoggedIn()).toBeFalse();
    expect(localStorage.getItem('chubb.claims.session')).toBeNull();
  });
});
