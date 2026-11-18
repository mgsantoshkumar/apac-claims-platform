import { fakeAsync, TestBed, tick } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { NotificationsApiService } from './notifications-api.service';
import { environment } from '../../environments/environment';
import { TimelineEntry } from '../shared/models/claim.models';

describe('NotificationsApiService', () => {
  let service: NotificationsApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), NotificationsApiService]
    });
    service = TestBed.inject(NotificationsApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads timeline entries', () => {
    const entry: TimelineEntry = {
      id: 't1',
      claimId: 'c1',
      claimNumber: 'CLM-SG-1',
      eventType: 'SUBMITTED',
      status: 'SUBMITTED',
      occurredAt: '2026-11-01T00:00:00Z'
    };

    let result: TimelineEntry[] | undefined;
    service.timeline('c1').subscribe((items) => (result = items));

    const req = http.expectOne(`${environment.notificationsApiUrl}/api/claims/c1/timeline`);
    expect(req.request.method).toBe('GET');
    req.flush([entry]);
    expect(result).toEqual([entry]);
  });

  it('pollTimeline retries until events appear', fakeAsync(() => {
    const entry: TimelineEntry = {
      id: 't1',
      claimId: 'c1',
      claimNumber: 'CLM-SG-1',
      eventType: 'SUBMITTED',
      status: 'SUBMITTED',
      occurredAt: '2026-11-01T00:00:00Z'
    };

    const values: TimelineEntry[][] = [];
    service.pollTimeline('c1', 3, 10).subscribe((items) => values.push(items));

    tick(0);
    http.expectOne(`${environment.notificationsApiUrl}/api/claims/c1/timeline`).flush([]);

    tick(10);
    http.expectOne(`${environment.notificationsApiUrl}/api/claims/c1/timeline`).flush([entry]);

    expect(values.some((items) => items.length === 1)).toBeTrue();
    expect(values[values.length - 1]).toEqual([entry]);
  }));
});
