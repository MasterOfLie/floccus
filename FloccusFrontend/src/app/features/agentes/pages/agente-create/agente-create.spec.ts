import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AgenteCreate } from './agente-create';

describe('AgenteCreate', () => {
  let component: AgenteCreate;
  let fixture: ComponentFixture<AgenteCreate>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AgenteCreate],
    }).compileComponents();

    fixture = TestBed.createComponent(AgenteCreate);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
