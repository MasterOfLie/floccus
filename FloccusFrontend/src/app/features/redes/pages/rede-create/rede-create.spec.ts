import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RedeCreate } from './rede-create';

describe('RedeCreate', () => {
  let component: RedeCreate;
  let fixture: ComponentFixture<RedeCreate>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RedeCreate],
    }).compileComponents();

    fixture = TestBed.createComponent(RedeCreate);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
