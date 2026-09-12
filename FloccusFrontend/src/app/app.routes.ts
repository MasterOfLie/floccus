import { Routes } from '@angular/router';
import { authGuard } from './core/config/authGuard';
import { Agentes } from './features/agentes/agentes';
import { AgenteCreate } from './features/agentes/pages/agente-create/agente-create';
import { AgenteDetail } from './features/agentes/pages/agente-detail/agente-detail';
import { Aplicacao } from './features/aplicacao/aplicacao';
import { AplicacaoCreate } from './features/aplicacao/pages/aplicacao-create/aplicacao-create/aplicacao-create';
import { AplicacaoDetail } from './features/aplicacao/pages/aplicacao-detail/aplicacao-detail';
import { Dashboard } from './features/dashboard/dashboard';
import { Users } from './features/users/users';

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
        path: 'aplicacoes/criar',
        component: AplicacaoCreate,
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
      },

      {
        path: 'git/github',
        loadComponent: () =>
          import('./features/git/github/github')
            .then(m => m.Github),
        canActivate: [authGuard]
      },


    ],
    canActivate: [authGuard]
  },
];
