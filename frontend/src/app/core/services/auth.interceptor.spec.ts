import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AUTH_SESSION_STORAGE_KEY } from './auth-session.storage';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    localStorage.setItem(
      AUTH_SESSION_STORAGE_KEY,
      JSON.stringify({
        token: 'stored-token',
        user: { id: 'user-1', name: 'Aeon', email: 'aeon@example.com', createdAt: '2026-04-24T00:00:00Z' }
      })
    );

    TestBed.configureTestingModule({
      providers: [provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting()]
    });

    http = TestBed.inject(HttpClient);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    localStorage.removeItem(AUTH_SESSION_STORAGE_KEY);
    httpTestingController.verify();
  });

  it('adds the bearer token to api requests', () => {
    http.get('/api/v1/projects').subscribe();

    const request = httpTestingController.expectOne('/api/v1/projects');
    expect(request.request.headers.get('Authorization')).toBe('Bearer stored-token');
    request.flush([]);
  });

  it('does not add auth headers to non-api requests', () => {
    http.get('/assets/example.json').subscribe();

    const request = httpTestingController.expectOne('/assets/example.json');
    expect(request.request.headers.has('Authorization')).toBeFalse();
    request.flush({});
  });
});
