import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-pagination',
  imports: [],
  templateUrl: './pagination.html',
  styleUrl: './pagination.css',
})
export class Pagination {

  paginaAtual = input(0);
  totalPaginas = input(0);
  totalElementos = input(0);

  paginaChange = output<number>();


  irParaPagina(pagina: number): void {

    if (
      pagina < 0 ||
      pagina >= this.totalPaginas() ||
      pagina === this.paginaAtual()
    ) {
      return;
    }

    this.paginaChange.emit(pagina);
  }


  paginaAnterior(): void {
    this.irParaPagina(this.paginaAtual() - 1);
  }


  proximaPagina(): void {
    this.irParaPagina(this.paginaAtual() + 1);
  }


  paginas(): number[] {

    const total = this.totalPaginas();
    const atual = this.paginaAtual();

    const resultado: number[] = [];

    const inicio = Math.max(0, atual - 2);
    const fim = Math.min(total - 1, atual + 2);

    for (let i = inicio; i <= fim; i++) {
      resultado.push(i);
    }

    return resultado;
  }
}
