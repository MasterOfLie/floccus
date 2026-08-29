import {
  AfterViewInit,
  Component,
  OnInit,
  TemplateRef,
  ViewChild,
  inject,
  signal
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ContainerService } from '../../core/services/container-service';
import { Container } from '../containers/models/ContainerModel';
import { DataTable } from '../../shared/components/data-table/data-table';
import { DataTableColumn } from '../../shared/models/DataTableColumn';

@Component({
  selector: 'app-containers',
  standalone: true,
  imports: [
    CommonModule,
    DataTable
  ],
  templateUrl: './containers.html',
  styleUrl: './containers.css'
})
export class Containers implements OnInit, AfterViewInit {
  private containerService = inject(ContainerService);
  private router = inject(Router);

  pagina = signal(0);
  tamanhoPagina = signal(10);
  containers = signal<Container[]>([]);
  carregando = signal(false);
  totalPaginas = signal(0);
  totalElementos = signal(0);

  @ViewChild('statusTemplate') statusTemplate!: TemplateRef<any>;
  @ViewChild('acoesTemplate') acoesTemplate!: TemplateRef<any>;

  colunas: DataTableColumn<Container>[] = [];

  ngOnInit(): void {
    this.listarContainers();
  }

  ngAfterViewInit(): void {
    this.colunas = [
      {
        key: 'id',
        label: 'ID'
      },
      {
        key: 'nome',
        label: 'Nome'
      },
      {
        key: 'image',
        label: 'Imagem'
      },
      {
        key: 'status',
        label: 'Status',
        template: this.statusTemplate
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
    this.containerService
      .listar(this.pagina(), this.tamanhoPagina())
      .subscribe({
        next: (resposta) => {
          console.log('Containers recebidos:', resposta);
          this.containers.set(resposta.content);
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

  abrirContainer(container: Container): void {
    this.router.navigate(['/containers', container.id]);
  }
}