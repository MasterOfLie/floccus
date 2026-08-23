import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, NavigationEnd, Router } from '@angular/router';
import { SidebarService } from '../../../core/services/sidebar';
import { filter } from 'rxjs';
import { MenuItem } from '../../../features/containers/models/MenuItem';

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class Sidebar {

  private sidebarService = inject(SidebarService);
  private router = inject(Router);

  collapsed = this.sidebarService.collapsed;
  mobileOpen = this.sidebarService.mobileOpen;

  menuItems: MenuItem[] = [
    {
      label: 'Dashboard',
      icon: 'bi-house',
      route: '/dashboard',
      exact: true,
      permission: 'NOT_IMPLEMENTED'
    },
    {
      label: 'Containers',
      icon: 'bi-hdd-stack',
      route: '/containers',
      exact: true,
      permission: 'NOT_IMPLEMENTED'
    },
    {
      label: 'Usuários',
      icon: 'bi-people',
      route: '/users',
      permission: 'NOT_IMPLEMENTED'
    }
  ];


  constructor() {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.sidebarService.closeMobile();
    });
  }

  closeMobile(): void {
    this.sidebarService.closeMobile();
  }

}
