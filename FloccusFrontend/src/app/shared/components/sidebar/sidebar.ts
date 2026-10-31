import { Component, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { filter } from 'rxjs';
import { SidebarService } from '../../../core/services/sidebar';
import { MenuItem } from '../../../features/aplicacao/models/MenuItem';

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class Sidebar {

  perfilModalAberta = signal(false);
  private sidebarService = inject(SidebarService);
  private router = inject(Router);
  tooltipVisible = signal(false);
  tooltipText = signal('');
  tooltipY = signal(0);

  collapsed = this.sidebarService.collapsed;
  mobileOpen = this.sidebarService.mobileOpen;

  menuItems: MenuItem[] = [
    // {
    //   label: 'Dashboard',
    //   icon: 'bi-house',
    //   route: '/',
    //   exact: true,
    //   permission: 'NOT_IMPLEMENTED'
    // },
    {
      label: 'Aplicações',
      icon: 'bi-hdd-stack',
      route: '/aplicacoes',
      exact: true,
      permission: 'NOT_IMPLEMENTED'
    },
    // {
    //   label: 'Usuários',
    //   icon: 'bi-people',
    //   route: '/users',
    //   permission: 'NOT_IMPLEMENTED'
    // },
    {
      label: 'Agentes',
      icon: 'bi-motherboard',
      route: '/agentes',
      permission: 'NOT_IMPLEMENTED'
    }, {
      label: 'Git',
      icon: 'bi-git',
      route: '/git/github',
      permission: 'NOT_IMPLEMENTED'
    },
    {
      label: 'Templates',
      icon: 'bi-file-earmark-code',
      route: '/templates',
      permission: 'NOT_IMPLEMENTED'
    },

    {
      label: 'Redes',
      icon: 'bi-ethernet',
      route: '/redes',
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


  showTooltip(event: MouseEvent, label: string) {

    if (!this.collapsed()) {
      return;
    }

    const element = event.currentTarget as HTMLElement;
    const rect = element.getBoundingClientRect();

    this.tooltipText.set(label);
    this.tooltipY.set(rect.top + rect.height / 2);
    this.tooltipVisible.set(true);
  }

  hideTooltip() {
    this.tooltipVisible.set(false);
  }

}
