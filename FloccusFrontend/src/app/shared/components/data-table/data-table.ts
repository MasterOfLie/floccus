import { Component, input, output } from '@angular/core';
import { Pagination } from '../pagination/pagination';
import { DataTableColumn } from '../../models/DataTableColumn';
import { NgTemplateOutlet } from '@angular/common';

@Component({
  selector: 'app-data-table',
  imports: [Pagination, NgTemplateOutlet],
  templateUrl: './data-table.html',
  styleUrl: './data-table.css',
})

export class DataTable<T extends Record<string, any>> {

  columns = input<DataTableColumn<T>[]>([]);

  data = input<T[]>([]);

  loading = input(false);

  emptyMessage = input(
    'Nenhum registro encontrado.'
  );

  paginaAtual = input(0);

  totalPaginas = input(0);

  totalElementos = input(0);

  paginaChange = output<number>();


  valorLinha(
    item: T,
    coluna: DataTableColumn<T>
  ): any {

    return item[coluna.key];

  }


  alterarPagina(pagina: number): void {

    this.paginaChange.emit(pagina);

  }

}
