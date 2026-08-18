import { Injectable } from '@angular/core';

@Injectable()
export class WorkspaceContext {
  currentLayoutName: string | null = null;
  readonly frames = new Map<string, Promise<any>>();

  // Frames that called awaitFrame() before the target registered.
  private readonly deferred = new Map<string, Array<() => void>>();

  registerFrame(name: string, readyPromise: Promise<any>): void {
    this.frames.set(name, readyPromise);
    const waiters = this.deferred.get(name);
    if (waiters) {
      readyPromise.then(() => waiters.forEach(r => r()));
      this.deferred.delete(name);
    }
  }

  // Resolves when the named frame is ready, whether or not it has registered yet.
  awaitFrame(name: string): Promise<any> {
    if (this.frames.has(name)) return this.frames.get(name)!;
    return new Promise<void>(resolve => {
      const waiters = this.deferred.get(name) ?? [];
      waiters.push(resolve);
      this.deferred.set(name, waiters);
    });
  }

  reset(): void {
    this.frames.clear();
    this.deferred.clear();
    this.currentLayoutName = null;
  }
}
