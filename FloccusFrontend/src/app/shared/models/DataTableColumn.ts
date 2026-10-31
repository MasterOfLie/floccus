import { TemplateRef } from '@angular/core';
export interface DataTableColumn<T> {
    key: keyof T | string;
    label: string;
    template?: TemplateRef<any>;
    width?: string;
}