import { HttpInterceptorFn } from '@angular/common/http';
import { readStoredAuthToken } from './auth-session.storage';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const token = readStoredAuthToken();

  if (!token || !request.url.startsWith('/api/')) {
    return next(request);
  }

  return next(
    request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    })
  );
};
