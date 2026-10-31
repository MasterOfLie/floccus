import { CommonModule } from '@angular/common';
import { Component, ElementRef, inject, OnDestroy, OnInit, signal, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Chart, registerables } from 'chart.js';
import { finalize, Subscription } from 'rxjs';
import { AgenteService } from '../../../../core/services/agente-service';
import { AlertaService } from '../../../../core/services/alerta-service';
import { AgenteMetricas } from '../../models/AgenteMetricas';
import { Agente } from '../../models/AgenteModel';
Chart.register(...registerables);
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
  // Injeção de dependências moderna
  private readonly agenteService = inject(AgenteService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly alertaService = inject(AlertaService);

  private metricasSub?: Subscription;

  // Gerenciamento de Estado com Signals
  readonly metricas = signal<AgenteMetricas | null>(null);
  readonly agente = signal<Agente | null>(null);
  readonly carregando = signal(true);
  readonly salvando = signal(false);

  // Referência interna para o Canvas
  private cpuChartElement!: ElementRef<HTMLCanvasElement>;

  @ViewChild('cpuChart', { static: false }) set cpuChart(content: ElementRef<HTMLCanvasElement> | undefined) {
    if (content) {
      this.cpuChartElement = content;

      if (!this.graficoCpu) {
        setTimeout(() => {
          this.criarGraficoCpu();
          if (this.historicoCpu.length > 0) {
            this.atualizarGraficoCpu();
          }
        }, 50);
      }
    }
  }
  private graficoCpu?: Chart;

  private historicoCpu: number[] = [];
  private historicoCpuLabels: string[] = [];

  private readonly MAX_PONTOS = 60;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (!id) {
      this.alertaService.erro('ID do agente não fornecido.');
      this.router.navigate(['/agentes']);
      return;
    }

    this.carregarAgente(id);

    this.metricasSub = this.agenteService
      .conectarMetricas(id)
      .subscribe({
        next: (novasMetricas) => {
          this.metricas.set(novasMetricas);
          this.adicionarCpuHistorico(novasMetricas.cpuUsage);
        },
        error: (err) => console.error('Erro ao conectar métricas:', err)
      });
  }

  ngOnDestroy(): void {
    this.metricasSub?.unsubscribe();
    this.agenteService.desconectarMetricas();
  }

  carregarAgente(id: string): void {
    this.carregando.set(true);

    this.agenteService.buscarPorId(id)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (agente) => this.agente.set(agente),
        error: () => this.alertaService.erro('Não foi possível carregar os detalhes do agente.')
      });
  }

  salvar(): void {
    const agente = this.agente();
    if (!agente) return;

    this.salvando.set(true);

    this.agenteService.atualizar(agente.id, agente)
      .pipe(finalize(() => this.salvando.set(false)))
      .subscribe({
        next: (agenteAtualizado) => {
          this.agente.set(agenteAtualizado);
          this.alertaService.sucesso('Configurações salvas com sucesso!');
        },
        error: () => this.alertaService.erro('Erro ao salvar as configurações do agente.')
      });
  }

  async gerarNovaChave(): Promise<void> {
    const agente = this.agente();
    if (!agente) return;

    const confirmado = await this.alertaService.confirmar(
      'O agente atual perderá a conexão até ser atualizado com a nova chave. Esta ação é irreversível.',
      'Gerar nova chave de API?'
    );

    if (!confirmado) return;

    this.salvando.set(true);

    this.agenteService.atualizarConfiguracao(agente.id)
      .pipe(finalize(() => this.salvando.set(false)))
      .subscribe({
        next: (agenteAtualizado) => {
          this.agente.set(agenteAtualizado);
          this.alertaService.sucesso('Nova chave gerada! Atualize o agente com o novo YAML.');
        },
        error: () => this.alertaService.erro('Não foi possível gerar uma nova chave no momento.')
      });
  }

  gerarYaml(): string {
    const agente = this.agente();
    if (!agente) return '';

    return `floccus:
  agent:
    agent-id: "${agente.id}"
    port: ${agente.portaAgente}
    secret-key: "${agente.chaveAgente}"
    email-ssl: "contato@${window.location.hostname}"
  backend:
    host: "${agente.ipBackend ?? ''}"
    port: ${agente.portaBackend}
  docker:
    host: "${agente.dockerHost}"`;
  }

  async copiarYaml(): Promise<void> {
    try {
      await navigator.clipboard.writeText(this.gerarYaml());
      this.alertaService.sucesso('YAML copiado para a área de transferência');
    } catch (err) {
      console.error('Falha ao copiar:', err);
      this.alertaService.erro('Não foi possível copiar o YAML. Tente selecionar e copiar manualmente.');
    }
  }

  formatarUptime(segundosTotais?: number | null): string {
    if (!segundosTotais || segundosTotais <= 0) return "--";

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

  private criarGraficoCpu(): void {
    this.graficoCpu = new Chart(this.cpuChartElement.nativeElement, {
      type: 'line',

      data: {
        labels: this.historicoCpuLabels,

        datasets: [
          {
            label: 'CPU',
            data: this.historicoCpu,

            borderWidth: 2,

            pointRadius: 0,
            pointHoverRadius: 4,

            tension: 0.35,

            fill: true,

            borderColor: '#18181b',
            backgroundColor: 'rgba(24, 24, 27, 0.1)',
            pointBackgroundColor: '#18181b',
            pointBorderColor: '#ffffff',
          }
        ]
      },

      options: {
        responsive: true,

        maintainAspectRatio: false,

        animation: false,

        interaction: {
          intersect: false,
          mode: 'index'
        },

        plugins: {
          legend: {
            display: false
          },

          tooltip: {
            callbacks: {
              label: (context) =>
                ` CPU: ${context.parsed.y?.toFixed(1)}%`
            }
          }
        },

        scales: {
          x: {
            display: true,

            grid: {
              display: false
            },

            ticks: {
              maxTicksLimit: 6
            }
          },

          y: {
            min: 0,
            max: 100,

            ticks: {
              callback: (value) => `${value}%`
            }
          }
        }
      }
    });
  }

  private adicionarCpuHistorico(cpu: number): void {
    this.historicoCpu.push(cpu);

    const agora = new Date();

    this.historicoCpuLabels.push(
      agora.toLocaleTimeString('pt-BR', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    );

    if (this.historicoCpu.length > this.MAX_PONTOS) {
      this.historicoCpu.shift();
      this.historicoCpuLabels.shift();
    }

    this.atualizarGraficoCpu();
  }

  private atualizarGraficoCpu(): void {
    if (!this.graficoCpu) {
      return;
    }

    this.graficoCpu.data.labels = this.historicoCpuLabels;
    this.graficoCpu.data.datasets[0].data = this.historicoCpu;

    this.graficoCpu.update('none');
  }

  excluindo = signal(false);

  async excluirAgente(): Promise<void> {
    const agente = this.agente();
    if (!agente) return;

    const confirmado = await this.alertaService.confirmar(`Tem certeza que deseja excluir o agente "${agente.nome}"?`, 'Confirmar exclusão');

    if (!confirmado) {
      return;
    }
    this.excluindo.set(true);
    this.agenteService.excluir(agente.id).subscribe({
      next: () => {
        this.alertaService.sucesso('Agente excluído com sucesso!');
        this.router.navigate(['/agentes']);
      },
      error: (erro) => {
        this.alertaService.erro(erro.error?.message || 'Erro ao excluir agente');
        this.excluindo.set(false);
      }
    });
  }
}