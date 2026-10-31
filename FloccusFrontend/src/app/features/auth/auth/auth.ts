import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RegisterRequest } from '../../../core/config/auth/models/AuthModel';
import { AlertaService } from '../../../core/services/alerta-service';
import { AuthService } from '../../../core/services/auth-service';

@Component({
  selector: 'app-auth',
  imports: [FormsModule],
  templateUrl: './auth.html',
  styleUrl: './auth.css',
})
export class Auth {


  private authService = inject(AuthService);
  private alertaService = inject(AlertaService);

  modo: 'login' | 'registro' | 'recuperar' = 'login';

  mostrarSenha = false;
  carregando = false;

  erro = '';

  anoAtual = new Date().getFullYear();

  loginData = {
    email: '',
    password: '',
    remember: false
  };

  registroData = {
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
    terms: false
  };

  recuperacaoData = {
    email: '',
    token: '',
    novaSenha: '',
    confirmarSenha: ''
  };


  alterarModo(novoModo: 'login' | 'registro' | 'recuperar'): void {
    this.modo = novoModo;
    this.erro = '';
    this.mostrarSenha = false;
  }


  login(): void {

    this.erro = '';

    if (!this.loginData.email || !this.loginData.password) {
      this.alertaService.erroToast('Informe o e-mail e a senha.');
      return;
    }

    this.carregando = true;

    this.authService.login({
      email: this.loginData.email,
      senha: this.loginData.password
    }).subscribe({
      next: () => {
        this.carregando = false;
        window.location.href = '/aplicacoes';
      },

      error: (erro) => {
        this.carregando = false;
        this.alertaService.erroToast(erro.error.message)
        this.erro =
          erro.error?.message ||
          'E-mail ou senha inválidos.';
      }
    });
  }


  registrar(): void {
    this.erro = '';
    if (
      !this.registroData.name ||
      !this.registroData.email ||
      !this.registroData.password ||
      !this.registroData.confirmPassword
    ) {
      this.alertaService.erroToast('Preencha todos os campos.');
      return;
    }

    if (this.registroData.password !== this.registroData.confirmPassword) {
      this.alertaService.erroToast('As senhas não coincidem.');
      return;
    }

    if (!this.registroData.terms) {
      this.alertaService.erroToast('Você precisa aceitar os termos de uso.');
      return;
    }

    this.carregando = true;

    const dados: RegisterRequest = {
      nome: this.registroData.name,
      email: this.registroData.email,
      senha: this.registroData.password
    };

    this.authService.register(dados).subscribe({
      next: () => {
        this.carregando = false;
        window.location.href = '/aplicacoes';
      },

      error: (erro) => {
        this.carregando = false;

        this.erro =
          erro.error?.message ||
          'Não foi possível criar sua conta.';
      }
    });
  }
  solicitarRecuperacao(): void {
    this.erro = '';

    if (!this.recuperacaoData.email) {
      this.alertaService.erroToast('Informe o seu e-mail.');
      return;
    }

    this.carregando = true;


    this.authService.solicitarRecuperacao(this.recuperacaoData.email).subscribe({
      next: () => {
        this.carregando = false;
        this.alertaService.sucessoToast('E-mail de recuperação enviado com sucesso! Verifique sua caixa de entrada.');
        this.alterarModo('login');
        this.recuperacaoData.email = '';
      },
      error: (erro) => {
        this.carregando = false;
        const mensagem = erro.error?.message || 'Não foi possível enviar o e-mail de recuperação.';
        this.alertaService.erroToast(mensagem);
        this.erro = mensagem;
      }
    });
  }
}
