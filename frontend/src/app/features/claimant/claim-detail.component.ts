import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ClaimsApiService } from '../../core/claims-api.service';
import { NotificationsApiService } from '../../core/notifications-api.service';
import { Claim, TimelineEntry } from '../../shared/models/claim.models';

@Component({
  selector: 'app-claim-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, MatButtonModule, MatFormFieldModule, MatInputModule],
  template: `
    <a mat-button class="back" routerLink="/claimant">← Back to My claims</a>

    @if (loading()) {
      <p>Loading claim…</p>
    } @else if (!claim()) {
      <p class="error">Claim not found.</p>
    } @else {
      <div class="header">
        <div>
          <p class="eyebrow">{{ claim()!.claimNumber }}</p>
          <h1>{{ claim()!.incidentSummary }}</h1>
          <p>{{ claim()!.market }} · {{ claim()!.lob }} · <strong>{{ claim()!.status }}</strong></p>
        </div>
      </div>

      <section class="grid">
        <article>
          <h2>Incident</h2>
          <p>{{ claim()!.incidentDescription }}</p>
          <dl>
            <div><dt>Date</dt><dd>{{ claim()!.incidentDate }}</dd></div>
            <div><dt>Location</dt><dd>{{ claim()!.location }}</dd></div>
            <div><dt>Estimated loss</dt><dd>{{ claim()!.estimatedLoss | number:'1.2-2' }}</dd></div>
            @if (claim()!.decisionReason) {
              <div><dt>Decision</dt><dd>{{ claim()!.decisionReason }}</dd></div>
            }
          </dl>

          @if (claim()!.status === 'INFO_REQUESTED') {
            <div class="info-box">
              <h3>Information requested</h3>
              <p>{{ claim()!.infoRequestMessage }}</p>
              <mat-form-field appearance="outline" class="full">
                <mat-label>Your response</mat-label>
                <textarea matInput rows="3" [(ngModel)]="response"></textarea>
              </mat-form-field>
              <button mat-flat-button color="primary" type="button" (click)="provideInfo()" [disabled]="saving()">
                Send response
              </button>
            </div>
          }
          @if (message()) {
            <p class="ok">{{ message() }}</p>
          }
          @if (error()) {
            <p class="error">{{ error() }}</p>
          }
        </article>

        <aside>
          <h2>Timeline</h2>
          @if (timeline().length === 0) {
            <p class="muted">No events yet. Timeline fills as Kafka events are projected.</p>
          } @else {
            <ol class="timeline">
              @for (item of timeline(); track item.id) {
                <li>
                  <strong>{{ item.eventType }}</strong>
                  <span>{{ item.occurredAt | date:'medium' }}</span>
                  <p>{{ item.message }}</p>
                </li>
              }
            </ol>
          }
        </aside>
      </section>
    }
  `,
  styles: `
    .back { margin: 0 0 0.75rem -0.5rem; color: #0b3a53; }
    .eyebrow { text-transform: uppercase; letter-spacing: 0.08em; color: #627d98; font-size: 0.8rem; margin: 0; }
    h1 { font-family: 'Source Serif 4', Georgia, serif; margin: 0.25rem 0; color: #102a43; }
    .grid { display: grid; grid-template-columns: 1.4fr 1fr; gap: 1.5rem; margin-top: 1.5rem; }
    article, aside { background: #fff; padding: 1.25rem; }
    h2 { margin-top: 0; font-size: 1.1rem; }
    dl { display: grid; gap: 0.5rem; }
    dt { color: #627d98; font-size: 0.85rem; }
    dd { margin: 0; }
    .info-box { margin-top: 1rem; padding: 1rem; background: #f0f4f8; }
    .full { width: 100%; display: block; }
    .timeline { list-style: none; padding: 0; margin: 0; display: grid; gap: 0.85rem; }
    .timeline li { border-left: 3px solid #0b3a53; padding-left: 0.75rem; }
    .timeline span { display: block; color: #829ab1; font-size: 0.8rem; }
    .muted { color: #829ab1; }
    .error { color: #9b1c1c; }
    .ok { color: #0b6e4f; }
    @media (max-width: 900px) { .grid { grid-template-columns: 1fr; } }
  `
})
export class ClaimDetailComponent implements OnInit {
  readonly claim = signal<Claim | null>(null);
  readonly timeline = signal<TimelineEntry[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly message = signal<string | null>(null);
  response = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly claimsApi: ClaimsApiService,
    private readonly notificationsApi: NotificationsApiService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.claimsApi.get(id).subscribe({
      next: (claim) => {
        this.claim.set(claim);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
    this.refreshTimeline(id);
  }

  provideInfo(): void {
    const claim = this.claim();
    if (!claim || !this.response.trim()) {
      return;
    }
    this.saving.set(true);
    this.claimsApi.provideInfo(claim.id, this.response.trim()).subscribe({
      next: (updated) => {
        this.claim.set(updated);
        this.message.set('Response sent. Status moved to UNDER_REVIEW.');
        this.saving.set(false);
        this.refreshTimeline(claim.id);
      },
      error: () => {
        this.error.set('Could not send response.');
        this.saving.set(false);
      }
    });
  }

  private refreshTimeline(id: string): void {
    this.notificationsApi.pollTimeline(id).subscribe({
      next: (items) => this.timeline.set(items),
      error: () => this.timeline.set([])
    });
  }
}
