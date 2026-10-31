import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { authInterceptor } from './core/config/authInterceptor';
import { httpErrorInterceptor } from './core/config/httpErrorInterceptor';
import { loadingInterceptor } from './core/config/loadingInterceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([
        authInterceptor,
        httpErrorInterceptor,
        loadingInterceptor
      ])
    ),
  ]
};
