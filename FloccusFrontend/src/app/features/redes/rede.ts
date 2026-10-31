import { CommonModule } from '@angular/common';
import { Component, inject, signal, TemplateRef, ViewChild } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AlertaService } from '../../core/services/alerta-service';
import { RedeService } from '../../core/services/rede-service';
import { DataTable } from '../../shared/components/data-table/data-table';
import { DataTableColumn } from '../../shared/models/DataTableColumn';
import { RedeModel } from './models/Rede';

@Component({
  selector: 'app-rede',
  imports: [
    CommonModule,
    DataTable,
    RouterLink
  ],
  templateUrl: './rede.html',
  styleUrl: './rede.css',
})
export class Rede {
  private readonly redeService = inject(RedeService);
  private readonly router = inject(Router);
  private readonly alertaService = inject(AlertaService)

  pagina = signal(0);
  tamanhoPagina = signal(10);

  redes = signal<RedeModel[]>([]);


  totalPaginas = signal(0);
  totalElementos = signal(0);

  @ViewChild('acoesTemplate')
  acoesTemplate!: TemplateRef<any>;

  colunas: DataTableColumn<RedeModel>[] = [
    {
      key: 'nome',
      label: 'Nome'
    },
    {
      key: 'nomeAgente',
      label: 'Agente'
    },
    {
      key: 'subnet',
      label: 'Sub-rede'
    },
    {
      key: 'gateway',
      label: 'Gateway'
    }
  ];

  ngOnInit(): void {
    this.carregarRedes();
  }

  ngAfterViewInit(): void {
    this.colunas.push({
      key: 'acoes',
      label: 'Ações',
      template: this.acoesTemplate
    });
  }

  deletarRede(rede: RedeModel): void {

    this.alertaService.confirmar(
      `Deseja remover a rede ${rede.nome}?`
    )
      .then((result) => {

        if (!result) return;

        this.redeService.deletar(rede.id!).subscribe({

          next: () => {

            this.carregarRedes();

          },

          error: (erro) => {
            this.alertaService.erro(erro.error.message)
          }

        });

      });
  }

  alterarPagina(pagina: number): void {
    this.pagina.set(pagina);
    this.carregarRedes();
  }

  carregarRedes(): void {

    this.redeService
      .listar(
        this.pagina(),
        this.tamanhoPagina()
      )
      .subscribe({
        next: (resposta) => {

          this.redes.set(
            resposta.content
          );

          this.totalPaginas.set(
            resposta.totalPages
          );

          this.totalElementos.set(
            resposta.totalElements
          );

        },

        error: (erro) => {
          this.alertaService.erro(erro.error.message)
        }
      });
  }
}
