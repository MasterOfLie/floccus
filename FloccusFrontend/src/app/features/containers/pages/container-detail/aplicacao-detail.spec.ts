import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AplicacaoDetail } from './aplicacao-detail';

describe('ContainerDetail', () => {
  let component: AplicacaoDetail;
  let fixture: ComponentFixture<AplicacaoDetail>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AplicacaoDetail],
    }).compileComponents();

    fixture = TestBed.createComponent(AplicacaoDetail);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
