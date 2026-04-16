import { Component } from '@angular/core';
import SharedModule from 'app/shared/shared.module';

@Component({
  selector: 'jhi-footer',
  standalone: true,
  imports: [SharedModule],
  templateUrl: './footer.component.html',
})
export default class FooterComponent {
  currentYear = new Date().getFullYear();
}
