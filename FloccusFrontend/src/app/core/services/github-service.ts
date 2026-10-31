import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GitHubBranchDTO } from '../../features/aplicacao/models/Branches';
import { GithubDTO } from '../../features/git/github/models/GithubModel';
import { GitHubRepository } from '../../features/git/github/models/GitHubRepository';

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
    listarRepositorios(gitId: string): Observable<GitHubRepository[]> {
        return this.http.get<GitHubRepository[]>(`${this.apiUrl}/github/repositories/${gitId}`);
    }
    listarBranches(repositorioId: string, owner: string, repo: string): Observable<GitHubBranchDTO[]> {
        return this.http.get<GitHubBranchDTO[]>(`${this.apiUrl}/github/branches/${repositorioId}/${owner}/${repo}`);
    }
}
