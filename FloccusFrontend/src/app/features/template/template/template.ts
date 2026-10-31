import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AlertaService } from '../../../core/services/alerta-service';
import { TemplateService } from '../../../core/services/template-service';
import { TemplateDTO, Template as TemplateModel } from '../../../shared/models/Template';

@Component({
  selector: 'app-template',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './template.html',
  styleUrl: './template.css',
})
export class Template {


  private readonly fb = inject(FormBuilder);
  private readonly templateService = inject(TemplateService);
  private readonly alertaService = inject(AlertaService);

  templates = signal<TemplateModel[]>([]);

  carregando = signal(false);
  salvando = signal(false);
  editando = signal(false);

  templateSelecionado = signal<TemplateModel | null>(null);

  formulario: FormGroup = this.fb.group({
    linguagem: ['', Validators.required],
    imagem: ['', Validators.required],
    framework: [''],
    runtime: ['', Validators.required],
    buildTool: [''],
    versao: [''],
    buildCommand: ['', Validators.required],
    startCommand: ['', Validators.required]
  });

  ngOnInit(): void {
    this.carregarTemplates();
  }

  carregarTemplates(): void {
    this.carregando.set(true);

    this.templateService.listar().subscribe({
      next: (templates) => {
        this.templates.set(templates);
        this.carregando.set(false);
      },

      error: () => {
        this.carregando.set(false);

        this.alertaService.erro(
          'Não foi possível carregar os templates.'
        );
      }
    });
  }

  novoTemplate(): void {
    this.editando.set(false);
    this.templateSelecionado.set(null);

    this.formulario.reset({
      linguagem: '',
      imagem: '',
      framework: '',
      runtime: '',
      buildTool: '',
      versao: '',
      buildCommand: '',
      startCommand: ''
    });
  }

  editarTemplate(template: TemplateModel): void {
    this.editando.set(true);
    this.templateSelecionado.set(template);

    this.formulario.patchValue({
      linguagem: template.linguagem,
      imagem: template.imagem,
      framework: template.framework,
      runtime: template.runtime,
      buildTool: template.buildTool,
      versao: template.versao,
      buildCommand: template.buildCommand,
      startCommand: template.startCommand
    });
  }

  salvar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.salvando.set(true);

    const dados: TemplateDTO = this.formulario.getRawValue();

    const template = this.templateSelecionado();

    if (this.editando() && template?.id) {
      this.atualizarTemplate(template.id, dados);
      return;
    }

    this.criarTemplate(dados);
  }

  private criarTemplate(dados: TemplateDTO): void {
    this.templateService.criar(dados).subscribe({
      next: (novoTemplate) => {
        this.templates.update(templates => [
          ...templates,
          novoTemplate
        ]);

        this.salvando.set(false);

        this.alertaService.sucesso(
          'O template foi criado com sucesso.',
          'Template criado'
        );

        this.novoTemplate();
      },

      error: () => {
        this.salvando.set(false);

        this.alertaService.erro(
          'Não foi possível criar o template.'
        );
      }
    });
  }

  private atualizarTemplate(
    id: string,
    dados: TemplateDTO
  ): void {
    this.templateService.atualizar(id, dados).subscribe({
      next: (templateAtualizado) => {
        this.templates.update(templates =>
          templates.map(template =>
            template.id === templateAtualizado.id
              ? templateAtualizado
              : template
          )
        );

        this.salvando.set(false);

        this.alertaService.sucesso(
          'O template foi atualizado com sucesso.',
          'Template atualizado'
        );

        this.novoTemplate();
      },

      error: () => {
        this.salvando.set(false);

        this.alertaService.erro(
          'Não foi possível atualizar o template.'
        );
      }
    });
  }

  excluirTemplate(template: TemplateModel): void {
    if (!template.id) {
      return;
    }

    this.alertaService
      .confirmar(
        `O template "${template.linguagem}" será excluído permanentemente.`,
        'Excluir template?'
      )
      .then(confirmado => {
        if (!confirmado) {
          return;
        }

        this.templateService
          .excluir(template.id)
          .subscribe({
            next: () => {
              this.templates.update(templates =>
                templates.filter(t => t.id !== template.id)
              );

              if (
                this.templateSelecionado()?.id === template.id
              ) {
                this.novoTemplate();
              }

              this.alertaService.sucesso(
                'O template foi excluído com sucesso.',
                'Template excluído'
              );
            },

            error: () => {
              this.alertaService.erro(
                'Não foi possível excluir o template.'
              );
            }
          });
      });
  }

  cancelarEdicao(): void {
    this.novoTemplate();
  }
}
