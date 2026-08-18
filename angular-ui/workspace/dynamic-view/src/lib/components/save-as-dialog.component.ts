import { Component, ElementRef, EventEmitter, Output, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'save-as-dialog',
  imports: [FormsModule],
  templateUrl: './save-as-dialog.component.html',
  styleUrl: './save-as-dialog.component.scss',
})
export class SaveAsDialogComponent {
  @Output() readonly confirmed = new EventEmitter<string>();

  @ViewChild('dialog') private dialogRef!: ElementRef<HTMLDialogElement>;

  name = '';

  open(): void {
    this.name = '';
    this.dialogRef.nativeElement.showModal();
  }

  confirm(): void {
    if (!this.name.trim()) return;
    this.confirmed.emit(this.name.trim());
    this.dialogRef.nativeElement.close();
  }

  close(): void {
    this.dialogRef.nativeElement.close();
  }
}
