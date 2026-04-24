import { computed, inject, Injectable, signal } from '@angular/core';
import { catchError, Observable, of, tap } from 'rxjs';
import { AuthResponse, AuthUser, LoginRequest, RegisterRequest } from '../models/api.models';
import { AuthApiService } from './auth-api.service';
import { readStoredAuthSession, StoredAuthSession, writeStoredAuthSession } from './auth-session.storage';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly authApi = inject(AuthApiService);
  private readonly session = signal<StoredAuthSession | null>(readStoredAuthSession());

  readonly user = computed(() => this.session()?.user ?? null);
  readonly token = computed(() => this.session()?.token ?? null);
  readonly isAuthenticated = computed(() => !!this.token());

  register(payload: RegisterRequest): Observable<AuthResponse> {
    return this.authApi.register(payload).pipe(tap((response) => this.persistSession(response)));
  }

  login(payload: LoginRequest): Observable<AuthResponse> {
    return this.authApi.login(payload).pipe(tap((response) => this.persistSession(response)));
  }

  fetchCurrentUser(): Observable<AuthUser> {
    return this.authApi.me().pipe(
      tap((user) => {
        const currentSession = this.session();
        if (!currentSession) {
          return;
        }
        this.updateSession({ ...currentSession, user });
      })
    );
  }

  logout(): Observable<void> {
    if (!this.token()) {
      this.clearSession();
      return of(void 0);
    }

    return this.authApi.logout().pipe(
      catchError(() => of(void 0)),
      tap(() => this.clearSession())
    );
  }

  clearSession(): void {
    this.updateSession(null);
  }

  private persistSession(response: AuthResponse): void {
    this.updateSession({
      token: response.token,
      user: response.user
    });
  }

  private updateSession(session: StoredAuthSession | null): void {
    this.session.set(session);
    writeStoredAuthSession(session);
  }
}
