import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { inject } from "@angular/core/primitives/di";
import { Observable } from "rxjs";
import { RedeModel } from "../../features/redes/models/Rede";

@Injectable({
    providedIn: 'root'
})
export class RedeService {

    private readonly apiUrl = '/api/rede';
    private readonly http = inject(HttpClient);
    criar(rede: RedeModel): Observable<RedeModel> {
        return this.http.post<RedeModel>(
            this.apiUrl + '/criar',
            rede
        );
    }
    listar(page: number = 0, size: number = 10, paginado: boolean = true): Observable<any> {
        return this.http.get<any>(
            `${this.apiUrl}?page=${page}&size=${size}&paginado=${paginado}`
        );
    }
    deletar(id: string): Observable<void> {
        return this.http.delete<void>(
            `${this.apiUrl}/${id}`
        );
    }
}
