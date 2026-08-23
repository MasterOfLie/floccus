import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { Client, Message } from '@stomp/stompjs';
import { Container } from '../../features/containers/models/ContainerModel';

@Injectable({
  providedIn: 'root'
})
export class ContainerService {

  private http = inject(HttpClient);
  private stompClient?: Client;
  private readonly apiUrl = '/api/container';
  private readonly wsUrl =
    `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/ws-floccus`;

  listar(): Observable<Container[]> {
    return this.http.get<Container[]>(this.apiUrl);
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
  detalharContainer(id: string): Observable<Container> {
    return this.http.get<Container>(this.apiUrl + `/${id}`);
  }

  getLogs(id: string): Observable<string> {
    return this.http.get(`${this.apiUrl}/${id}/logs`, { responseType: 'text' });
  }

  conectar(containerId: string): Observable<string> {
    const subject = new Subject<string>();

    this.stompClient = new Client({
      brokerURL: this.wsUrl,
      reconnectDelay: 5000,

      onConnect: () => {
        console.log(`WebSocket conectado para o container ${containerId}`);

        this.stompClient?.subscribe(
          `/topic/logs/${containerId}`,
          (message: Message) => {
            subject.next(message.body);
          }
        );

        this.stompClient?.publish({
          destination: `/app/logs/start/${containerId}`
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

  desconectar(containerId: string): void {
    if (!this.stompClient) {
      return;
    }

    if (this.stompClient.connected) {
      this.stompClient.publish({
        destination: `/app/logs/stop/${containerId}`
      });
    }

    this.stompClient.deactivate();
    this.stompClient = undefined;
  }
}