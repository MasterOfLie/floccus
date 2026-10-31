import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Template, TemplateDTO } from '../../shared/models/Template';

@Injectable({
    providedIn: 'root'
})
export class TemplateService {


    private readonly http = inject(HttpClient);

    private readonly apiUrl = '/api/templates';

    listar(): Observable<Template[]> {
        return this.http.get<Template[]>(this.apiUrl);
    }

    buscarPorId(id: string): Observable<Template> {
        return this.http.get<Template>(`${this.apiUrl}/${id}`);
    }

    criar(dto: TemplateDTO): Observable<Template> {
        return this.http.post<Template>(this.apiUrl, dto);
    }

    atualizar(id: string, dto: TemplateDTO): Observable<Template> {
        return this.http.put<Template>(`${this.apiUrl}/${id}`, dto);
    }

    excluir(id: string): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }
}
