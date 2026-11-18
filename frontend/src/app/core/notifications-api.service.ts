import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, timer } from 'rxjs';
import { concatMap, map, take, takeWhile } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { NotificationItem, TimelineEntry } from '../shared/models/claim.models';

@Injectable({ providedIn: 'root' })
export class NotificationsApiService {
  private readonly base = environment.notificationsApiUrl;

  constructor(private readonly http: HttpClient) {}

  timeline(claimId: string) {
    return this.http.get<TimelineEntry[]>(`${this.base}/api/claims/${claimId}/timeline`);
  }

  /**
   * Polls the projection read-model briefly so the UI can catch up after Kafka lag.
   */
  pollTimeline(claimId: string, attempts = 6, delayMs = 400): Observable<TimelineEntry[]> {
    return timer(0, delayMs).pipe(
      take(attempts),
      concatMap((attempt) =>
        this.timeline(claimId).pipe(map((items) => ({ items, attempt })))
      ),
      takeWhile(({ items, attempt }) => items.length === 0 && attempt < attempts - 1, true),
      map(({ items }) => items)
    );
  }

  mine() {
    return this.http.get<NotificationItem[]>(`${this.base}/api/notifications/me`);
  }
}
