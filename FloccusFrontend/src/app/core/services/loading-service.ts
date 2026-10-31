import { Injectable, signal } from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class LoadingService {
    private requests = 0;

    readonly loading = signal(false);

    mostrar(): void {
        this.requests++;
        this.loading.set(true);
    }

    esconder(): void {
        this.requests--;

        if (this.requests <= 0) {
            this.requests = 0;
            this.loading.set(false);
        }
    }
}
