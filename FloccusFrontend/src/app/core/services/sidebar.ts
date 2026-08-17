import { Injectable, signal } from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class SidebarService {
    private collapsedSignal = signal(false);

    collapsed = this.collapsedSignal.asReadonly();

    toggle(): void {
        this.collapsedSignal.update(value => !value);
    }

    setCollapsed(value: boolean): void {
        this.collapsedSignal.set(value);
    }

}
