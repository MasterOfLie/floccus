import { HttpClient } from '@angular/common/http';
import { Component, EventEmitter, inject, Input, Output, signal, SimpleChanges } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { AlertaService } from '../../../core/services/alerta-service';

@Component({
  selector: 'app-perfil',
  imports: [ReactiveFormsModule],
  templateUrl: './perfil.html',
  styleUrl: './perfil.css',
})
export class Perfil {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private alertaservice = inject(AlertaService)
  @Input() visible = false;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() perfilAtualizado = new EventEmitter<PerfilResponse>();
  carregando = signal(false);
  salvando = signal(false);
  erro = signal('');
  sucesso = signal('');
  mostrarSenha = signal(false);

  form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(150)]],
    email: ['', [Validators.required, Validators.email]],
    senha: ['']
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible'] && this.visible) {
      this.carregarPerfil();
    }
  }
  carregarPerfil(): void {
    this.carregando.set(true);
    this.erro.set('');
    this.sucesso.set('');

    this.http.get<PerfilResponse>('/api/usuarios/me')
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (perfil) => {
          this.form.patchValue({
            nome: perfil.nome,
            email: perfil.email,
            senha: ''
          });
        },
        error: () => {
          this.erro.set('Não foi possível carregar os dados do perfil.');
        }
      });
  }

  salvar(): void {
    this.erro.set('');
    this.sucesso.set('');

    if (this.form.invalid || this.salvando()) {
      this.form.markAllAsTouched();
      return;
    }

    const valores = this.form.getRawValue();

    const dto: AtualizaUsuarioDTO = {
      nome: valores.nome.trim(),
      email: valores.email.trim(),
      senha: valores.senha.trim() || null
    };

    this.salvando.set(true);

    this.http.put<PerfilResponse>('/api/usuarios/me', dto)
      .pipe(finalize(() => this.salvando.set(false)))
      .subscribe({
        next: (perfil) => {
          this.alertaservice.sucesso('Perfil atualizado com sucesso. Por favor faça login novamente.');
          this.perfilAtualizado.emit(perfil);

          this.fechar();
        },
        error: (err) => {
          this.alertaservice.erro(err.erro.message);
        }
      });
  }

  fechar(): void {
    if (this.salvando()) return;

    this.visible = false;
    this.visibleChange.emit(false);
    this.erro.set('');
    this.sucesso.set('');
    this.mostrarSenha.set(false);
  }

  alternarSenha(): void {
    this.mostrarSenha.update(valor => !valor);
  }
}
