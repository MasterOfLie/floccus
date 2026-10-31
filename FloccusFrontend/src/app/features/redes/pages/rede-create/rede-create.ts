import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { NgSelectComponent } from '@ng-select/ng-select';
import { AgenteService } from '../../../../core/services/agente-service';
import { AlertaService } from '../../../../core/services/alerta-service';
import { RedeService } from '../../../../core/services/rede-service';
import { Agente } from '../../../agentes/models/AgenteModel';
import { RedeModel } from '../../models/Rede';


@Component({
  selector: 'app-rede-create',
  imports: [RouterLink, FormsModule, NgSelectComponent],
  templateUrl: './rede-create.html',
  styleUrl: './rede-create.css',
})
export class RedeCreate implements OnInit {

  carregando = signal(false);

  private readonly agenteService = inject(AgenteService);
  private readonly router = inject(Router);
  private readonly alertaService = inject(AlertaService);
  private readonly redeService = inject(RedeService);

  rede: RedeModel = {
    nome: '',
    subnet: null,
    gateway: null,
    agenteId: '',
    nomeAgente: '',
  };

  agentes: Agente[] = [];


  ngOnInit(): void {
    this.carregando.set(true);
    this.carregarAgentes();
  }

  carregarAgentes(): void {
    this.agenteService.listar(0, 100, false).subscribe({
      next: (response) => {
        this.agentes = response as any;
        this.carregando.set(false);
      },
      error: (erro) => {
        this.alertaService.erro('Erro ao carregar agentes', erro.error.message);
      }
    });
  }

  alterarAgente(agenteId: string): void {
    const agente = this.agentes.find((agente) => agente.id === agenteId);
    if (agente) {
      this.rede.agenteId = agente.id;
    }
  }


  criarRede(): void {
    this.redeService.criar(this.rede).subscribe({
      next: () => {
        this.alertaService.sucesso('Rede criada com sucesso');
        this.router.navigate(['/redes']);
      },
      error: (erro) => {
        this.alertaService.erro(erro.error.message);
      }
    });
  }
}