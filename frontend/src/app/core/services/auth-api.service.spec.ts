import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AuthApiService } from './auth-api.service';

describe('AuthApiService', () => {
  let service: AuthApiService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AuthApiService, provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(AuthApiService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('registers a new user', () => {
    service.register({ name: 'Aeon', email: 'aeon@example.com', password: 'password123' }).subscribe();

    const request = httpTestingController.expectOne('/api/v1/auth/register');
    expect(request.request.method).toBe('POST');
    expect(request.request.body.name).toBe('Aeon');
    request.flush({ token: 'token-1', user: { id: 'user-1' } });
  });

  it('logs in an existing user', () => {
    service.login({ email: 'aeon@example.com', password: 'password123' }).subscribe();

    const request = httpTestingController.expectOne('/api/v1/auth/login');
    expect(request.request.method).toBe('POST');
    expect(request.request.body.email).toBe('aeon@example.com');
    request.flush({ token: 'token-1', user: { id: 'user-1' } });
  });

  it('loads the current user profile', () => {
    service.me().subscribe();

    const request = httpTestingController.expectOne('/api/v1/auth/me');
    expect(request.request.method).toBe('GET');
    request.flush({ id: 'user-1' });
  });

  it('logs out the current user', () => {
    service.logout().subscribe();

    const request = httpTestingController.expectOne('/api/v1/auth/logout');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });
});
