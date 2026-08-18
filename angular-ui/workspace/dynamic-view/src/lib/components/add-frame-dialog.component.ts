import {
  Component,
  ComponentRef,
  ElementRef,
  EventEmitter,
  Output,
  Type,
  ViewChild,
  ViewContainerRef,
  inject,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DynamicViewFrameConstructorRegistry } from '../services/frame-constructor-registry.service';

export interface AddFrameConfig {
  name: string;
  type: string;
  params: Record<string, unknown>;
}

@Component({
  selector: 'add-frame-dialog',
  imports: [FormsModule],
  templateUrl: './add-frame-dialog.component.html',
  styleUrl: './add-frame-dialog.component.scss',
})
export class AddFrameDialogComponent {
  @Output() readonly confirmed = new EventEmitter<AddFrameConfig>();

  @ViewChild('dialog') private dialogRef!: ElementRef<HTMLDialogElement>;
  @ViewChild('configHost', { read: ViewContainerRef }) private configHost!: ViewContainerRef;

  private readonly registry = inject(DynamicViewFrameConstructorRegistry);
  private configRef: ComponentRef<unknown> | null = null;

  name = '';
  type = '';

  readonly typeOptions: string[] = this.registry.keys();

  open(): void {
    this.name = '';
    this.type = this.typeOptions[0] ?? '';
    this.dialogRef.nativeElement.showModal();
    this.renderConfigComponent();
  }

  onTypeChange(): void {
    this.renderConfigComponent();
  }

  confirm(): void {
    if (!this.name.trim()) return;
    const params: Record<string, unknown> = (this.configRef?.instance as any)?.value ?? {};
    this.confirmed.emit({ name: this.name.trim(), type: this.type, params });
    this.teardown();
  }

  close(): void {
    this.teardown();
  }

  private teardown(): void {
    this.configHost?.clear();
    this.configRef = null;
    this.dialogRef.nativeElement.close();
  }

  private renderConfigComponent(): void {
    if (!this.configHost) return;
    this.configHost.clear();
    this.configRef = null;
    const registration = this.registry.get(this.type);
    if (!registration) return;
    this.configRef = this.configHost.createComponent(registration.config as Type<any>);
    this.configRef.changeDetectorRef.detectChanges();
  }
}
