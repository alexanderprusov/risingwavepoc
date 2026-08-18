import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'url-opener-config',
  imports: [FormsModule],
  templateUrl: './url-opener-config.component.html',
  styleUrl: './url-opener-config.component.scss',
})
export class UrlOpenerConfigComponent {
  url = 'https://example.com';

  get value(): Record<string, unknown> {
    return { url: this.url };
  }
}
