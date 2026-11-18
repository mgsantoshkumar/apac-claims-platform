import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ClaimsApiService } from '../../core/claims-api.service';
import { NotificationsApiService } from '../../core/notifications-api.service';
import { Claim, TimelineEntry } from '../../shared/models/claim.models';

@Component({
  selector: 'app-staff-claim',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule
  ],
  template: `
    @if (!claim()) {
      <p>Loading…</p>
    } @else {
      <h1>{{ claim()!.claimNumber }} · {{ claim()!.incidentSummary }}</h1>
      <p>{{ claim()!.status }} · {{ claim()!.market }} / {{ claim()!.lob }}</p>

      <section class="grid">
        <article>
          <p>{{ claim()!.incidentDescription }}</p>
          <p><strong>Estimated loss:</strong> {{ claim()!.estimatedLoss | number:'1.2-2' }}</p>
          @if (claim()!.infoResponseMessage) {
            <p><strong>Claimant response:</strong> {{ claim()!.infoResponseMessage }}</p>
          }

          <div class="actions">
            @if (claim()!.status === 'SUBMITTED') {
              <button mat-flat-button color="primary" type="button" (click)="assign()">Pick up claim</button>
            }

            @if (claim()!.status === 'ASSIGNED' || claim()!.status === 'UNDER_REVIEW') {
              <mat-form-field appearance="outline" class="full">
                <mat-label>Request more information</mat-label>
                <textarea matInput rows="2" [(ngModel)]="infoMessage"></textarea>
              </mat-form-field>
              <button mat-stroked-button type="button" (click)="requestInfo()">Request info</button>

              <div class="decide">
                <mat-form-field appearance="outline">
                  <mat-label>Outcome</mat-label>
                  <mat-select [(ngModel)]="outcome">
                    <mat-option value="SETTLED">Settle</mat-option>
                    <mat-option value="REJECTED">Reject</mat-option>
                  </mat-select>
                </mat-form-field>
                <mat-form-field appearance="outline">
                  <mat-label>Settlement amount</mat-label>
                  <input matInput type="number" [(ngModel)]="settlementAmount" />
                </mat-form-field>
                <mat-form-field appearance="outline" class="full">
                  <mat-label>Decision reason</mat-label>
                  <textarea matInput rows="2" [(ngModel)]="reason"></textarea>
                </mat-form-field>
                <button mat-flat-button color="primary" type="button" (click)="decide()">Record decision</button>
              </div>
            }
          </div>
          @if (message()) { <p class="ok">{{ message() }}</p> }
          @if (error()) { <p class="error">{{ error() }}</p> }
        </article>

        <aside>
          <h2>Timeline</h2>
          <ol class="timeline">
            @for (item of timeline(); track item.id) {
              <li>
                <strong>{{ item.eventType }}</strong>
                <span>{{ item.occurredAt | date:'medium' }}</span>
                <p>{{ item.message }}</p>
              </li>
            }
          </ol>
        </aside>
      </section>
    }
  `,
  styles: `
    h1 { font-family: 'Source Serif 4', Georgia, serif; color: #102a43; }
    .grid { display: grid; grid-template-columns: 1.4fr 1fr; gap: 1.25rem; }
    article, aside { background: #fff; padding: 1.25rem; }
    .actions { display: grid; gap: 0.75rem; margin-top: 1rem; }
    .decide { display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; margin-top: 0.5rem; }
    .full { grid-column: 1 / -1; width: 100%; }
    .timeline { list-style: none; padding: 0; margin: 0; display: grid; gap: 0.75rem; }
    .timeline li { border-left: 3px solid #102a43; padding-left: 0.75rem; }
    .timeline span { display: block; color: #829ab1; font-size: 0.8rem; }
    .ok { color: #0b6e4f; }
    .error { color: #9b1c1c; }
    @media (max-width: 900px) { .grid, .decide { grid-template-columns: 1fr; } }
  `
})
export class StaffClaimComponent implements OnInit {
  readonly claim = signal<Claim | null>(null);
  readonly timeline = signal<TimelineEntry[]>([]);
  readonly message = signal<string | null>(null);
  readonly error = signal<string | null>(null);
  infoMessage = '';
  outcome: 'SETTLED' | 'REJECTED' = 'SETTLED';
  settlementAmount = 0;
  reason = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly claimsApi: ClaimsApiService,
    private readonly notificationsApi: NotificationsApiService
  ) {}

  ngOnInit(): void {
    this.reload(this.route.snapshot.paramMap.get('id')!);
  }

  assign(): void {
    const id = this.claim()!.id;
    this.claimsApi.assign(id).subscribe({
      next: (claim) => {
        this.claim.set(claim);
        this.message.set('Claim assigned to you.');
        this.refreshTimeline(id);
      },
      error: () => this.error.set('Assign failed.')
    });
  }

  requestInfo(): void {
    const id = this.claim()!.id;
    this.claimsApi.requestInfo(id, this.infoMessage).subscribe({
      next: (claim) => {
        this.claim.set(claim);
        this.message.set('Information requested from claimant.');
        this.refreshTimeline(id);
      },
      error: () => this.error.set('Request failed.')
    });
  }

  decide(): void {
    const id = this.claim()!.id;
    this.claimsApi
      .decide(id, {
        outcome: this.outcome,
        reason: this.reason,
        settlementAmount: this.outcome === 'SETTLED' ? this.settlementAmount : 0
      })
      .subscribe({
        next: (claim) => {
          this.claim.set(claim);
          this.message.set(`Claim ${claim.status}.`);
          this.refreshTimeline(id);
        },
        error: () => this.error.set('Decision failed.')
      });
  }

  private reload(id: string): void {
    this.claimsApi.get(id).subscribe({
      next: (claim) => {
        this.claim.set(claim);
        this.settlementAmount = Number(claim.estimatedLoss);
      }
    });
    this.refreshTimeline(id);
  }

  private refreshTimeline(id: string): void {
    this.notificationsApi.pollTimeline(id).subscribe({
      next: (items) => this.timeline.set(items),
      error: () => this.timeline.set([])
    });
  }
}
