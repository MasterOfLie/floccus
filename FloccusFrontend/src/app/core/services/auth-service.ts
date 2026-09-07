import { inject, Injectable, signal } from '@angular/core';
import { AuthResponse, LoginRequest, RegisterRequest } from '../config/auth/models/AuthModel';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {

    private http = inject(HttpClient);
    private router = inject(Router);

    private readonly apiUrl = '/api/auth';
    currentUser = signal<AuthResponse | null>(this.obterUsuarioDoCache());

    login(dados: LoginRequest) {
        return this.http.post<AuthResponse>(`${this.apiUrl}/login`, dados).pipe(
            tap((resposta) => {
                localStorage.setItem('floccus_user', JSON.stringify(resposta));
                localStorage.setItem('floccus_token', resposta.token);
                this.currentUser.set(resposta);
            })
        );
    }

    register(dados: RegisterRequest) {
        return this.http.post<AuthResponse>(`${this.apiUrl}/register`, dados).pipe(
            tap((resposta) => {
                localStorage.setItem('floccus_user', JSON.stringify(resposta));
                localStorage.setItem('floccus_token', resposta.token);
                this.currentUser.set(resposta);
            })
        );
    }

    logout() {
        localStorage.removeItem('floccus_user');
        localStorage.removeItem('floccus_token');
        this.currentUser.set(null);
        this.router.navigate(['/login']);
    }

    private obterUsuarioDoCache(): AuthResponse | null {
        const userStr = localStorage.getItem('floccus_user');
        return userStr ? JSON.parse(userStr) : null;
    }
}
