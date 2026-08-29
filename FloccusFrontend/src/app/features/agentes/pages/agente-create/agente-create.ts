import { Component, inject, signal } from '@angular/core';
import { AgenteService } from '../../../../core/services/agente-service';
import { CriarAgenteRequest } from '../../models/CriarAgenteRequest';
import { FormsModule } from '@angular/forms';
import { AlertaService } from '../../../../core/services/alerta-service';

@Component({
  selector: 'app-agente-create',
  imports: [FormsModule],
  templateUrl: './agente-create.html',
  styleUrl: './agente-create.css',
})
export class AgenteCreate {


  private readonly agenteService = inject(AgenteService);
  private readonly alertaService = inject(AlertaService);
  agente: CriarAgenteRequest = {
    ipAgente: 'localhost',
    portaAgente: 50051,
    ipBackend: 'localhost',
    portaBackend: "8080",
    dockerHost: 'unix:///var/run/docker.sock'
  };
  carregando = signal(false);
  criarAgente(): void {

    this.carregando.set(true);

    this.agenteService
      .criarAgente(this.agente)
      .subscribe({

        next: (agenteCriado) => {
          this.carregando.set(false);
          this.alertaService.sucesso('Agente criado com sucesso');
        },

        error: (error) => {
          this.alertaService.erro('Erro ao criar agente: ' + error.message);

          this.carregando.set(false);

        }

      });

  }

}
