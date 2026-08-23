import { Component, inject } from '@angular/core';
import { SidebarService } from '../../../core/services/sidebar';

@Component({
  selector: 'app-header',
  imports: [],
  templateUrl: './header.html',
  styleUrl: './header.css',
})

export class Header {

  private sidebarService = inject(SidebarService);

  collapsed = this.sidebarService.collapsed;

  toggleSidebar(): void {
    this.sidebarService.toggle();
  }

  toggleMobileSidebar(): void {
    this.sidebarService.toggleMobile();
  }

}
