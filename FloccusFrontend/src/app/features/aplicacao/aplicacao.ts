import { CommonModule } from '@angular/common';
import {
  AfterViewInit,
  Component,
  OnInit,
  TemplateRef,
  ViewChild,
  inject,
  signal
} from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AplicacaoService } from '../../core/services/aplicacao-service';
import { DataTable } from '../../shared/components/data-table/data-table';
import { DataTableColumn } from '../../shared/models/DataTableColumn';
import { AplicacaoModel } from './models/AplicacaoModel';

@Component({
  selector: 'app-aplicacao',
  standalone: true,
  imports: [
    CommonModule,
    DataTable,
    RouterLink
  ],
  templateUrl: './aplicacao.html',
  styleUrl: './aplicacao.css'
})
export class Aplicacao implements OnInit, AfterViewInit {
  private aplicacaoService = inject(AplicacaoService);
  private router = inject(Router);

  pagina = signal(0);
  tamanhoPagina = signal(10);
  aplicacoes = signal<AplicacaoModel[]>([]);
  carregando = signal(false);
  totalPaginas = signal(0);
  totalElementos = signal(0);

  @ViewChild('statusTemplate') statusTemplate!: TemplateRef<any>;
  @ViewChild('acoesTemplate') acoesTemplate!: TemplateRef<any>;
  @ViewChild('memoriaTemplate') memoriaTemplate!: TemplateRef<any>;

  colunas: DataTableColumn<AplicacaoModel>[] = [];

  ngOnInit(): void {
    this.listarContainers();
  }

  ngAfterViewInit(): void {
    this.colunas = [
      {
        key: 'nome',
        label: 'Nome'
      },
      {
        key: 'imagem',
        label: 'Imagem'
      },
      {
        key: 'memoriaLimite',
        label: 'Memória',
        template: this.memoriaTemplate
      },
      {
        key: 'cpuLimite',
        label: 'CPU'
      },
      {
        key: 'tipo',
        label: 'Tipo'
      },
      {
        key: 'id',
        label: 'Ações',
        template: this.acoesTemplate
      }
    ];
  }

  alterarPagina(pagina: number): void {
    this.pagina.set(pagina);
    this.listarContainers();
  }

  listarContainers(): void {
    this.carregando.set(true);
    this.aplicacaoService
      .listar(this.pagina(), this.tamanhoPagina())
      .subscribe({
        next: (resposta) => {
          console.log('Containers recebidos:', resposta);
          this.aplicacoes.set(resposta.content);
          this.totalPaginas.set(resposta.totalPages);
          this.totalElementos.set(resposta.totalElements);
          this.carregando.set(false);
        },
        error: (error) => {
          console.error('Erro ao buscar containers:', error);
          this.carregando.set(false);
        }
      });
  }

  traduzirStatus(status: string): string {
    if (!status) {
      return 'Desconhecido';
    }

    const statusMin = status.toLowerCase();

    if (statusMin.includes('up') || statusMin.includes('running')) {
      return 'Em execução';
    }

    if (statusMin.includes('exited')) {
      return 'Parado';
    }

    return status;
  }

  estaRodando(status: string): boolean {
    if (!status) {
      return false;
    }

    const statusMin = status.toLowerCase();
    return statusMin.includes('up') || statusMin.includes('running');
  }

  abrirContainer(aplicacao: AplicacaoModel): void {
    this.router.navigate(['/aplicacoes', aplicacao.id]);
  }
  formatarMemoria(memoria: number): string {
    if (!memoria) {
      return '0 MB';
    }

    if (memoria >= 1024) {
      const gb = memoria / 1024;
      return `${gb.toFixed(1)} GB`;
    }

    return `${memoria} MB`;
  }
}