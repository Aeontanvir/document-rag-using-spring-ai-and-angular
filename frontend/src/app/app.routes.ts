import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/chat-shell/chat-shell.component').then((module) => module.ChatShellComponent)
  }
];
