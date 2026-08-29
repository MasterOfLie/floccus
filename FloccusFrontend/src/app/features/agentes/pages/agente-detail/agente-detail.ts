import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AgenteService } from '../../../../core/services/agente-service';
import { Agente } from '../../models/AgenteModel';
import { AlertaService } from '../../../../core/services/alerta-service';
import { AgenteMetricas } from '../../models/AgenteMetricas';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-agente-detail',
  imports: [
    CommonModule,
    FormsModule,
    RouterLink
  ],
  templateUrl: './agente-detail.html',
  styleUrl: './agente-detail.css'
})
export class AgenteDetail implements OnInit, OnDestroy {
  private agenteService = inject(AgenteService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private alertaService = inject(AlertaService);
  private metricasSub?: Subscription;

  metricas = signal<AgenteMetricas | null>(null);
  agente = signal<Agente | null>(null);
  carregando = signal(true);
  salvando = signal(false);


  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (!id) {
      this.router.navigate(['/agentes']);
      return;
    }

    this.carregarAgente(id);
    this.metricasSub = this.agenteService
      .conectarMetricas(id)
      .subscribe((novasMetricas) => {
        this.metricas.set(novasMetricas);
      });
  }

  carregarAgente(id: string): void {
    this.carregando.set(true);

    this.agenteService.buscarPorId(id).subscribe({
      next: (agente) => {
        this.agente.set(agente);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
      }
    });
  }

  salvar(): void {
    const agente = this.agente();

    if (!agente) {
      return;
    }

    this.salvando.set(true);
    this.agenteService.atualizar(agente.id, agente).subscribe({
      next: (agenteAtualizado) => {
        this.agente.set(agenteAtualizado);
        this.salvando.set(false);
        this.alertaService.sucesso('Agente atualizado com sucesso');
      },
      error: () => {
        this.salvando.set(false);
      }
    });
  }


  async gerarNovaChave(): Promise<void> {
    const agente = this.agente();

    if (!agente) return;

    const confirmado = await this.alertaService.confirmar(
      'Tem certeza que deseja gerar uma nova chave?',
      'Essa ação não pode ser desfeita'
    );

    if (!confirmado) {
      return;
    }

    this.salvando.set(true);

    this.agenteService.atualizarConfiguracao(agente.id).subscribe({
      next: (agenteAtualizado) => {
        this.agente.set(agenteAtualizado);
        this.salvando.set(false);

        this.alertaService.sucesso(
          'Nova chave gerada com sucesso'
        );
      },
      error: () => {
        this.salvando.set(false);

        this.alertaService.erro(
          'Não foi possível gerar uma nova chave.'
        );
      }
    });
  }

  gerarYaml(): string {
    const agente = this.agente();

    if (!agente) {
      return '';
    }

    return `floccus:
  agent:
    agent-id: "${agente.id}"
    port: ${agente.portaAgente}
    secret-key: "${agente.chaveAgente}"
  backend:
    host: "${agente.ipBackend ?? ''}"
    port: ${agente.portaBackend}
  docker:
    host: "${agente.dockerHost}"`;
  }

  copiarYaml(): void {
    navigator.clipboard.writeText(this.gerarYaml());
    this.alertaService.sucesso('YAML copiado para a área de transferência');
  }
  ngOnDestroy(): void {
    this.metricasSub?.unsubscribe();
    this.agenteService.desconectarMetricas();
  }
  formatarUptime(segundosTotais?: number | null): string {
    if (!segundosTotais || segundosTotais <= 0) {
      return "Desconhecido";
    }

    const dias = Math.floor(segundosTotais / 86400);
    const horas = Math.floor((segundosTotais % 86400) / 3600);
    const minutos = Math.floor((segundosTotais % 3600) / 60);
    const segundos = segundosTotais % 60;

    const partes: string[] = [];
    if (dias > 0) partes.push(`${dias}d`);
    if (horas > 0) partes.push(`${horas}h`);
    if (minutos > 0) partes.push(`${minutos}m`);
    if (segundos > 0 || partes.length === 0) partes.push(`${segundos}s`);

    return partes.join(' ');
  }
}