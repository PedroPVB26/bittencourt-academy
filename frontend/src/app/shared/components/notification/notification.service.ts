import { inject, Service } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NotificationAction, NotificationData } from './notification.model';
import { Notification } from './notification';

@Service()
export class NotificationService {
    private readonly snackBar = inject(MatSnackBar);

    success(title: string, message: string, actions?: NotificationAction[]): void {
        this.show({ type: 'success', title, message, actions });
    }

    error(title: string, message: string, actions?: NotificationAction[]): void {
        this.show({ type: 'error', title, message, actions });
    }

    warning(title: string, message: string, actions?: NotificationAction[]): void {
        this.show({ type: 'warning', title, message, actions });
    }

    info(title: string, message: string, actions?: NotificationAction[]): void {
        this.show({ type: 'info', title, message, actions });
    }

    private show(data: NotificationData): void {
        this.snackBar.openFromComponent(Notification, {
            data,
            duration: data.actions?.length ? undefined : 5000,
            horizontalPosition: 'center',
            verticalPosition: 'top',
            panelClass: ['custom-snackbar-overlay', `notification-${data.type}`]
        });
    }
}
