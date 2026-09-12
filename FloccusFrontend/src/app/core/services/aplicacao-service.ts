import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Client, Message } from '@stomp/stompjs';
import { Observable, Subject } from 'rxjs';
import { map } from 'rxjs/operators';
import { AplicacaoModel } from '../../features/aplicacao/models/AplicacaoModel';
import { CriarAplicacaoRequest } from '../../features/aplicacao/models/CriarAplicacaoRequest';

@Injectable({
  providedIn: 'root'
})
export class AplicacaoService {

  private http = inject(HttpClient);
  private stompClient?: Client;
  private readonly apiUrl = '/api/aplicacoes';
  private readonly wsUrl =
    `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/ws-floccus`;

  listar(
    page: number = 0,
    size: number = 10
  ): Observable<PageResponse<AplicacaoModel>> {

    return this.http.get<AplicacaoModel[]>(this.apiUrl).pipe(

      map(containers => {

        const inicio = page * size;

        const fim = inicio + size;

        const content = containers.slice(inicio, fim);

        const totalElements = containers.length;

        const totalPages = Math.ceil(
          totalElements / size
        );

        return {
          content,

          number: page,

          size,

          totalElements,

          totalPages,

          numberOfElements: content.length,

          first: page === 0,

          last: page >= totalPages - 1,

          empty: content.length === 0
        };

      })

    );

  }
  // Implementar paginação com PageResponse
  // listar(
  //   page: number = 0,
  //   size: number = 10
  // ): Observable<PageResponse<Container>> {

  //   return this.http.get<PageResponse<Container>>(
  //     this.apiUrl,
  //     {
  //       params: {
  //         page,
  //         size
  //       }
  //     }
  //   );

  // }
  criarAplicacao(
    aplicacao: CriarAplicacaoRequest
  ): Observable<any> {

    return this.http.post(
      '/api/aplicacoes',
      aplicacao
    );
  }
  start(id: string): Observable<void> {
    return this.http.get<void>(this.apiUrl + `/iniciar/${id}`);
  }

  stop(id: string): Observable<void> {
    return this.http.get<void>(this.apiUrl + `/parar/${id}`);
  }

  restart(id: string): Observable<void> {
    return this.http.get<void>(this.apiUrl + `/reiniciar/${id}`);
  }
  detalharContainer(id: string): Observable<AplicacaoModel> {
    return this.http.get<AplicacaoModel>(this.apiUrl + `/${id}`);
  }

  getLogs(id: string): Observable<string> {
    return this.http.get(`${this.apiUrl}/${id}/logs`, { responseType: 'text' });
  }

  conectar(aplicacaoId: string): Observable<string> {
    const subject = new Subject<string>();

    this.stompClient = new Client({
      brokerURL: this.wsUrl,
      reconnectDelay: 5000,

      onConnect: () => {
        console.log(`WebSocket conectado para o container ${aplicacaoId}`);

        this.stompClient?.subscribe(
          `/topic/logs/${aplicacaoId}`,
          (message: Message) => {
            subject.next(message.body);
          }
        );

        this.stompClient?.publish({
          destination: `/app/logs/start/${aplicacaoId}`
        });
      },

      onStompError: (frame) => {
        console.error('Erro STOMP:', frame);
        subject.error(frame);
      },

      onWebSocketError: (error) => {
        console.error('Erro WebSocket:', error);
      },

      onWebSocketClose: () => {
        console.log('WebSocket desconectado');
      }
    });

    this.stompClient.activate();

    return subject.asObservable();
  }

  desconectar(aplicacaoId: string): void {
    if (!this.stompClient) {
      return;
    }

    if (this.stompClient.connected) {
      this.stompClient.publish({
        destination: `/app/logs/stop/${aplicacaoId}`
      });
    }

    this.stompClient.deactivate();
    this.stompClient = undefined;
  }
}