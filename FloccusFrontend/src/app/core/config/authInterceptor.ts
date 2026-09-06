import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth-service';
import { inject } from '@angular/core';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const token = localStorage.getItem('floccus_token');

    if (!token) {
        return next(req);
    }

    let expirado = false;

    try {
        const payload = JSON.parse(atob(token.split('.')[1]));

        expirado = Date.now() >= payload.exp * 1000;

    } catch {
        expirado = true;
    }

    if (expirado) {
        authService.logout();

        return throwError(() => new Error('Token inválido ou expirado'));
    }

    const requisicaoClonada = req.clone({
        setHeaders: {
            Authorization: `Bearer ${token}`
        }
    });

    return next(requisicaoClonada).pipe(
        catchError((error: HttpErrorResponse) => {

            if (error.status === 401) {
                authService.logout();
            }

            return throwError(() => error);
        })
    );
};