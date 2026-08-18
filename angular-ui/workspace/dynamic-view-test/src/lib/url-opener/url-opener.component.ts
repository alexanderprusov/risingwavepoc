import { Component, Input } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

export interface UrlOpenerConfig {
  url: string;
}

@Component({
  selector: 'url-opener',
  templateUrl: './url-opener.component.html',
  styleUrl: './url-opener.component.scss',
})
export class UrlOpenerComponent {
  @Input({ required: true }) set config(value: UrlOpenerConfig) {
    this.safeUrl = this.sanitizer.bypassSecurityTrustResourceUrl(value.url);
  }

  safeUrl: SafeResourceUrl = '';

  constructor(private readonly sanitizer: DomSanitizer) {}
}
