import { Injectable } from '@angular/core';
import { SerializedDockview } from 'dockview-core';

export interface SavedLayout {
  name: string;
  layout: SerializedDockview;
}

const STORAGE_KEY = 'dynamic-view:layouts';

@Injectable({ providedIn: 'root' })
export class LayoutStorageService {
  save(layout: SerializedDockview): SavedLayout {
    return this.saveAs(`MyLayout-${Date.now()}`, layout);
  }

  saveAs(name: string, layout: SerializedDockview): SavedLayout {
    const entry: SavedLayout = { name, layout };
    const all = this.loadAll();
    all.push(entry);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(all));
    return entry;
  }

  loadAll(): SavedLayout[] {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return [];
    try {
      return JSON.parse(raw) as SavedLayout[];
    } catch {
      return [];
    }
  }

  remove(name: string): void {
    const filtered = this.loadAll().filter(e => e.name !== name);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(filtered));
  }
}
