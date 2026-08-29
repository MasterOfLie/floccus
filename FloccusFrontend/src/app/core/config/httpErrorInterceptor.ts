import {
    HttpErrorResponse,
    HttpInterceptorFn
} from '@angular/common/http';

import { inject } from '@angular/core';

import {
    catchError,
    throwError
} from 'rxjs';

import { AlertaService } from '../services/alerta-service';


export const httpErrorInterceptor: HttpInterceptorFn = (
    req,
    next
) => {

    const alertService = inject(AlertaService);

    return next(req).pipe(

        catchError((error: HttpErrorResponse) => {

            console.error(
                'Erro HTTP:',
                error
            );


            // Mensagem enviada pelo backend
            const mensagem =
                error.error?.message;


            switch (error.status) {

                case 400:

                    alertService.erro(
                        mensagem ?? 'Os dados enviados são inválidos.',
                        'Requisição inválida'
                    );

                    break;


                case 401:

                    alertService.erro(
                        mensagem ?? 'Sua sessão expirou.',
                        'Não autenticado'
                    );

                    break;


                case 403:

                    alertService.erro(
                        mensagem ?? 'Você não possui permissão para realizar esta ação.',
                        'Acesso negado'
                    );

                    break;


                case 404:

                    alertService.erro(
                        mensagem ?? 'O recurso solicitado não foi encontrado.',
                        'Não encontrado'
                    );

                    break;


                case 409:

                    alertService.erro(
                        mensagem ?? 'A operação entrou em conflito com os dados existentes.',
                        'Conflito'
                    );

                    break;


                case 500:

                    alertService.erro(
                        mensagem ?? 'Ocorreu um erro interno no servidor.',
                        'Erro interno'
                    );

                    break;


                default:

                    alertService.erro(
                        mensagem ?? 'Ocorreu um erro inesperado.',
                        'Erro'
                    );

            }


            // Continua propagando o erro
            return throwError(
                () => error
            );

        })

    );

};