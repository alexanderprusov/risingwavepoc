import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { WorkspaceContext } from 'dynamic-view';

@Component({
  selector: 'test-grid-config',
  imports: [FormsModule],
  templateUrl: './test-grid-config.component.html',
  styleUrl: './test-grid-config.component.scss',
})
export class TestGridConfigComponent {
  title = 'Test Grid';
  rows = 5;
  dependsOn = '';

  private readonly ctx = inject(WorkspaceContext, { optional: true });

  get availableFrames(): string[] {
    return this.ctx ? [...this.ctx.frames.keys()] : [];
  }

  get value(): Record<string, unknown> {
    return {
      title: this.title,
      rows: this.rows,
      ...(this.dependsOn ? { dependsOn: this.dependsOn } : {}),
    };
  }
}
