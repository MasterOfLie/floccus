import { Routes } from '@angular/router';
import { Dashboard } from './features/dashboard/dashboard';
import { Users } from './features/users/users';
import { Containers } from './features/containers/containers';
import { ContainerDetail } from './features/containers/pages/container-detail/container-detail';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./layouts/dashboard/dashboard').then(m => m.Dashboard),
    children: [
      {
        path: 'dashboard',
        component: Dashboard
      },
      {
        path: 'users',
        component: Users
      },
      {
        path: 'containers',
        component: Containers
      },
      {
        path: 'containers/:id',
        component: ContainerDetail
      }
    ]
  },
];
