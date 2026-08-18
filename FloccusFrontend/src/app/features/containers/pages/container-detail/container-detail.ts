import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ContainerService } from '../../../../core/services/container-service';
import { Container } from '../../models/ContainerModel';

@Component({
  selector: 'app-container-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './container-detail.html',
  styleUrl: './container-detail.css',
})
export class ContainerDetail implements OnInit {
  private route = inject(ActivatedRoute);
  private containerService = inject(ContainerService);

  container = signal<Container | null>(null);
  loading = signal(false);
  error = signal('');
  consoleOutput = signal('');
  consoleLoading = signal(false);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadContainer(id);
    } else {
      this.error.set('ID do container não informado.');
    }
  }

  private loadContainer(id: string): void {
    this.loading.set(true);
    // Simple reuse of listar and filter locally; replace with dedicated endpoint if available
    this.containerService.listar().subscribe({
      next: (list) => {
        const found = list.find(c => c.id === id) || null;
        this.container.set(found);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Erro ao carregar detalhes do container.');
        this.loading.set(false);
      }
    });
  }

  start(): void {
    const id = this.container()?.id;
    if (!id) return;
    this.containerService.start(id).subscribe({
      next: () => this.refreshStatus(),
      error: () => this.error.set('Falha ao iniciar container.')
    });
  }

  restart(): void {
    const id = this.container()?.id;
    if (!id) return;
    this.containerService.restart(id).subscribe({
      next: () => this.refreshStatus(),
      error: () => this.error.set('Falha ao reiniciar container.')
    });
  }

  stop(): void {
    const id = this.container()?.id;
    if (!id) return;
    this.containerService.stop(id).subscribe({
      next: () => this.refreshStatus(),
      error: () => this.error.set('Falha ao parar container.')
    });
  }

  private refreshStatus(): void {
    const id = this.container()?.id;
    if (!id) return;
    this.loadContainer(id);
  }

  openConsole(): void {
    const id = this.container()?.id;
    if (!id) return;
    this.consoleLoading.set(true);
    this.containerService.getLogs(id).subscribe({
      next: (log) => {
        this.consoleOutput.set(log);
        this.consoleLoading.set(false);
      },
      error: () => {
        this.error.set('Não foi possível obter o console.');
        this.consoleLoading.set(false);
      }
    });
  }
}

