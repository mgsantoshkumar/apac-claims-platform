import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Claim, WorkloadSummary } from '../shared/models/claim.models';

@Injectable({ providedIn: 'root' })
export class ClaimsApiService {
  private readonly base = `${environment.claimsApiUrl}/api/claims`;

  constructor(private readonly http: HttpClient) {}

  list() {
    return this.http.get<Claim[]>(this.base);
  }

  get(id: string) {
    return this.http.get<Claim>(`${this.base}/${id}`);
  }

  create(body: unknown) {
    return this.http.post<Claim>(this.base, body);
  }

  assign(id: string) {
    return this.http.post<Claim>(`${this.base}/${id}/assign`, {});
  }

  requestInfo(id: string, message: string) {
    return this.http.post<Claim>(`${this.base}/${id}/request-info`, { message });
  }

  provideInfo(id: string, message: string) {
    return this.http.post<Claim>(`${this.base}/${id}/provide-info`, { message });
  }

  decide(id: string, body: unknown) {
    return this.http.post<Claim>(`${this.base}/${id}/decide`, body);
  }

  workload() {
    return this.http.get<WorkloadSummary>(`${this.base}/workload/summary`);
  }
}
