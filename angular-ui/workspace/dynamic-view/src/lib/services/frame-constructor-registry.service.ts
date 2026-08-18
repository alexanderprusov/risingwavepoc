import { Injectable, Type } from '@angular/core';

export interface FrameRegistration {
  frame: Type<unknown>;
  config: Type<unknown>;
}

@Injectable({ providedIn: 'root' })
export class DynamicViewFrameConstructorRegistry {
  private readonly registry = new Map<string, FrameRegistration>();

  register(key: string, registration: FrameRegistration): void {
    this.registry.set(key, registration);
  }

  get(key: string): FrameRegistration | undefined {
    return this.registry.get(key);
  }

  keys(): string[] {
    return [...this.registry.keys()];
  }
}
