import { Component, inject } from '@angular/core';
import { NotificationData } from './notification.model';
import { MAT_SNACK_BAR_DATA, MatSnackBarRef } from '@angular/material/snack-bar';

@Component({
  selector: 'app-notification',
  imports: [],
  templateUrl: './notification.html',
  styleUrl: './notification.scss',
})
export class Notification {
  readonly data = inject<NotificationData>(MAT_SNACK_BAR_DATA);
  private readonly snackBarRef = inject(MatSnackBarRef<Notification>);

  executeAction(callback: () => void): void {
    callback();
    this.snackBarRef.dismiss();
  }

  close(): void {
    this.snackBarRef.dismiss();
  }
}
