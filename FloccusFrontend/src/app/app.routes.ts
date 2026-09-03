import { Routes } from '@angular/router';
import { Dashboard } from './features/dashboard/dashboard';
import { Users } from './features/users/users';
import { Aplicacao } from './features/containers/aplicacao';
import { AplicacaoDetail } from './features/containers/pages/container-detail/aplicacao-detail';
import { Agentes } from './features/agentes/agentes';
import { AgenteCreate } from './features/agentes/pages/agente-create/agente-create';
import { AgenteDetail } from './features/agentes/pages/agente-detail/agente-detail';

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
        path: 'aplicacoes',
        component: Aplicacao
      },
      {
        path: 'aplicacoes/:id',
        component: AplicacaoDetail
      },
      {
        path: 'agentes',
        component: Agentes
      },
      {
        path: 'agentes/criar',
        component: AgenteCreate
      },
      {
        path: 'agentes/:id',
        component: AgenteDetail
      }
    ]
  },
];
