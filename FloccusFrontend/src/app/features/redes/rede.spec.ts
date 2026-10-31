import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Rede } from './rede';

describe('Rede', () => {
  let component: Rede;
  let fixture: ComponentFixture<Rede>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Rede],
    }).compileComponents();

    fixture = TestBed.createComponent(Rede);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
