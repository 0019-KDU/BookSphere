import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { httpTokenInterceptor } from './services/interceptor/http-token.interceptor';
import { ApiModule } from './services/api.module';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([httpTokenInterceptor])),
    (ApiModule.forRoot({ rootUrl: 'http://52.221.204.241:8088/api/v1' }).providers || [])
  ],
};
