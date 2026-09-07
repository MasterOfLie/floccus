import { Component, inject, OnInit, signal, TemplateRef, ViewChild } from '@angular/core';
import { Observable } from 'rxjs';
import { GithubService } from '../../../core/services/github-service';
import { ActivatedRoute } from '@angular/router';
import { AlertaService } from '../../../core/services/alerta-service';
import { GithubDTO } from './models/GithubModel';
import { DataTableColumn } from '../../../shared/models/DataTableColumn';
import { CommonModule } from '@angular/common';
import { DataTable } from '../../../shared/components/data-table/data-table';

@Component({
  selector: 'app-github',
  imports: [
    CommonModule,
    DataTable
  ],
  templateUrl: './github.html',
  styleUrl: './github.css',
})
export class Github implements OnInit {
  private readonly githubService = inject(GithubService);
  private readonly route = inject(ActivatedRoute);
  private readonly alertaService = inject(AlertaService);

  pagina = signal(0);
  tamanhoPagina = signal(10);

  githubApps = signal<GithubDTO[]>([]);

  carregando = signal(false);
  totalPaginas = signal(0);
  totalElementos = signal(0);

  @ViewChild('acoesTemplate')
  acoesTemplate!: TemplateRef<any>;

  colunas: DataTableColumn<GithubDTO>[] = [];

  ngOnInit(): void {

    const installationId =
      this.route.snapshot.queryParamMap.get('installation_id');

    const githubId =
      this.route.snapshot.queryParamMap.get('state');

    if (installationId && githubId) {

      this.githubService
        .salvarInstalacao(installationId, githubId)
        .subscribe({
          next: (response) => {

            this.alertaService.sucesso(response.message);

            this.listarGithubApps();
          },

          error: (error) => {

            console.error(
              'Erro ao salvar instalação:',
              error
            );

            this.alertaService.erro(
              error?.error?.message ?? 'Erro ao salvar instalação.'
            );
          }
        });

    } else {

      console.log('Carregamento normal');

      this.listarGithubApps();
    }
  }

  ngAfterViewInit(): void {

    this.colunas = [

      {
        key: 'nomeApp',
        label: 'Aplicação'
      },

      {
        key: 'appId',
        label: 'App ID'
      },

      {
        key: 'clienteId',
        label: 'Client ID'
      },

      {
        key: 'instalacaoId',
        label: 'Instalação'
      },

      {
        key: 'nomeUsuario',
        label: 'Usuário'
      },

      {
        key: 'id',
        label: 'Ações',
        template: this.acoesTemplate
      }

    ];
  }

  listarGithubApps(): void {

    this.carregando.set(true);

    this.githubService
      .listar(this.pagina(), this.tamanhoPagina())
      .subscribe({

        next: (resposta) => {


          this.githubApps.set(resposta.content);

          this.totalPaginas.set(resposta.totalPages);

          this.totalElementos.set(resposta.totalElements);

          this.carregando.set(false);
        },

        error: (error) => {
          this.alertaService.erro(
            error?.error?.message ?? 'Erro ao listar GitHub Apps.'
          );
          this.carregando.set(false);
        }
      });
  }
  removerGithub(app: GithubDTO): void {

    this.alertaService.confirmar(
      `Deseja remover a aplicação ${app.nomeApp}?`
    )
      .then((result) => {

        if (!result) return;

        this.githubService.remover(app.id).subscribe({

          next: (response) => {

            this.alertaService.sucesso(response.message);

            this.listarGithubApps();

          },

          error: (error) => {
            this.alertaService.erro(
              error?.error?.message ?? 'Erro ao remover aplicação.'
            );

          }

        });

      });
  }
  alterarPagina(pagina: number): void {

    this.pagina.set(pagina);

    this.listarGithubApps();
  }
  iniciarIntegracao(): void {

    this.githubService.conectarGitHub()
      .subscribe({

        next: (response) => {

          const form = document.createElement('form');

          form.method = 'POST';
          form.action =
            'https://github.com/settings/apps/new?state='
            + encodeURIComponent(response.state);

          const inputManifest = document.createElement('input');

          inputManifest.type = 'hidden';
          inputManifest.name = 'manifest';
          inputManifest.value = response.manifest;

          form.appendChild(inputManifest);

          document.body.appendChild(form);

          form.submit();
        },

        error: (error) => {
          console.error(
            'Erro ao iniciar integração com GitHub',
            error
          );
        }
      });
  }
}