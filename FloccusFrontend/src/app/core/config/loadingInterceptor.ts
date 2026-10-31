import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { finalize } from 'rxjs';
import { LoadingService } from '../services/loading-service';

export const loadingInterceptor: HttpInterceptorFn = (req, next) => {

    const loadingService = inject(LoadingService);

    console.log('LOADING ON:', req.url);

    loadingService.mostrar();

    return next(req).pipe(

        finalize(() => {
            setTimeout(() => {
                loadingService.esconder();
            }, 200);
        })
    );
};