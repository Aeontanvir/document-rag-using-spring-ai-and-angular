import { AuthResponse } from '../models/api.models';

export const AUTH_SESSION_STORAGE_KEY = 'document-rag-auth-session';

export interface StoredAuthSession {
  token: string;
  user: AuthResponse['user'];
}

export function readStoredAuthSession(): StoredAuthSession | null {
  if (typeof localStorage === 'undefined') {
    return null;
  }

  const storedValue = localStorage.getItem(AUTH_SESSION_STORAGE_KEY);
  if (!storedValue) {
    return null;
  }

  try {
    const session = JSON.parse(storedValue) as StoredAuthSession;
    if (!session?.token || !session?.user) {
      return null;
    }
    return session;
  } catch {
    return null;
  }
}

export function readStoredAuthToken(): string | null {
  return readStoredAuthSession()?.token ?? null;
}

export function writeStoredAuthSession(session: StoredAuthSession | null): void {
  if (typeof localStorage === 'undefined') {
    return;
  }

  if (!session) {
    localStorage.removeItem(AUTH_SESSION_STORAGE_KEY);
    return;
  }

  localStorage.setItem(AUTH_SESSION_STORAGE_KEY, JSON.stringify(session));
}
