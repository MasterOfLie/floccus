import { Component, ElementRef, OnDestroy, AfterViewInit, viewChild, input, inject } from '@angular/core';
import { Terminal } from 'xterm';
import { FitAddon } from 'xterm-addon-fit';
import { Subscription } from 'rxjs';
import { ContainerService } from '../../../core/services/container-service';

@Component({
  selector: 'app-terminal-logs',
  imports: [],
  templateUrl: './terminal-logs.html',
  styleUrl: './terminal-logs.css',
})
export class TerminalLogs implements AfterViewInit, OnDestroy {

  containerId = input.required<string>();
  terminalContainer = viewChild.required<ElementRef<HTMLDivElement>>('terminalContainer');

  // Injeção de dependência moderna
  private logService = inject(ContainerService);

  private term!: Terminal;
  private fitAddon!: FitAddon;
  private logSubscription?: Subscription;

  ngAfterViewInit() {
    this.term = new Terminal({
      cursorBlink: true,
      theme: { background: '#1e1e1e', foreground: '#d4d4d4' },
      fontFamily: '"Consolas", monospace',
      fontSize: 14
    });

    this.fitAddon = new FitAddon();
    this.term.loadAddon(this.fitAddon);

    // O Signal terminalContainer() retorna o ElementRef
    this.term.open(this.terminalContainer().nativeElement);
    this.fitAddon.fit();

    this.term.writeln('\x1b[33mConectando ao console...\x1b[0m');

    // Conecta usando o valor do Signal containerId()
    this.logSubscription = this.logService.conectar(this.containerId()).subscribe({
      next: (linha: string) => {
        this.term.write(linha);
        this.term.scrollToBottom();
      },
      error: () => this.term.writeln('\r\n\x1b[31m[Erro] Conexão perdida com o servidor.\x1b[0m')
    });

    window.addEventListener('resize', this.onResize);
  }

  private onResize = () => {
    this.fitAddon.fit();
  };

  ngOnDestroy() {
    window.removeEventListener('resize', this.onResize);
    this.logSubscription?.unsubscribe();
    this.logService.desconectar(this.containerId());
    this.term?.dispose();
  }

}
