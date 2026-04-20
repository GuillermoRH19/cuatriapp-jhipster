import { Component, OnInit, signal, inject } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';

import { AppPageTitleStrategy } from 'app/app-page-title-strategy';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';
import { SseNotificationService } from 'app/core/auth/sse-notification.service';
import FooterComponent from '../footer/footer.component';
import PageRibbonComponent from '../profiles/page-ribbon.component';
import { BreadcrumbComponent } from 'app/shared/breadcrumb/breadcrumb.component';

@Component({
  standalone: true,
  selector: 'jhi-main',
  templateUrl: './main.component.html',
  providers: [AppPageTitleStrategy],
  imports: [RouterOutlet, FooterComponent, PageRibbonComponent, BreadcrumbComponent],
})
export default class MainComponent implements OnInit {
  public readonly router = inject(Router);
  private readonly appPageTitleStrategy = inject(AppPageTitleStrategy);
  private readonly accountService = inject(AccountService);
  private readonly sseNotificationService = inject(SseNotificationService);

  account = signal<Account | null>(null);

  ngOnInit(): void {
    this.accountService.getAuthenticationState().subscribe(account => {
      this.account.set(account);
      // Start listening to SSE if logged in, otherwise stop
      if (account !== null) {
        this.sseNotificationService.startListening();
      } else {
        this.sseNotificationService.stopListening();
      }
    });

    this.accountService.identity().subscribe();
  }

  isHeroRoute(): boolean {
    return this.router.url === '/' || this.router.url.startsWith('/dashboard') || this.router.url === '/login';
  }
}
