import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ClaimsApiService } from '../../core/claims-api.service';
import { Claim } from '../../shared/models/claim.models';
import { MyClaimsComponent } from './my-claims.component';

describe('MyClaimsComponent', () => {
  let fixture: ComponentFixture<MyClaimsComponent>;
  let component: MyClaimsComponent;

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
      claimNumber: 'CLM-HK-2026-2002',
      market: 'HK',
      lob: 'PROPERTY',
      status: 'SETTLED',
      claimantUserId: 'u-1',
      incidentSummary: 'Water damage',
      incidentDescription: 'desc',
      incidentDate: '2026-11-02',
      location: 'Kowloon',
      estimatedLoss: 5000,
      createdAt: '2026-11-02T00:00:00Z',
      updatedAt: '2026-11-02T00:00:00Z'
    }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MyClaimsComponent, NoopAnimationsModule],
      providers: [
        provideRouter([]),
        {
          provide: ClaimsApiService,
          useValue: { list: () => of(claims) }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(MyClaimsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('loads claims and shows all by default', () => {
    expect(component.claims().length).toBe(2);
    expect(component.filteredClaims().length).toBe(2);
  });

  it('filters by status and market', () => {
    component.statusFilter.set('SUBMITTED');
    component.marketFilter.set('SG');
    expect(component.filteredClaims().map((c) => c.claimNumber)).toEqual(['CLM-SG-2026-2001']);
  });

  it('filters by claim number and clears filters', () => {
    component.claimQuery.set('HK-2026');
    expect(component.filteredClaims()).toHaveSize(1);
    expect(component.hasActiveFilters()).toBeTrue();
    component.clearFilters();
    expect(component.filteredClaims().length).toBe(2);
    expect(component.hasActiveFilters()).toBeFalse();
  });
});
