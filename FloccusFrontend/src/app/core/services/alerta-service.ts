import { Injectable } from '@angular/core';
import Swal, { SweetAlertResult } from 'sweetalert2';

@Injectable({
    providedIn: 'root'
})
export class AlertaService {

    private swalFloccus = Swal.mixin({
        width: '380px', // Caixa ainda mais compacta e elegante
        background: 'var(--floccus-surface, #ffffff)',
        color: 'var(--floccus-text, #18181b)',
        buttonsStyling: false,
        customClass: {
            popup: 'floccus-swal-popup',
            title: 'floccus-swal-title',
            htmlContainer: 'floccus-swal-text',
            actions: 'floccus-swal-actions',
            confirmButton: 'btn btn-primary swal-btn-custom',
            cancelButton: 'btn btn-cancelar swal-btn-custom',
            icon: 'floccus-swal-icon' // Classe para o ícone modal
        }
    });

    private toastFloccus = Swal.mixin({
        toast: true,
        position: 'top-end',
        showConfirmButton: false,
        timer: 3000,
        timerProgressBar: true,
        background: 'var(--floccus-surface, #ffffff)',
        color: 'var(--floccus-text, #18181b)',
        customClass: {
            popup: 'floccus-toast-popup',
            title: 'floccus-toast-title',
            htmlContainer: 'floccus-toast-text',
            icon: 'floccus-toast-icon' // Classe para o ícone toast
        }
    });

    erro(mensagem: string, titulo = 'Erro'): void {
        this.swalFloccus.fire({
            icon: 'error',
            iconHtml: '<i class="bi bi-x-circle"></i>', // Ícone limpo Bootstrap
            title: titulo,
            text: mensagem,
            confirmButtonText: 'OK'
        });
    }

    sucesso(mensagem: string, titulo = 'Sucesso'): void {
        this.swalFloccus.fire({
            icon: 'success',
            iconHtml: '<i class="bi bi-check-circle"></i>', // Ícone limpo Bootstrap
            title: titulo,
            text: mensagem,
            confirmButtonText: 'OK'
        });
    }

    sucessoDeploy(
        mensagem: string,
        titulo = 'Sucesso'
    ): Promise<SweetAlertResult> {
        return this.swalFloccus.fire({
            icon: 'success',
            iconHtml: '<i class="bi bi-check-circle"></i>',
            title: titulo,
            text: mensagem,
            confirmButtonText: 'OK'
        });
    }
    erroToast(mensagem: string): void {
        this.toastFloccus.fire({
            icon: 'error',
            iconHtml: '<i class="bi bi-x-circle-fill"></i>',
            html: mensagem || 'Erro desconhecido'
        });
    }

    sucessoToast(mensagem: string): void {
        this.toastFloccus.fire({
            icon: 'success',
            iconHtml: '<i class="bi bi-check-circle-fill"></i>',
            html: mensagem || 'Sucesso'
        });
    }

    confirmar(mensagem: string, titulo = 'Confirmação'): Promise<boolean> {
        return this.swalFloccus.fire({
            icon: 'warning',
            iconHtml: '<i class="bi bi-exclamation-triangle"></i>',
            title: titulo,
            text: mensagem,
            showCancelButton: true,
            confirmButtonText: 'Sim',
            cancelButtonText: 'Não'
        }).then((result) => {
            return result.isConfirmed;
        });
    }
}