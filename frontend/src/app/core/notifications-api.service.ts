import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { NotificationItem, TimelineEntry } from '../shared/models/claim.models';

@Injectable({ providedIn: 'root' })
export class NotificationsApiService {
  private readonly base = environment.notificationsApiUrl;

  constructor(private readonly http: HttpClient) {}

  timeline(claimId: string) {
    return this.http.get<TimelineEntry[]>(`${this.base}/api/claims/${claimId}/timeline`);
  }

  mine() {
    return this.http.get<NotificationItem[]>(`${this.base}/api/notifications/me`);
  }
}
