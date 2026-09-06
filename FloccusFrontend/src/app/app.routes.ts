import { Routes } from '@angular/router';
import { Dashboard } from './features/dashboard/dashboard';
import { Users } from './features/users/users';
import { Aplicacao } from './features/containers/aplicacao';
import { AplicacaoDetail } from './features/containers/pages/container-detail/aplicacao-detail';
import { Agentes } from './features/agentes/agentes';
import { AgenteCreate } from './features/agentes/pages/agente-create/agente-create';
import { AgenteDetail } from './features/agentes/pages/agente-detail/agente-detail';
import { authGuard } from './core/config/authGuard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/auth/auth')
        .then(m => m.Auth)
  },
  {
    path: '',
    loadComponent: () => import('./layouts/dashboard/dashboard').then(m => m.Dashboard),

    children: [
      {
        path: '',
        component: Dashboard,
        canActivate: [authGuard]
      },
      {
        path: 'users',
        component: Users,
        canActivate: [authGuard]
      },
      {
        path: 'aplicacoes',
        component: Aplicacao,
        canActivate: [authGuard]
      },
      {
        path: 'aplicacoes/:id',
        component: AplicacaoDetail,
        canActivate: [authGuard]
      },
      {
        path: 'agentes',
        component: Agentes,
        canActivate: [authGuard]
      },
      {
        path: 'agentes/criar',
        component: AgenteCreate,
        canActivate: [authGuard]
      },
      {
        path: 'agentes/:id',
        component: AgenteDetail,
        canActivate: [authGuard]
      }

    ],
    canActivate: [authGuard]
  },
];
