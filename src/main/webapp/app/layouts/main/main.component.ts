import { Component, OnInit, inject } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';

import { AccountService } from 'app/core/auth/account.service';
import { AppPageTitleStrategy } from 'app/app-page-title-strategy';
import FooterComponent from '../footer/footer.component';
import PageRibbonComponent from '../profiles/page-ribbon.component';
// 👇 IMPORTANTE: Importamos el componente Breadcrumb
import { BreadcrumbComponent } from 'app/shared/breadcrumb/breadcrumb.component';
import { SseNotificationService } from 'app/core/auth/sse-notification.service';

@Component({
  selector: 'jhi-main',
  templateUrl: './main.component.html',
  providers: [AppPageTitleStrategy],
  // 👇 AGREGADO: BreadcrumbComponent en la lista de imports
  imports: [RouterOutlet, FooterComponent, PageRibbonComponent, BreadcrumbComponent],
})
export default class MainComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly appPageTitleStrategy = inject(AppPageTitleStrategy);
  private readonly accountService = inject(AccountService);
  private readonly sseNotificationService = inject(SseNotificationService);

  ngOnInit(): void {
    // try to log in automatically
    this.accountService.identity().subscribe();

    this.accountService.getAuthenticationState().subscribe(account => {
      // Start listening to SSE if logged in, otherwise stop
      if (account !== null) {
        this.sseNotificationService.startListening();
      } else {
        this.sseNotificationService.stopListening();
      }
    });
  }

  isHeroRoute(): boolean {
    return this.router.url === '/' || this.router.url === '/dashboard/inicio' || this.router.url === '/dashboard';
  }
}

