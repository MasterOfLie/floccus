import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Container } from '../../features/containers/models/ContainerModel';

@Injectable({
  providedIn: 'root'
})
export class ContainerService {

  private http = inject(HttpClient);

  private readonly apiUrl = '/api/container';

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

  getLogs(id: string): Observable<string> {
    return this.http.get(`${this.apiUrl}/${id}/logs`, { responseType: 'text' });
  }

}