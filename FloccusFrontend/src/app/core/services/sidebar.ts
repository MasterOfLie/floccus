import { Injectable, signal } from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class SidebarService {
    private collapsedSignal = signal(false);
    private mobileOpenSignal = signal(false);

    collapsed = this.collapsedSignal.asReadonly();
    mobileOpen = this.mobileOpenSignal.asReadonly();

    toggle(): void {
        this.collapsedSignal.update(value => !value);
    }

    setCollapsed(value: boolean): void {
        this.collapsedSignal.set(value);
    }

    toggleMobile(): void {
        this.mobileOpenSignal.update(value => !value);
    }

    closeMobile(): void {
        this.mobileOpenSignal.set(false);
    }

}
