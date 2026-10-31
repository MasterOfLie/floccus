import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { VariaveisRequest, VariavelAplicacao } from '../../features/aplicacao/models/VariavelAplicacao';


@Injectable({
    providedIn: 'root'
})
export class VariaveisService {

    private readonly http = inject(HttpClient);

    private readonly apiUrl = '/api/variaveis';

    salvarTodas(
        aplicacaoId: string,
        request: VariaveisRequest
    ): Observable<VariavelAplicacao[]> {

        return this.http.post<VariavelAplicacao[]>(
            `${this.apiUrl}/${aplicacaoId}`,
            request
        );
    }

}
