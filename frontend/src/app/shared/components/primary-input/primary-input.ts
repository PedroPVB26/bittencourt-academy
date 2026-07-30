import { AfterViewInit, ChangeDetectorRef, Component, forwardRef, inject, Injector, Input, OnDestroy, OnInit } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, NgControl, ReactiveFormsModule } from '@angular/forms';

@Component({
  standalone: true,
  selector: 'app-primary-input',
  imports: [ReactiveFormsModule],
  templateUrl: './primary-input.html',
  styleUrl: './primary-input.scss',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => PrimaryInput),
      multi: true
    }
  ]
})
export class PrimaryInput implements ControlValueAccessor, AfterViewInit {
  @Input() label = '';
  @Input() placeholder = '';
  @Input() type: InputType = 'text';
  @Input() hint = '';
  @Input() inputName = '';
  value = '';
  disabled = false;
  showPassword = false;

  private injector = inject(Injector);
  private cdr = inject(ChangeDetectorRef);


  get control() {
    return this.injector.get(NgControl, null)?.control;
  }

  get invalid(): boolean {
    return !!(this.control?.invalid && (this.control.dirty || this.control.touched));
  }

  get inputType(): string {
    if (this.type === 'password') {
      return this.showPassword ? 'text' : 'password';
    }

    return this.type;
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  get errorMessage(): string {
    const control = this.control;

    if (!control || !this.invalid) {
      return '';
    }

    if (control.hasError('required')) {
      return 'This field is required';
    }

    if (control.hasError('minlength')) {
      return 'Minimum length not reached';
    }

    if (control.hasError('maxlength')) {
      return 'Max length reached';
    }

    if (control.hasError('pattern')) {
      return 'Must contain uppercase, lowercase and number'
    }

    if (control.hasError('emailAlreadyExists')) {
      return 'An account with this email already exists';
    }

    if (control.hasError('passwordMismatch')) {
      return "Passwords don't match"
    }

    if (control.hasError('email')) {
      return 'Please enter a valid email address';
    }

    return '';
  }

  private onChange = (value: string) => { };
  private onTouched = () => { };

  ngAfterViewInit(): void {
    this.control?.statusChanges.subscribe(() => {
      this.cdr.markForCheck();
    });
  }

  // Angular chama quando o FormControl altera o valor
  writeValue(value: string): void {
    this.value = value ?? '';
  }

  // Angular registrar uma função apra receber mudanças
  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  // Angular registra quando o campo for tocado
  registerOnTouched(fn: () => void): void {
    this.onTouched = fn
  }

  // Desabilitar pelo FormControl
  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  onInput(event: Event) {
    const value = (event.target as HTMLInputElement).value;
    this.value = value;
    this.onChange(value);
  }

  onBlur() {
    if (this.type === 'password' && this.showPassword) {
      this.showPassword = false;
    }
    this.onTouched();
  }

}

type InputType = "text" | "email" | "password"
