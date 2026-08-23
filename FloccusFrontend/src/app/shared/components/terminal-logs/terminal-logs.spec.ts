import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TerminalLogs } from './terminal-logs';

describe('TerminalLogs', () => {
  let component: TerminalLogs;
  let fixture: ComponentFixture<TerminalLogs>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TerminalLogs],
    }).compileComponents();

    fixture = TestBed.createComponent(TerminalLogs);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
