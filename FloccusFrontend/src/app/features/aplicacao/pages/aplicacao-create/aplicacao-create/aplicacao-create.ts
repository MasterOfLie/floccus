import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import {
  NgLabelTemplateDirective,
  NgOptionTemplateDirective,
  NgSelectComponent
} from '@ng-select/ng-select';

import { AgenteService } from '../../../../../core/services/agente-service';
import { AplicacaoService } from '../../../../../core/services/aplicacao-service';
import { GithubService } from '../../../../../core/services/github-service';

import { Agente } from '../../../../agentes/models/AgenteModel';
import { GitHubRepository } from '../../../../git/github/models/GitHubRepository';

import { TipoAplicacao } from '../../../models/AplicacaoModel';
import { CriarAplicacaoRequest } from '../../../models/CriarAplicacaoRequest';

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

  // Dados
  repositorios: GitHubRepository[] = [];
  agentes: Agente[] = [];

  // Estado
  repositorioSelecionado: GitHubRepository | null = null;

  carregandoRepositorios = false;
  carregandoAgentes = false;
  salvando = false;

  // Formulário
  formulario: FormGroup;

  constructor() {
    this.formulario = this.fb.group({
      repositorioId: ['', Validators.required],

      nome: ['', Validators.required],

      agenteId: ['', Validators.required],

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
          Validators.min(1)
        ]
      ],

      cpuLimite: [
        1,
        [
          Validators.min(1)
        ]
      ]
    });

    // Atualiza a validação do domínio quando o tipo muda
    this.formulario
      .get('tipo')
      ?.valueChanges
      .subscribe((tipo: TipoAplicacao) => {
        this.atualizarValidacaoDominio(tipo);
      });
  }

  ngOnInit(): void {
    this.carregarRepositorios();
    this.carregarAgentes();
  }

  /**
   * Carrega os repositórios disponíveis no GitHub.
   */
  carregarRepositorios(): void {
    this.carregandoRepositorios = true;

    this.githubService.listarRepositorios().subscribe({
      next: (repositorios) => {
        this.repositorios = repositorios;
        this.carregandoRepositorios = false;
      },

      error: (erro) => {
        console.error(
          'Erro ao carregar repositórios:',
          erro
        );

        this.repositorios = [];
        this.carregandoRepositorios = false;
      }
    });
  }

  /**
   * Carrega os agentes disponíveis.
   */
  carregarAgentes(): void {
    this.carregandoAgentes = true;

    this.agenteService.listar(0, 0, false).subscribe({
      next: (resposta) => {

        this.agentes = resposta.content || resposta as any;

        this.carregandoAgentes = false;
      },

      error: (erro) => {
        console.error(
          'Erro ao carregar agentes:',
          erro
        );

        this.agentes = [];
        this.carregandoAgentes = false;
      }
    });
  }

  /**
   * Executado quando o usuário seleciona um repositório.
   */
  selecionarRepositorio(
    repositorio: GitHubRepository | null
  ): void {

    this.repositorioSelecionado = repositorio;

    if (!repositorio) {
      this.formulario.patchValue({
        nome: ''
      });

      return;
    }

    this.formulario.patchValue({
      nome: repositorio.name,
      repositorioId: repositorio.id
    }
    );
  }

  /**
   * Atualiza a obrigatoriedade do domínio.
   *
   * HTTP -> domínio obrigatório
   * TCP/UDP -> domínio não utilizado
   */
  atualizarValidacaoDominio(
    tipo: TipoAplicacao
  ): void {

    const dominio = this.formulario.get('dominio');

    if (!dominio) {
      return;
    }

    if (tipo === 'HTTP') {

      dominio.setValidators([
        Validators.required
      ]);

    } else {

      dominio.clearValidators();
      dominio.setValue('');

    }

    dominio.updateValueAndValidity();
  }

  /**
   * Cria a aplicação.
   */
  criarAplicacao(): void {

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const valor = this.formulario.value;

    /*
     * Procura o repositório selecionado pelo ID.
     *
     * Isso evita depender de uma variável separada
     * para saber qual repositório foi escolhido.
     */
    const repositorio = this.repositorios.find(
      repo =>
        String(repo.id) ===
        String(valor.repositorioId)
    );

    if (!repositorio) {
      console.error(
        'Repositório selecionado não encontrado.'
      );

      return;
    }

    this.salvando = true;

    const request: CriarAplicacaoRequest = {
      nome: valor.nome,

      repositorioId: String(
        valor.repositorioId
      ),

      imagem: repositorio.name,

      agenteId: valor.agenteId,

      tipo: valor.tipo,

      portaContainer: valor.portaContainer,

      portaExterna: valor.portaExterna,

      dominio:
        valor.tipo === 'HTTP'
          ? valor.dominio
          : undefined,

      memoriaLimite: valor.memoriaLimite,

      cpuLimite: valor.cpuLimite
    };

    this.aplicacaoService
      .criarAplicacao(request)
      .subscribe({

        next: (resposta) => {

          console.log(
            'Aplicação criada:',
            resposta
          );

          this.salvando = false;

          this.cancelarConfiguracao();
        },

        error: (erro) => {

          console.error(
            'Erro ao criar aplicação:',
            erro
          );

          this.salvando = false;
        }

      });
  }

  /**
   * Limpa o formulário.
   */
  cancelarConfiguracao(): void {

    this.repositorioSelecionado = null;

    this.formulario.reset({
      repositorioId: '',
      nome: '',
      agenteId: '',
      tipo: 'HTTP',
      portaContainer: '',
      portaExterna: '',
      dominio: '',
      memoriaLimite: 512,
      cpuLimite: 1
    });
  }
}