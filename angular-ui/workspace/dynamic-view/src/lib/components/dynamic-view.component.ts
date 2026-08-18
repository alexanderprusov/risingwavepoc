import {
  AfterViewInit,
  ApplicationRef,
  Component,
  ComponentRef,
  createComponent,
  createEnvironmentInjector,
  ElementRef,
  EnvironmentInjector,
  inject,
  OnDestroy,
  Type,
  ViewChild,
} from '@angular/core';
import {
  createDockview,
  DockviewApi,
  GroupPanelPartInitParameters,
  IContentRenderer,
  themeAbyss,
} from 'dockview-core';
import { LayoutStorageService, SavedLayout } from '../services/layout-storage.service';
import { DynamicViewFrameConstructorRegistry } from '../services/frame-constructor-registry.service';
import { WorkspaceContext } from '../services/workspace-context.service';
import { AddFrameConfig, AddFrameDialogComponent } from './add-frame-dialog.component';
import { LoadLayoutDialogComponent } from './load-layout-dialog.component';
import { SaveAsDialogComponent } from './save-as-dialog.component';
import { generateUUID } from '../utils/uuid';

@Component({
  selector: 'dynamic-view',
  imports: [AddFrameDialogComponent, LoadLayoutDialogComponent, SaveAsDialogComponent],
  providers: [WorkspaceContext],
  templateUrl: './dynamic-view.component.html',
  styleUrl: './dynamic-view.component.scss',
})
export class DynamicViewComponent implements AfterViewInit, OnDestroy {

  @ViewChild('container') containerRef!: ElementRef<HTMLElement>;
  @ViewChild(AddFrameDialogComponent) private addFrameDialog!: AddFrameDialogComponent;
  @ViewChild(LoadLayoutDialogComponent) private loadDialog!: LoadLayoutDialogComponent;
  @ViewChild(SaveAsDialogComponent) private saveAsDialog!: SaveAsDialogComponent;

  readonly context = inject(WorkspaceContext);
  loadedLayouts: SavedLayout[] = [];

  private dockview: DockviewApi | null = null;
  private frameInjector!: EnvironmentInjector;
  private readonly storage = inject(LayoutStorageService);
  private readonly registry = inject(DynamicViewFrameConstructorRegistry);

  constructor(
    private readonly appRef: ApplicationRef,
    private readonly injector: EnvironmentInjector,
  ) {}

  saveLayout(): void {
    if (!this.dockview) return;
    this.storage.save(this.dockview.toJSON());
  }

  openSaveAsDialog(): void {
    this.saveAsDialog.open();
  }

  saveLayoutAs(name: string): void {
    if (!this.dockview) return;
    this.storage.saveAs(name, this.dockview.toJSON());
    this.context.currentLayoutName = name;
  }

  clean(): void {
    this.dockview?.closeAllGroups();
    this.context.reset();
  }

  openLoadDialog(): void {
    this.loadedLayouts = this.storage.loadAll();
    this.loadDialog.open();
  }

  applyLayout(saved: SavedLayout): void {
    this.context.reset();
    this.dockview?.fromJSON(saved.layout);
    this.context.currentLayoutName = saved.name;
  }

  openAddFrameDialog(): void {
    this.addFrameDialog.open();
  }

  addFrame(config: AddFrameConfig): void {
    if (!this.dockview) return;
    const id = generateUUID();
    this.dockview.addPanel({
      id,
      title: config.name,
      component: config.type,
      params: { ...config.params, id, name: config.name },
    });
  }

  ngAfterViewInit(): void {
    const { appRef, injector, registry, context } = this;

    const frameInjector = createEnvironmentInjector(
      [{ provide: WorkspaceContext, useValue: context }],
      injector,
    );
    this.frameInjector = frameInjector;

    this.dockview = createDockview(this.containerRef.nativeElement, {
      theme: themeAbyss,
      createComponent(options) {
        const registration = registry.get(options.name);
        if (!registration) throw new Error(`Unknown frame type: ${options.name}`);
        return new DynamicFrameRenderer(registration.frame, appRef, frameInjector);
      },
    });
  }

  ngOnDestroy(): void {
    this.dockview?.dispose();
    this.frameInjector?.destroy();
  }
}

class DynamicFrameRenderer implements IContentRenderer {
  private readonly _element = document.createElement('div');
  private componentRef: ComponentRef<unknown> | null = null;

  get element(): HTMLElement {
    return this._element;
  }

  constructor(
    private readonly componentType: Type<unknown>,
    private readonly appRef: ApplicationRef,
    private readonly injector: EnvironmentInjector,
  ) {}

  init(params: GroupPanelPartInitParameters): void {
    this.componentRef = createComponent(this.componentType as Type<any>, {
      environmentInjector: this.injector,
      hostElement: this._element,
    });
    if (params.params) {
      this.componentRef.setInput('config', params.params);
    }
    this.appRef.attachView(this.componentRef.hostView);
    this.componentRef.changeDetectorRef.detectChanges();
  }

  dispose(): void {
    this.componentRef?.destroy();
    this.componentRef = null;
  }
}
