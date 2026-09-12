import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AplicacaoCreate } from './aplicacao-create';

describe('AplicacaoCreate', () => {
  let component: AplicacaoCreate;
  let fixture: ComponentFixture<AplicacaoCreate>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AplicacaoCreate],
    }).compileComponents();

    fixture = TestBed.createComponent(AplicacaoCreate);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
