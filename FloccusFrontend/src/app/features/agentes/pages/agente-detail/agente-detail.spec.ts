import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AgenteDetail } from './agente-detail';

describe('AgenteDetail', () => {
  let component: AgenteDetail;
  let fixture: ComponentFixture<AgenteDetail>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AgenteDetail],
    }).compileComponents();

    fixture = TestBed.createComponent(AgenteDetail);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
