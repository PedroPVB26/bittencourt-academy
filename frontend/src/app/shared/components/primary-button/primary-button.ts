import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-primary-button',
  imports: [],
  templateUrl: './primary-button.html',
  styleUrl: './primary-button.scss',
})
export class PrimaryButton {
  @Input() label = '';
  @Input() buttonStyle: ButtonStyle = 'primary';
  @Input() type: 'button' | 'submit' | 'reset' = 'button';
  @Input() disabled = false;
}

type ButtonStyle = "primary" | "secondary"
