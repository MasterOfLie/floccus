import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { Agente } from '../../features/agentes/models/AgenteModel';
import { CriarAgenteRequest } from '../../features/agentes/models/CriarAgenteRequest';
import { Client, Message } from '@stomp/stompjs';
import { AgenteMetricas } from '../../features/agentes/models/AgenteMetricas';

@Injectable({
    providedIn: 'root'
})
export class AgenteService {

    private readonly http = inject(HttpClient);
    private stompClient?: Client;
    private readonly apiUrl = '/api/agentes';
    private readonly wsUrl =
        `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/ws-floccus`;
    listar(
        page: number = 0,
        size: number = 10
    ): Observable<PageResponse<Agente>> {

        return this.http.get<PageResponse<Agente>>(
            this.apiUrl,
            {
                params: {
                    page: page,
                    size: size
                }
            }
        );

    }

    buscarPorId(id: string): Observable<Agente> {
        return this.http.get<Agente>(`${this.apiUrl}/${id}`);
    }

    excluir(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }
    atualizar(id: string, agente: Agente): Observable<Agente> {
        return this.http.put<Agente>(`${this.apiUrl}/${id}`, agente);
    }
    atualizarConfiguracao(id: string): Observable<Agente> {
        return this.http.put<Agente>(`${this.apiUrl}/${id}/chave`, null);
    }

    criarAgente(
        agente: CriarAgenteRequest
    ): Observable<Agente> {

        return this.http.post<Agente>(
            this.apiUrl,
            agente
        );

    }

    conectarMetricas(agenteId: string): Observable<AgenteMetricas> {
        const subject = new Subject<AgenteMetricas>();

        this.stompClient = new Client({
            brokerURL: this.wsUrl,
            reconnectDelay: 5000,

            onConnect: () => {
                this.stompClient?.subscribe(
                    `/topic/agentes/${agenteId}/status`,
                    (message: Message) => {
                        try {
                            const dados: AgenteMetricas = JSON.parse(message.body);
                            subject.next(dados);
                        } catch (err) {
                            console.error('Erro ao processar métricas do WebSocket:', err);
                        }
                    }
                );
            },

            onStompError: (frame) => {
                console.error('Erro STOMP:', frame);
                subject.error(frame);
            },

            onWebSocketError: (error) => {
                console.error('Erro WebSocket:', error);
            }
        });

        this.stompClient.activate();

        return subject.asObservable();
    }
    desconectarMetricas(): void {
        if (this.stompClient) {
            this.stompClient.deactivate();
            this.stompClient = undefined;
        }
    }
}
