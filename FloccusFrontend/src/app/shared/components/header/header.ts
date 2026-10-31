import { Component, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth-service';
import { SidebarService } from '../../../core/services/sidebar';
import { Perfil } from '../perfil/perfil';

@Component({
  selector: 'app-header',
  imports: [Perfil],
  templateUrl: './header.html',
  styleUrl: './header.css',
})

export class Header implements OnInit {

  private sidebarService = inject(SidebarService);
  private authService = inject(AuthService);
  private router = inject(Router);
  perfilModalAberta = signal(false);

  collapsed = this.sidebarService.collapsed;
  accountMenuOpen = signal(false);
  usuario: any;

  toggleSidebar(): void {
    this.sidebarService.toggle();
  }

  toggleMobileSidebar(): void {
    this.sidebarService.toggleMobile();
  }


  ngOnInit(): void {
    const usuarioStorage = localStorage.getItem('floccus_user');
    if (usuarioStorage) {
      this.usuario = JSON.parse(usuarioStorage);
    }

  }

  toggleAccountMenu(): void {
    this.accountMenuOpen.update(value => !value);
  }

  abrirPerfil(): void {
    this.accountMenuOpen.set(false);
    this.router.navigate(['/perfil']);
  }

  abrirConfiguracoes(): void {
    this.accountMenuOpen.set(false);
    this.router.navigate(['/configuracoes']);
  }

  abrirContasRecursos(): void {
    this.accountMenuOpen.set(false);
    this.router.navigate(['/contas-recursos']);
  }

  logout(): void {
    this.accountMenuOpen.set(false);
    this.authService.logout();
  }

  isAdmin(): boolean {
    return this.usuario?.permissoes?.includes('ROLE_ADMIN') ?? false;
  }
  atualizarDadosUsuario(perfilAtualizado: any): void {
    this.usuario = {
      ...this.usuario,
      nome: perfilAtualizado.nome,
      email: perfilAtualizado.email
    };
    localStorage.removeItem('floccus_user');
    localStorage.removeItem('floccus_token');
    this.router.navigate(['/login']);
  }
}
