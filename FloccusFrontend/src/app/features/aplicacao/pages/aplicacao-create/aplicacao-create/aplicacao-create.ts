import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { NgLabelTemplateDirective, NgOptionTemplateDirective, NgSelectComponent } from '@ng-select/ng-select';
import { StompSubscription } from '@stomp/stompjs';
import { AgenteService } from '../../../../../core/services/agente-service';
import { AlertaService } from '../../../../../core/services/alerta-service';
import { AplicacaoService } from '../../../../../core/services/aplicacao-service';
import { GithubService } from '../../../../../core/services/github-service';
import { RedeService } from '../../../../../core/services/rede-service';
import { TemplateService } from '../../../../../core/services/template-service';
import { VariaveisService } from '../../../../../core/services/variaveis-service';
import { GithubDTO } from '../../../../git/github/models/GithubModel';
import { AplicacaoModel } from '../../../models/AplicacaoModel';
import { GitHubBranchDTO } from '../../../models/Branches';
import { VariaveisRequest, VariavelAplicacao } from '../../../models/VariavelAplicacao';

@Component({
  selector: 'app-aplicacao-create',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    NgSelectComponent,
    NgLabelTemplateDirective,
    NgOptionTemplateDirective
  ],
  templateUrl: './aplicacao-create.html',
  styleUrl: './aplicacao-create.css'
})
export class AplicacaoCreate implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly githubService = inject(GithubService);
  private readonly agenteService = inject(AgenteService);
  private readonly aplicacaoService = inject(AplicacaoService);
  private readonly alertaService = inject(AlertaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly templateService = inject(TemplateService);
  private readonly redeService = inject(RedeService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly variaveisService = inject(VariaveisService);

  passoAtual = 1;

  aplicacaoCriadaId: string | null = null;
  gitSelecionado: GithubDTO | null = null;
  branchSelecionado: GitHubBranchDTO | null = null;

  realizandoDeploy = false;
  salvando = false;

  templates: any[] = [];
  gits: any[] = [];
  branches: any[] = [];
  repositorios: any[] = [];
  agentes: any[] = [];
  redes: any[] = [];
  variaveis: VariavelAplicacao[] = [];

  templateSelecionado: any = null;
  repositorioSelecionado: any = null;
  redeSelecionada: any = null;


  carregandoTemplates = false;
  carregandoRepositorios = false;
  carregandoGits = false;
  carregandoAgentes = false;
  carregandoRedes = false;
  carregandoBranches = false;
  formulario!: FormGroup;

  ngOnInit(): void {
    this.formulario = this.fb.group({
      templateId: ['', Validators.required],
      imagem: ['', Validators.required],
      buildCommand: ['', Validators.required],
      startCommand: ['', Validators.required],
      repositorioId: ['', Validators.required],
      gitId: ['', Validators.required],
      branch: ['', Validators.required],
      agenteId: ['', Validators.required],
      nome: ['', Validators.required],
      redeId: ['', Validators.required],
      tipo: ['HTTP', Validators.required],
      portaContainer: [
        '',
        [
          Validators.required,
          Validators.min(1),
          Validators.max(65535)
        ]
      ],
      portaExterna: [
        '',
        [
          Validators.required,
          Validators.min(1),
          Validators.max(65535)
        ]
      ],
      dominio: [''],
      memoriaLimite: [
        512,
        [
          Validators.required,
          Validators.min(1)
        ]
      ],
      cpuLimite: [
        1,
        [
          Validators.required,
          Validators.min(0.1)
        ]
      ]
    });

    this.carregarTemplates();

    this.carregarAgentes();
    this.carregarGits();

    this.formulario.get('agenteId')?.valueChanges.subscribe(() => {
      this.formulario.get('redeId')?.setValue('');
      this.redeSelecionada = null;
      this.carregarRedes();
    });
    this.atualizarValidacaoConectividade();

    this.formulario.get('tipo')?.valueChanges.subscribe(() => {
      this.atualizarValidacaoConectividade();
    });
  }

  proximoPasso(): void {
    let campos: string[] = [];
    switch (this.passoAtual) {
      case 1:
        campos = ['templateId', 'imagem', 'buildCommand', 'startCommand'];
        break;
      case 2:
        campos = ['repositorioId', 'agenteId', 'nome'];
        break;
      case 3:
        campos = ['redeId', 'tipo', 'portaContainer'];
        if (this.formulario.get('tipo')?.value !== 'HTTP') {
          campos.push('portaExterna');
        }
        if (this.formulario.get('tipo')?.value === 'HTTP') {
          campos.push('dominio');
        }
        break;
      case 4:
        campos = ['memoriaLimite', 'cpuLimite'];
        break;
    }
    let valido = true;
    campos.forEach(campo => {
      const controle = this.formulario.get(campo);
      if (controle?.invalid) {
        controle.markAsTouched();
        valido = false;
      }
    });
    if (!valido) {
      this.alertaService.erro('Preencha todos os campos obrigatórios desta etapa.');
      return;
    }
    this.passoAtual++;
  }

  passoAnterior(): void {
    if (this.passoAtual <= 1) {
      return;
    }
    if (this.passoAtual >= 5) {
      return;
    }
    this.passoAtual--;
  }

  irParaPasso(passo: number): void {
    if (this.passoAtual >= 5 && passo < this.passoAtual) {
      return;
    }
    if (passo <= 4) {
      this.passoAtual = passo;
      return;
    }
    if ((passo === 5 || passo === 6) && !this.aplicacaoCriadaId) {
      this.alertaService.erro(
        'Conclua a criação da aplicação antes de continuar.'
      );
      return;
    }

    this.passoAtual = passo;
  }

  selecionarTemplate(template: any): void {
    this.templateSelecionado = template;
    this.formulario.patchValue({
      imagem: template.imagem || '',
      buildCommand: template.buildCommand || '',
      startCommand: template.startCommand || ''
    });
  }

  selecionarRepositorio(repositorio: any): void {
    this.repositorioSelecionado = repositorio;

    this.branches = [];

    if (!repositorio?.full_name) {
      return;
    }

    const gitId = this.formulario.get('gitId')?.value;

    if (!gitId) {
      return;
    }

    const [owner, repo] = repositorio.full_name.split('/');

    this.carregarBranches(gitId, owner, repo);
  }

  selecionarRede(rede: any): void {
    this.redeSelecionada = rede;
  }

  carregarTemplates(): void {
    this.templateService.listar().subscribe({
      next: (res: any) => {
        this.templates = res;
        this.carregandoTemplates = false;
      },
      error: () => {
        this.carregandoTemplates = false;
        this.alertaService.erro('Erro ao carregar templates.');
      }
    });
  }

  carregarGits(): void {
    this.githubService.listar(0, 100).subscribe({
      next: (res: PageResponse<GithubDTO>) => {
        this.gits = res.content;
        this.carregandoGits = false;
      },
      error: () => {
        this.carregandoGits = false;
        this.alertaService.erro('Erro ao carregar gits.');
      }
    });
  }

  selecionarGit(git: any): void {
    this.gitSelecionado = git;
    this.carregarRepositorios(git.id);
  }

  carregarBranches(gitId: string, owner: string, repo: string): void {
    this.githubService.listarBranches(gitId, owner, repo).subscribe({
      next: (res: any) => {
        this.branches = res;
        this.carregandoBranches = false;
      },
      error: () => {
        this.carregandoBranches = false;
        this.alertaService.erro('Erro ao carregar branches.');
      }
    });
  }

  selecionarBranch(branch: any): void {
    this.branchSelecionado = branch;
  }

  carregarRepositorios(gitId: string): void {
    this.githubService.listarRepositorios(gitId).subscribe({
      next: (res: any) => {
        this.repositorios = res;
        this.carregandoRepositorios = false;
      },
      error: () => {
        this.carregandoRepositorios = false;
        this.alertaService.erro('Erro ao carregar repositórios.');
      }
    });
  }

  carregarAgentes(): void {
    this.agenteService.listar(0, 0, false).subscribe({
      next: (res: any) => {
        this.agentes = res;
        this.carregandoAgentes = false;
      },
      error: () => {
        this.carregandoAgentes = false;
        this.alertaService.erro('Erro ao carregar agentes.');
      }
    });
  }

  carregarRedes(): void {
    const agenteId = this.formulario.get('agenteId')?.value;
    if (!agenteId) {
      this.redes = [];
      return;
    }
    this.carregandoRedes = true;
    this.redeService.listar(0, 0, false).subscribe({
      next: (res: any[]) => {
        this.redes = res.filter(rede => rede.agenteId === agenteId);
        this.carregandoRedes = false;
      },
      error: () => {
        this.redes = [];
        this.carregandoRedes = false;
        this.alertaService.erro('Erro ao carregar redes.');
      }
    });
  }

  criarAplicacao(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      this.alertaService.erro('Preencha todos os campos obrigatórios.');
      return;
    }

    this.salvando = true;

    const aplicacao: AplicacaoModel = {
      status: 'CRIADO',
      nome: this.formulario.get('nome')?.value,
      imagem: this.formulario.get('imagem')?.value,
      agenteId: this.formulario.get('agenteId')?.value,
      redeId: this.formulario.get('redeId')?.value,
      tipo: this.formulario.get('tipo')?.value,
      portaContainer: this.formulario.get('portaContainer')?.value,
      portaExterna: this.formulario.get('portaExterna')?.value,
      dominio: this.formulario.get('dominio')?.value,
      memoriaLimite: this.formulario.get('memoriaLimite')?.value,
      memoria: `${this.formulario.get('memoriaLimite')?.value} MB`,
      cpuLimite: this.formulario.get('cpuLimite')?.value,
      cpu: `${this.formulario.get('cpuLimite')?.value} vCPU`,
      repositorioId: this.formulario.get('repositorioId')?.value,
      idGitApp: this.formulario.get('gitId')?.value,
      branch: this.formulario.get('branch')?.value,
      buildTool: this.formulario.get('buildCommand')?.value,
      runtime: this.formulario.get('startCommand')?.value,
      linguagem: this.formulario.get('linguagem')?.value,
      framework: this.formulario.get('framework')?.value,
      tempoStatus: '',
      nomeAgente: '',
      nomeRede: ''
    };

    this.aplicacaoService.criarAplicacao(aplicacao).subscribe({
      next: (res: any) => {
        this.salvando = false;
        this.aplicacaoCriadaId = res?.id ?? res?.uuid ?? (typeof res === 'string' ? res : null);
        this.passoAtual = 5;
        this.cdr.detectChanges();
        this.alertaService.sucesso('Aplicação criada com sucesso.');
      },
      error: () => {
        this.salvando = false;
        this.alertaService.erro('Erro ao criar aplicação.');
      }
    });
  }


  adicionarVariavel(): void {

    if (!this.aplicacaoCriadaId) {
      return;
    }

    this.variaveis.push({
      chave: '',
      valor: '',
      secreta: false
    });
  }

  alterarNomeVariavel(index: number, event: Event): void {

    const input = event.target as HTMLInputElement;

    this.variaveis[index].chave = input.value;
  }
  alterarValorVariavel(index: number, event: Event): void {

    const input = event.target as HTMLInputElement;

    this.variaveis[index].valor = input.value;
  }
  alterarSecretaVariavel(index: number, event: Event): void {

    const input = event.target as HTMLInputElement;

    this.variaveis[index].secreta = input.checked;
  }
  removerVariavel(index: number): void {
    this.variaveis.splice(index, 1);
  }

  salvarVariaveis(): void {
    if (!this.aplicacaoCriadaId) {
      this.alertaService.erro('ID da aplicação não encontrado.');
      return;
    }
    const request: VariaveisRequest = {
      variaveis: this.variaveis
    };
    this.salvando = true;

    this.variaveisService
      .salvarTodas(this.aplicacaoCriadaId, request)
      .subscribe({
        next: () => {
          this.salvando = false;
          this.passoAtual = 6;
          this.cdr.detectChanges();
        },
        error: () => {
          this.salvando = false;
          this.alertaService.erro('Erro ao salvar as variáveis.');
        }
      });
  }
  modalDeployAberta = false;
  deployConcluido = false;
  deployErro = false;

  deployEtapas: DeployEtapa[] = [];

  private deploySubscription?: StompSubscription;

  iniciarDeploy(id: string): void {
    if (!id) {
      this.alertaService.erro(
        'ID da aplicação não encontrado.'
      );
      return;
    }
    this.modalDeployAberta = true;
    this.realizandoDeploy = true;
    this.deployConcluido = false;
    this.deployErro = false;
    this.deployEtapas = [];
    this.aplicacaoService
      .conectarDeploy()
      .then(() => {
        this.deploySubscription =
          this.aplicacaoService.inscrever(
            id,
            evento => this.processarEventoDeploy(evento)
          );

        if (!this.deploySubscription) {
          this.realizandoDeploy = false;
          this.deployErro = true;
          this.alertaService.erro(
            'Não foi possível acompanhar o deploy.'
          );
          return;
        }
        this.aplicacaoService
          .iniciarDeploy(id)
          .subscribe({
            next: () => {
            },
            error: error => {
              this.realizandoDeploy = false;
              this.deployErro = true;
              this.alertaService.erro(
                error.error.message
              );
              this.finalizarDeployWebSocket();
            }
          });
      })
      .catch(error => {
        this.realizandoDeploy = false;
        this.deployErro = true;
        this.alertaService.erro(
          error.error.message
        );
      });
  }


  cancelarConfiguracao(): void {
    this.router.navigate(['/aplicacoes']);
  }

  getNomeAgente(): string {
    const agenteId = this.formulario.get('agenteId')?.value;
    const agente = this.agentes.find(agente => agente.id === agenteId);
    return agente?.nome || 'Não definido';
  }
  private processarEventoDeploy(evento: DeployEvento): void {
    this.deployEtapas.push({
      tipo: evento.tipo,
      mensagem: evento.mensagem,
      concluida: false,
      atual: true
    });
    this.deployEtapas.forEach(
      (etapa, index) => {
        etapa.atual =
          index === this.deployEtapas.length - 1;
        etapa.concluida =
          index < this.deployEtapas.length - 1;
      }
    );

    if (
      evento.tipo === 'CONTAINER_CRIADO' &&
      evento.containerId
    ) {
    }

    if (evento.tipo === 'CONCLUIDO') {

      this.realizandoDeploy = false;
      this.deployConcluido = true;

      const ultima =
        this.deployEtapas[
        this.deployEtapas.length - 1
        ];

      if (ultima) {
        ultima.concluida = true;
        ultima.atual = false;
      }



      this.finalizarDeployWebSocket();
      this.alertaService.sucessoDeploy(
        'Deploy realizado com sucesso.'
      ).then(() => {
        this.router.navigate(['/aplicacoes', this.aplicacaoCriadaId],
          { state: { abrirConsole: true } }
        );
      });
    }

    if (evento.tipo === 'ERRO') {

      this.realizandoDeploy = false;
      this.deployErro = true;

      this.finalizarDeployWebSocket();
    }

    this.cdr.detectChanges();
  }
  private finalizarDeployWebSocket(): void {
    this.deploySubscription?.unsubscribe();
    this.deploySubscription = undefined;
    this.aplicacaoService.desconectarDeploy();
  }

  private atualizarValidacaoConectividade(): void {
    const tipo = this.formulario.get('tipo')?.value;
    const portaExterna = this.formulario.get('portaExterna');
    const dominio = this.formulario.get('dominio');

    if (!portaExterna || !dominio) {
      return;
    }

    if (tipo === 'HTTP') {
      portaExterna.clearValidators();
      dominio.setValidators([Validators.required]);
    } else {
      portaExterna.setValidators([
        Validators.required,
        Validators.min(1),
        Validators.max(65535)
      ]);

      dominio.clearValidators();
    }

    portaExterna.updateValueAndValidity({ emitEvent: false });
    dominio.updateValueAndValidity({ emitEvent: false });
  }

}