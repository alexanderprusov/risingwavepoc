import { Component, inject } from '@angular/core';
import { DynamicViewComponent, DynamicViewFrameConstructorRegistry } from 'dynamic-view';
import { UrlOpenerComponent, UrlOpenerConfigComponent, TestGridComponent, TestGridConfigComponent } from 'dynamic-view-test';
import { RwGridComponent } from 'toolkit';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-root',
  imports: [DynamicViewComponent, RwGridComponent, ButtonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class App {
  totalRows = 0;

  constructor() {
    const registry = inject(DynamicViewFrameConstructorRegistry);
    registry.register('urlOpener', { frame: UrlOpenerComponent, config: UrlOpenerConfigComponent });
    registry.register('testGrid', { frame: TestGridComponent, config: TestGridConfigComponent });
  }
}
