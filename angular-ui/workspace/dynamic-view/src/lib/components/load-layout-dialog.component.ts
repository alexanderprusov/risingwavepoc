import { Component, ElementRef, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { SavedLayout } from '../services/layout-storage.service';

@Component({
  selector: 'load-layout-dialog',
  templateUrl: './load-layout-dialog.component.html',
  styleUrl: './load-layout-dialog.component.scss',
})
export class LoadLayoutDialogComponent {
  @Input() layouts: SavedLayout[] = [];
  @Output() readonly layoutSelected = new EventEmitter<SavedLayout>();

  @ViewChild('dialog') private dialogRef!: ElementRef<HTMLDialogElement>;

  open(): void {
    this.dialogRef.nativeElement.showModal();
  }

  select(layout: SavedLayout): void {
    this.layoutSelected.emit(layout);
    this.dialogRef.nativeElement.close();
  }

  close(): void {
    this.dialogRef.nativeElement.close();
  }
}
