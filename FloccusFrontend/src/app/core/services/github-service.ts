import { Injectable, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { GithubDTO } from '../../features/git/github/models/GithubModel';

@Injectable({
    providedIn: 'root'
})
export class GithubService {

    constructor(private http: HttpClient) { }
    private readonly apiUrl = '/api';

    conectarGitHub(): Observable<{ manifest: string, state: string }> {
        return this.http.get<{ manifest: string, state: string }>(
            `${this.apiUrl}/github/connect`
        );
    }
    salvarInstalacao(installationId: string, githubId: string): Observable<{ message: string }> {
        return this.http.post<{ message: string }>(
            `${this.apiUrl}/github/install`,
            null,
            {
                params: {
                    installation_id: installationId,
                    githubId: githubId
                }
            }
        );
    }
    listar(pagina: number, tamanho: number): Observable<PageResponse<GithubDTO>> {
        return this.http.get<PageResponse<GithubDTO>>(
            `${this.apiUrl}/github`,
            {
                params: {
                    page: pagina,
                    size: tamanho
                }
            }
        );
    }
    remover(id: string): Observable<{ message: string }> {
        return this.http.delete<{ message: string }>(
            `${this.apiUrl}/github/${id}`
        );
    }
}

