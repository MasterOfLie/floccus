import { Component, inject, OnInit, signal, TemplateRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AgenteService } from '../../core/services/agente-service';
import { Agente } from './models/AgenteModel';
import { DataTableColumn } from '../../shared/models/DataTableColumn';
import { DataTable } from '../../shared/components/data-table/data-table';
import { Router, RouterLink } from '@angular/router';


@Component({
  selector: 'app-agentes',
  standalone: true,
  imports: [CommonModule, DataTable, RouterLink],
  templateUrl: './agentes.html',
  styleUrl: './agentes.css'
})
export class Agentes implements OnInit {
  private readonly agenteService = inject(AgenteService);
  private router = inject(Router);
  pagina = signal(0);

  tamanhoPagina = signal(10);

  agentes = signal<Agente[]>([]);

  carregando = signal(false);

  totalPaginas = signal(0);

  totalElementos = signal(0);
  @ViewChild('acoesTemplate')
  acoesTemplate!: TemplateRef<any>;

  colunas: DataTableColumn<Agente>[] = [

    {
      key: 'id',
      label: 'ID'
    },

    {
      key: 'ipAgente',
      label: 'IP'
    },

    {
      key: 'portaAgente',
      label: 'Porta'
    },

    {
      key: 'chaveAgente',
      label: 'Chave'
    }

  ];

  ngAfterViewInit(): void {

    this.colunas.push({
      key: 'acoes',
      label: 'Ações',
      template: this.acoesTemplate
    });
  }
  ngOnInit(): void {
    this.carregarAgentes();
  }

  abrirAgente(agente: Agente): void {
    this.router.navigate(['/agentes', agente.id]);
  }

  alterarPagina(pagina: number): void {

    this.pagina.set(pagina);

    this.carregarAgentes();

  }

  carregarAgentes(): void {

    this.carregando.set(true);

    this.agenteService
      .listar(
        this.pagina(),
        this.tamanhoPagina()
      )
      .subscribe({

        next: (resposta) => {

          this.agentes.set(
            resposta.content
          );

          this.totalPaginas.set(
            resposta.totalPages
          );

          this.totalElementos.set(
            resposta.totalElements
          );

          this.carregando.set(false);

        },

        error: (erro) => {

          console.error(
            'Erro ao carregar agentes:',
            erro
          );

          this.carregando.set(false);

        }

      });
  }
}