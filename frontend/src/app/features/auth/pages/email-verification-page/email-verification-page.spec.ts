import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmailVerificationPage } from './email-verification-page';

describe('EmailVerificationPage', () => {
  let component: EmailVerificationPage;
  let fixture: ComponentFixture<EmailVerificationPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmailVerificationPage],
    }).compileComponents();

    fixture = TestBed.createComponent(EmailVerificationPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
