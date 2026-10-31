import { TestBed } from '@angular/core/testing';

import { VariaveisService } from './variaveis-service';

describe('VariaveisService', () => {
  let service: VariaveisService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(VariaveisService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
