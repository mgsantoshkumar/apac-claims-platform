import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ClaimsApiService } from '../../core/claims-api.service';
import { Claim } from '../../shared/models/claim.models';
import { StaffInboxComponent } from './staff-inbox.component';

describe('StaffInboxComponent', () => {
  let component: StaffInboxComponent;

  const claims: Claim[] = [
    {
      id: 'id-1',
      claimNumber: 'CLM-SG-2026-2001',
      market: 'SG',
      lob: 'MOTOR',
      status: 'SUBMITTED',
      claimantUserId: 'u-1',
      incidentSummary: 'Rear-end collision',
      incidentDescription: 'desc',
      incidentDate: '2026-11-01',
      location: 'Orchard',
      estimatedLoss: 2500,
      createdAt: '2026-11-01T00:00:00Z',
      updatedAt: '2026-11-01T00:00:00Z'
    },
    {
      id: 'id-2',
      claimNumber: 'CLM-AU-2026-2003',
      market: 'AU',
      lob: 'MOTOR',
      status: 'ASSIGNED',
      claimantUserId: 'u-2',
      adjusterUserId: 'u-adj',
      incidentSummary: 'Hail damage',
      incidentDescription: 'desc',
      incidentDate: '2026-11-03',
      location: 'Sydney',
      estimatedLoss: 8000,
      createdAt: '2026-11-03T00:00:00Z',
      updatedAt: '2026-11-03T00:00:00Z'
    }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StaffInboxComponent, NoopAnimationsModule],
      providers: [
        provideRouter([]),
        { provide: ClaimsApiService, useValue: { list: () => of(claims) } }
      ]
    }).compileComponents();

    const fixture: ComponentFixture<StaffInboxComponent> = TestBed.createComponent(StaffInboxComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('filters inbox by status and summary', () => {
    expect(component.filteredClaims().length).toBe(2);
    component.statusFilter.set('SUBMITTED');
    component.summaryQuery.set('collision');
    expect(component.filteredClaims().map((c) => c.claimNumber)).toEqual(['CLM-SG-2026-2001']);
  });
});
