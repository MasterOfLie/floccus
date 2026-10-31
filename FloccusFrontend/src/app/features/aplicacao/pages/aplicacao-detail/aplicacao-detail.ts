import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AlertaService } from '../../../../core/services/alerta-service';
import { AplicacaoService } from '../../../../core/services/aplicacao-service';
import { TerminalLogs } from '../../../../shared/components/terminal-logs/terminal-logs';
import { AplicacaoModel, TipoAplicacao } from '../../models/AplicacaoModel';
import { VariaveisRequest, VariavelAplicacao } from '../../models/VariavelAplicacao';

@Component({
  selector: 'app-aplicacao-detail',
  standalone: true,
  imports: [CommonModule, TerminalLogs, ReactiveFormsModule],
  templateUrl: './aplicacao-detail.html',
  styleUrl: './aplicacao-detail.css',
})
export class AplicacaoDetail implements OnInit {
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private aplicacaoService = inject(AplicacaoService);
  private wsSubscription?: Subscription;
  private fb = inject(FormBuilder);
  private alertaService = inject(AlertaService);

  verificandoHeartbeat = signal(true);
  aplicacao = signal<AplicacaoModel | null>(null);
  loading = signal(false);
  error = signal('');
  showConsole = signal(false);
  showConfig = signal(false);
  salvando = signal(false);

  formEdicao = this.fb.group({
    id: [''],
    nome: ['', [Validators.required, Validators.maxLength(100)]],
    imagem: ['', [Validators.required, Validators.maxLength(255)]],
    tipo: [TipoAplicacao.HTTP, Validators.required],

    agenteId: [{ value: '', disabled: false }],
    redeId: [{ value: '', disabled: false }],

    portaContainer: [null as number | null, [
      Validators.min(1),
      Validators.max(65535)
    ]],
    portaExterna: [null as number | null, [
      Validators.min(1),
      Validators.max(65535)
    ]],
    dominio: [''],

    repositorioId: [{ value: '', disabled: false }],
    branch: [''],
    linguagem: [''],
    framework: [''],
    runtime: [''],
    buildTool: [''],

    memoriaLimite: [null as number | null, [
      Validators.required,
      Validators.min(16)
    ]],
    cpuLimite: [null as number | null, [
      Validators.required,
      Validators.min(0.1)
    ]]
  });



  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadContainer(id);
      const abrirConsole =
        history.state?.abrirConsole === true;

      if (abrirConsole) {
        this.showConsole.set(true);
      }
    } else {
      this.error.set('ID do container não informado.');
    }
  }
  private iniciarEscutaWebSocket(
    agenteId: string,
    aplicacaoId: string
  ): void {
    this.verificandoHeartbeat.set(true);

    const timeout = setTimeout(() => {
      if (this.verificandoHeartbeat()) {
        this.verificandoHeartbeat.set(false);

        this.aplicacao.update((app): AplicacaoModel | null => {
          if (!app) return null;
          return { ...app, status: 'OFFLINE' };
        });
      }
    }, 10000);

    this.wsSubscription = this.aplicacaoService
      .escutarHeartbeatAgente(agenteId)
      .subscribe({
        next: (containers) => {
          const containerAtualizado = containers.find(
            (c: any) => c.aplicacaoId === aplicacaoId
          );

          if (!containerAtualizado) return;

          clearTimeout(timeout);

          this.aplicacao.update((app): AplicacaoModel | null => {
            if (!app) return null;

            return {
              ...app,
              status: containerAtualizado.status,
              cpu: `${containerAtualizado.cpuPercentual?.toFixed(2)}%`,
              memoria: this.formatarMemoria(
                containerAtualizado.memoriaUso
              ),
              memoriaLimite: containerAtualizado.memoriaLimite,
              containerId: containerAtualizado.containerId
            };
          });

          this.verificandoHeartbeat.set(false);
        },
        error: () => {
          clearTimeout(timeout);
          this.verificandoHeartbeat.set(false);

          this.aplicacao.update((app): AplicacaoModel | null => {
            if (!app) return null;
            return { ...app, status: 'OFFLINE' };
          });
        }
      });
  }

  private formatarMemoria(memoriaMb: number): string {
    if (!memoriaMb) return '0 MB';
    if (memoriaMb >= 1024) {
      const gb = memoriaMb / 1024;
      return `${gb.toFixed(1)} GB`;
    }
    return `${memoriaMb} MB`;
  }
  isOnline(): boolean {
    const status = this.aplicacao()?.status?.toLowerCase() || '';
    return status.includes('up') || status.includes('running') || status.includes('online');
  }

  private loadContainer(id: string): void {
    this.loading.set(true);

    this.aplicacaoService.buscarPorId(id).subscribe({
      next: (aplicacao) => {
        this.aplicacao.set(aplicacao);
        this.loading.set(false);
        if (aplicacao.agenteId && aplicacao.id) {
          this.iniciarEscutaWebSocket(aplicacao.agenteId, aplicacao.id);
        }
        this.preencherFormulario(aplicacao);
        if (aplicacao.id) {
          this.aplicacaoService.buscarVariaveis(aplicacao.id).subscribe({
            next: (res) => this.preencherVariaveis(res.variaveis)
          });
        }
      },
      error: (erro) => {
        this.error.set(
          'Erro ao carregar detalhes da aplicação.'
        );
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
  preencherFormulario(aplicacao: AplicacaoModel): void {
    this.formEdicao.patchValue({
      nome: aplicacao.nome,
      imagem: aplicacao.imagem,
      tipo: aplicacao.tipo,
      agenteId: aplicacao.nomeAgente,
      redeId: aplicacao.nomeRede ?? '',
      portaContainer: aplicacao.portaContainer ?? null,
      portaExterna: aplicacao.portaExterna ?? null,
      dominio: aplicacao.dominio ?? '',
      repositorioId: aplicacao.repositorioId ?? '',
      branch: aplicacao.branch ?? '',
      linguagem: aplicacao.linguagem ?? '',
      framework: aplicacao.framework ?? '',
      runtime: aplicacao.runtime ?? '',
      buildTool: aplicacao.buildTool ?? '',
      memoriaLimite: aplicacao.memoriaLimite ?? 512,
      cpuLimite: aplicacao.cpuLimite ?? 1
    });

    this.formEdicao.markAsPristine();
  }

  salvarConfiguracoes(): void {
    if (this.formEdicao.invalid || this.salvando()) {
      this.formEdicao.markAllAsTouched();
      return;
    }

    const app = this.aplicacao();
    if (!app?.id) {
      this.alertaService.erro('ID da aplicação não encontrado.');
      return;
    }
    const formValues = this.formEdicao.getRawValue();

    const dadosAtualizacao: Partial<AplicacaoModel> = {
      id: app.id,
      nome: formValues.nome ?? '',
      imagem: formValues.imagem ?? '',
      tipo: formValues.tipo ?? TipoAplicacao.HTTP,
      agenteId: formValues.agenteId ?? '',
      redeId: formValues.redeId ?? '',
      portaContainer: formValues.portaContainer ?? undefined,
      portaExterna: formValues.portaExterna ?? undefined,
      dominio: formValues.dominio ?? '',
      repositorioId: formValues.repositorioId ?? '',
      branch: formValues.branch ?? '',
      linguagem: formValues.linguagem ?? '',
      framework: formValues.framework ?? '',
      runtime: formValues.runtime ?? '',
      buildTool: formValues.buildTool ?? '',
      memoriaLimite: formValues.memoriaLimite ?? 512,
      cpuLimite: formValues.cpuLimite ?? 1
    };

    this.salvando.set(true);

    this.aplicacaoService.atualizar(app.id, dadosAtualizacao).subscribe({
      next: (aplicacaoAtualizada) => {
        this.aplicacao.set(aplicacaoAtualizada);
        this.preencherFormulario(aplicacaoAtualizada);
        this.alertaService.sucesso('Configurações salvas com sucesso! Faça o redeploy para aplicar as alterações.');
      },
      error: (erro) => {
        this.alertaService.erro(erro.error?.message || 'Erro ao atualizar aplicação');
      },
      complete: () => {
        this.salvando.set(false);
      }
    });
  }


  async redeploy(): Promise<void> {
    const app = this.aplicacao();
    if (!app?.id) return;

    const confirmado = await this.alertaService.confirmar('Deseja realmente fazer o redeploy desta aplicação?', 'Confirmar Redeploy');

    if (!confirmado) {
      return;
    }

    this.aplicacaoService.redeploy(app.id).subscribe({
      next: () => {
        this.error.set('');
        this.alertaService.sucesso('Redeploy iniciado com sucesso!');
        this.loadContainer(app.id!);
      },
      error: (erro) => {
        this.error.set('Falha ao iniciar redeploy.');
        this.alertaService.erro(erro.error?.message || 'Falha ao iniciar redeploy.');
      }
    });
  }
  abaAtiva: 'geral' | 'variaveis' = 'geral';
  salvandoVariaveis = signal(false);

  variaveis: VariavelAplicacao[] = [];

  formVariaveis = this.fb.group({
    variaveis: this.fb.array([])
  });

  get variaveisArray(): FormArray {
    return this.formVariaveis.get('variaveis') as FormArray;
  }
  preencherVariaveis(variaveis: VariavelAplicacao[]): void {
    this.variaveisArray.clear();
    variaveis.forEach(v => {
      this.adicionarVariavel(v);
    });
  }

  adicionarVariavel(variavel?: VariavelAplicacao): void {
    const varGroup = this.fb.group({
      id: [variavel?.id || null],
      chave: [variavel?.chave || '', [Validators.required]],
      valor: [variavel?.valor || ''],
      secreta: [variavel?.secreta || false]
    });
    this.variaveisArray.push(varGroup);
  }

  removerVariavel(index: number): void {
    this.variaveisArray.removeAt(index);
  }

  salvarVariaveis(): void {
    if (this.formVariaveis.invalid) {
      this.formVariaveis.markAllAsTouched();
      return;
    }

    const app = this.aplicacao();
    if (!app?.id) {
      this.alertaService.erro('ID da aplicação não encontrado.');
      return;
    }

    const variaveisMapeadas: VariavelAplicacao[] = this.variaveisArray.controls.map(control => {
      const id = control.get('id')?.value;
      const valor = control.get('valor')?.value;
      const secreta = control.get('secreta')?.value;

      let valorParaEnviar = valor;

      if (id && secreta && (!valor || valor.trim() === '')) {
        valorParaEnviar = null;
      }

      return {
        id: id || undefined,
        chave: control.get('chave')?.value,
        valor: valorParaEnviar,
        secreta: secreta
      };
    });

    const request: VariaveisRequest = {
      variaveis: variaveisMapeadas
    };

    this.salvandoVariaveis.set(true);

    this.aplicacaoService.salvarVariaveis(app.id, request).subscribe({
      next: () => {
        this.alertaService.sucesso('Variáveis de ambiente salvas com sucesso! Faça o redeploy para aplicar as alterações.');
        this.aplicacaoService.buscarVariaveis(app.id!).subscribe(res => {
          this.preencherVariaveis(res.variaveis);
        });
      },
      error: (erro) => {
        this.alertaService.erro(erro.error?.message || 'Erro ao salvar variáveis de ambiente');
      },
      complete: () => {
        this.salvandoVariaveis.set(false);
      }
    });
  }

  async excluirAplicacao(): Promise<void> {
    const app = this.aplicacao();
    if (!app?.id) {
      this.alertaService.erro('ID da aplicação não encontrado.');
      return;
    }
    const confirmado = await this.alertaService.confirmar(`Tem certeza que deseja excluir a aplicação "${app.nome}"?`, 'Confirmar exclusão');

    if (!confirmado) {
      return;
    }
    this.aplicacaoService.excluirAplicacao(app.id).subscribe({
      next: () => {
        this.alertaService.sucesso('Aplicação excluída com sucesso!');
        this.router.navigate(['/aplicacoes']);
      },
      error: (erro) => {
        this.alertaService.erro(erro.error?.message || 'Erro ao excluir aplicação');
      }
    });
  }


}


