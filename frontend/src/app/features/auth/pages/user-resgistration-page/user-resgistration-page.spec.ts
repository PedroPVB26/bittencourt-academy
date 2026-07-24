import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserResgistrationPage } from './user-resgistration-page';

describe('UserResgistrationPage', () => {
  let component: UserResgistrationPage;
  let fixture: ComponentFixture<UserResgistrationPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserResgistrationPage],
    }).compileComponents();

    fixture = TestBed.createComponent(UserResgistrationPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
