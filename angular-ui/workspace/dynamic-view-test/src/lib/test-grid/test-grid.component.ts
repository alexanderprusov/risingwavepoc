import { AsyncPipe } from '@angular/common';
import { Component, inject, Input, OnInit } from '@angular/core';
import { WorkspaceContext } from 'dynamic-view';

export interface TestGridConfig {
  name: string;
  title: string;
  rows: number;
  dependsOn?: string;
}

@Component({
  selector: 'test-grid',
  imports: [AsyncPipe],
  templateUrl: './test-grid.component.html',
  styleUrl: './test-grid.component.scss',
})
export class TestGridComponent implements OnInit {
  @Input({ required: true }) config!: TestGridConfig;

  readyPromise!: Promise<boolean>;

  private readonly ctx = inject(WorkspaceContext, { optional: true });

  get mockRows(): number[] {
    return Array.from({ length: this.config.rows }, (_, i) => i + 1);
  }

  ngOnInit(): void {
    let resolve!: (v: boolean) => void;
    this.readyPromise = new Promise<boolean>(r => (resolve = r));

    this.ctx?.registerFrame(this.config.name, this.readyPromise);

    const initialize = () => {
      setTimeout(() => resolve(true), 3000);
    };

    if (this.config.dependsOn && this.ctx) {
      this.ctx.awaitFrame(this.config.dependsOn).then(initialize);
    } else {
      initialize();
    }
  }
}
