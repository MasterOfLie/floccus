import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Client, IMessage, Message, StompSubscription } from '@stomp/stompjs';
import { Observable, Subject } from 'rxjs';
import { map } from 'rxjs/operators';
import { AplicacaoModel } from '../../features/aplicacao/models/AplicacaoModel';
import { MonitoramentoResponseDTO } from '../../features/aplicacao/models/ContainerMonitoramentoDTO';
import { CriarAplicacaoRequest } from '../../features/aplicacao/models/CriarAplicacaoRequest';
import { VariaveisRequest } from '../../features/aplicacao/models/VariavelAplicacao';

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

  redeploy(id: string): Observable<void> {
    return this.http.post<void>(this.apiUrl + `/redeploy/${id}`, {});
  }

  listarMonitoramento(): Observable<MonitoramentoResponseDTO> {
    return this.http.get<MonitoramentoResponseDTO>(
      '/api/heartbeat'
    );
  }

  criarAplicacao(aplicacao: CriarAplicacaoRequest): Observable<any> {
    return this.http.post('/api/aplicacoes', aplicacao);
  }
  start(id: string): Observable<void> {
    return this.http.get<void>(this.apiUrl + `/iniciar/${id}`);
  }
  atualizar(id: string, dados: Partial<AplicacaoModel>): Observable<AplicacaoModel> {
    return this.http.put<AplicacaoModel>(`/api/aplicacoes/${id}`, dados);
  }
  stop(id: string): Observable<void> {
    return this.http.get<void>(this.apiUrl + `/parar/${id}`);
  }

  restart(id: string): Observable<void> {
    return this.http.get<void>(this.apiUrl + `/reiniciar/${id}`);
  }
  buscarPorId(id: string): Observable<AplicacaoModel> {
    return this.http.get<AplicacaoModel>(this.apiUrl + `/${id}`);
  }
  buscarVariaveis(aplicacaoId: string): Observable<VariaveisRequest> {
    return this.http.get<VariaveisRequest>(`/api/variaveis/${aplicacaoId}`);
  }
  salvarVariaveis(aplicacaoId: string, request: VariaveisRequest): Observable<any> {
    return this.http.put(`/api/variaveis/${aplicacaoId}`, request);
  }
  excluirAplicacao(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
  getLogs(id: string): Observable<string> {
    return this.http.get(`${this.apiUrl}/${id}/logs`, { responseType: 'text' });
  }

  iniciarDeploy(aplicacaoId: string): Observable<void> {
    return this.http.post<void>(
      `${this.apiUrl}/deploy/${aplicacaoId}`,
      {}
    );
  }

  conectarDeploy(): Promise<void> {
    return new Promise((resolve, reject) => {

      this.stompClient = new Client({
        brokerURL: this.wsUrl,

        reconnectDelay: 5000,

        onConnect: () => {
          console.log('WebSocket conectado');
          resolve();
        },

        onStompError: (frame) => {
          console.error('Erro STOMP:', frame);
          reject(frame);
        },

        onWebSocketError: (error) => {
          console.error('Erro WebSocket:', error);
          reject(error);
        }
      });

      this.stompClient.activate();
    });
  }

  inscrever(
    aplicacaoId: string,
    callback: (evento: DeployEvento) => void
  ): StompSubscription | undefined {

    if (!this.stompClient?.connected) {
      console.error('WebSocket não conectado');
      return undefined;
    }

    return this.stompClient.subscribe(
      `/topic/deploy/${aplicacaoId}`,
      (message: IMessage) => {

        const evento: DeployEvento = JSON.parse(message.body);

        callback(evento);
      }
    );
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
  desconectarDeploy(): void {
    if (!this.stompClient) {
      return;
    }

    this.stompClient.deactivate();
    this.stompClient = undefined;

    console.log('WebSocket de deploy desconectado');
  }
  escutarHeartbeatAgente(agenteId: string): Observable<any[]> {
    return new Observable(observer => {
      this.stompClient = new Client({
        brokerURL: this.wsUrl,
        onConnect: () => {
          this.stompClient?.subscribe(`/topic/heartbeat/${agenteId}/containers`, (message) => {
            const heartbeat = JSON.parse(message.body);
            if (heartbeat && heartbeat.containers) {
              observer.next(heartbeat.containers);
            }
          });
        },
        onStompError: (frame) => {
          console.error('Erro no STOMP Broker:', frame.headers['message']);
        }
      });

      this.stompClient.activate();
      return () => {
        if (this.stompClient) {
          this.stompClient.deactivate();
        }
      };
    });
  }
}