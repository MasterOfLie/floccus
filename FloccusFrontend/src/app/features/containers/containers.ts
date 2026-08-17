import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import { ContainerService } from '../../core/services/container-service';
import { Container } from '../containers/models/ContainerModel';

@Component({
  selector: 'app-containers',
  imports: [CommonModule, RouterLink],
  templateUrl: './containers.html',
  styleUrl: './containers.css',
})
export class Containers {

  private containerService = inject(ContainerService);

  containers = signal<Container[]>([]);

  carregando = signal(false);

  erro = signal('');

  ngOnInit(): void {
    this.listarContainers();
  }

  listarContainers(): void {

    this.carregando.set(true);
    this.erro.set('');

    this.containerService.listar().subscribe({

      next: (containers) => {

        console.log('Containers recebidos:', containers);

        this.containers.set(containers);

        this.carregando.set(false);

      },

      error: (error) => {

        console.error('Erro ao buscar containers:', error);

        this.erro.set(
          'Não foi possível carregar os containers.'
        );

        this.carregando.set(false);

      }

    });

  }
  traduzirStatus(status: string): string {
    if (!status) return 'Desconhecido';
    const statusMin = status.toLowerCase();
    if (statusMin.includes('up') || statusMin.includes('running')) {
      return 'Em execução';
    }
    if (statusMin.includes('exited')) {
      return 'Parado';
    }
    return status;
  }


}
