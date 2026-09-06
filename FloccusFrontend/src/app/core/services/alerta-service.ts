import { Injectable } from '@angular/core';
import Swal from 'sweetalert2';

@Injectable({
    providedIn: 'root'
})
export class AlertaService {

    erro(mensagem: string, titulo = 'Erro'): void {
        Swal.fire({
            icon: 'error',
            title: titulo,
            text: mensagem,
            confirmButtonText: 'OK'
        });
    }

    sucesso(mensagem: string, titulo = 'Sucesso'): void {
        Swal.fire({
            icon: 'success',
            title: titulo,
            text: mensagem,
            confirmButtonText: 'OK'
        });
    }

    erroToast(mensagem: string): void {
        Swal.fire({
            toast: true,
            position: 'top-end',
            icon: 'error',
            text: mensagem || 'Erro desconhecido, entre em contato com o administrador',
            showConfirmButton: false,
            timer: 3000,
            timerProgressBar: true
        });
    }

    sucessoToast(mensagem: string): void {
        Swal.fire({
            toast: true,
            position: 'top-end',
            icon: 'success',
            text: mensagem || 'Sucesso',
            showConfirmButton: false,
            timer: 3000,
            timerProgressBar: true
        });
    }


    confirmar(
        mensagem: string,
        titulo = 'Confirmação'
    ): Promise<boolean> {

        return Swal.fire({
            icon: 'warning',
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