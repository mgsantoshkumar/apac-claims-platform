import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ClaimsApiService } from '../../core/claims-api.service';
import { NotificationsApiService } from '../../core/notifications-api.service';
import { ClaimDetailComponent } from './claim-detail.component';

describe('ClaimDetailComponent', () => {
  let fixture: ComponentFixture<ClaimDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClaimDetailComponent, NoopAnimationsModule],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => 'claim-1' } } }
        },
        {
          provide: ClaimsApiService,
          useValue: {
            get: () =>
              of({
                id: 'claim-1',
                claimNumber: 'CLM-SG-2026-2001',
                market: 'SG',
                lob: 'MOTOR',
                status: 'INFO_REQUESTED',
                claimantUserId: 'u-1',
                incidentSummary: 'Rear-end collision',
                incidentDescription: 'Hit at light',
                incidentDate: '2026-11-01',
                location: 'Orchard',
                estimatedLoss: 2500,
                infoRequestMessage: 'Please send photos',
                createdAt: '2026-11-01T00:00:00Z',
                updatedAt: '2026-11-01T00:00:00Z'
              }),
            provideInfo: () =>
              of({
                id: 'claim-1',
                claimNumber: 'CLM-SG-2026-2001',
                market: 'SG',
                lob: 'MOTOR',
                status: 'UNDER_REVIEW',
                claimantUserId: 'u-1',
                incidentSummary: 'Rear-end collision',
                incidentDescription: 'Hit at light',
                incidentDate: '2026-11-01',
                location: 'Orchard',
                estimatedLoss: 2500,
                createdAt: '2026-11-01T00:00:00Z',
                updatedAt: '2026-11-01T00:00:00Z'
              })
          }
        },
        {
          provide: NotificationsApiService,
          useValue: {
            pollTimeline: () =>
              of([
                {
                  id: 't1',
                  claimId: 'claim-1',
                  claimNumber: 'CLM-SG-2026-2001',
                  eventType: 'INFO_REQUESTED',
                  status: 'INFO_REQUESTED',
                  message: 'Please send photos',
                  occurredAt: '2026-11-01T01:00:00Z'
                }
              ])
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ClaimDetailComponent);
    fixture.detectChanges();
  });

  it('renders back link to My claims', () => {
    const back = fixture.nativeElement.querySelector('a.back') as HTMLAnchorElement;
    expect(back).toBeTruthy();
    expect(back.getAttribute('routerLink') || back.getAttribute('ng-reflect-router-link')).toContain('/claimant');
    expect(back.textContent).toContain('Back to My claims');
  });

  it('shows claim summary and timeline', () => {
    expect(fixture.nativeElement.textContent).toContain('CLM-SG-2026-2001');
    expect(fixture.nativeElement.textContent).toContain('INFO_REQUESTED');
    expect(fixture.nativeElement.textContent).toContain('Please send photos');
  });
});
