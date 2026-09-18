export type NotificationType = 'success' | 'error' | 'warning' | 'info';

export interface NotificationAction {
  label: string;
  callback: () => void;
}

export interface NotificationData {
  type: NotificationType;
  title: string;
  message: string;
  actions?: NotificationAction[];
}