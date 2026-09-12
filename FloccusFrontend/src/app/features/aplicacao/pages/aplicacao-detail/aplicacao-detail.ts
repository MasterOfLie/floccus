import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AplicacaoService } from '../../../../core/services/aplicacao-service';
import { AplicacaoModel } from '../../models/AplicacaoModel';
import { TerminalLogs } from '../../../../shared/components/terminal-logs/terminal-logs';

@Component({
  selector: 'app-aplicacao-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, TerminalLogs],
  templateUrl: './aplicacao-detail.html',
  styleUrl: './aplicacao-detail.css',
})
export class AplicacaoDetail implements OnInit {
  private route = inject(ActivatedRoute);
  private aplicacaoService = inject(AplicacaoService);

  aplicacao = signal<AplicacaoModel | null>(null);
  loading = signal(false);
  error = signal('');
  showConsole = signal(false);

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
    this.aplicacaoService.detalharContainer(id).subscribe({
      next: (aplicacao) => {
        this.aplicacao.set(aplicacao);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Erro ao carregar detalhes do container.');
        this.loading.set(false);
      }
    });
  }

  start(): void {
    const id = this.aplicacao()?.id;
    if (!id) return;
    this.aplicacaoService.start(id).subscribe({
      next: () => this.refreshStatus(),
      error: () => this.error.set('Falha ao iniciar container.')
    });
  }

  restart(): void {
    const id = this.aplicacao()?.id;
    if (!id) return;
    this.aplicacaoService.restart(id).subscribe({
      next: () => this.refreshStatus(),
      error: () => this.error.set('Falha ao reiniciar container.')
    });
  }

  stop(): void {
    const id = this.aplicacao()?.id;
    if (!id) return;
    this.aplicacaoService.stop(id).subscribe({
      next: () => this.refreshStatus(),
      error: () => this.error.set('Falha ao parar container.')
    });
  }

  private refreshStatus(): void {
    const id = this.aplicacao()?.id;
    if (!id) return;
    this.loadContainer(id);
  }

  openConsole(): void {
    this.showConsole.update(valorAtual => !valorAtual);
  }

}


