import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AlertaService } from '../../../core/services/alerta-service';
import { AuthService } from '../../../core/services/auth-service';

@Component({
  selector: 'app-redefinir-senha',
  imports: [RouterLink, FormsModule],
  templateUrl: './redefinir-senha.html',
  styleUrl: './redefinir-senha.css',
})
export class RedefinirSenha {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authService = inject(AuthService);
  private alertaService = inject(AlertaService);

  token = '';
  novaSenha = '';
  confirmarSenha = '';
  mostrarSenha = signal(false);
  carregando = signal(false);
  tokenValido = signal(true);
  anoAtual = new Date().getFullYear();
  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.token = params['token'];
      if (!this.token) {
        this.tokenValido.set(false);
        this.alertaService.erroToast('Token de recuperação inválido ou ausente.');
      }
    });
  }

  alternarSenha(): void {
    this.mostrarSenha.update(valor => !valor);
  }

  salvarNovaSenha(): void {
    if (!this.novaSenha) {
      this.alertaService.erroToast('Preencha todos os campos.');
      return;
    }


    if (this.novaSenha.length < 6) {
      this.alertaService.erroToast('A nova senha deve ter pelo menos 6 caracteres.');
      return;
    }

    this.carregando.set(true);

    this.authService.redefinirSenha(this.token, this.novaSenha).subscribe({
      next: () => {
        this.carregando.set(false);
        this.alertaService.sucessoToast('Senha redefinida com sucesso!');
        this.router.navigate(['/login']);
      },
      error: (erro) => {
        this.carregando.set(false);
        const mensagem = erro.error?.message || 'Não foi possível redefinir a senha. O token pode ter expirado.';
        this.alertaService.erroToast(mensagem);
      }
    });
  }
}
