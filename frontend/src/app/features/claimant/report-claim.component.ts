import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { ClaimsApiService } from '../../core/claims-api.service';
import { Lob, Market } from '../../shared/models/claim.models';

@Component({
  selector: 'app-report-claim',
  standalone: true,
  imports: [CommonModule, FormsModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule],
  template: `
    <h1>Report an incident</h1>
    <p>Submit a motor or property claim. You will receive updates as staff progress the case.</p>

    <form class="form" (ngSubmit)="submit()">
      <mat-form-field appearance="outline">
        <mat-label>Market</mat-label>
        <mat-select [(ngModel)]="market" name="market" required>
          @for (m of markets; track m) {
            <mat-option [value]="m">{{ m }}</mat-option>
          }
        </mat-select>
      </mat-form-field>
      <mat-form-field appearance="outline">
        <mat-label>Line of business</mat-label>
        <mat-select [(ngModel)]="lob" name="lob" required>
          <mat-option value="MOTOR">Motor</mat-option>
          <mat-option value="PROPERTY">Property</mat-option>
        </mat-select>
      </mat-form-field>
      <mat-form-field appearance="outline" class="full">
        <mat-label>Summary</mat-label>
        <input matInput [(ngModel)]="incidentSummary" name="summary" required maxlength="200" />
      </mat-form-field>
      <mat-form-field appearance="outline" class="full">
        <mat-label>Description</mat-label>
        <textarea matInput rows="4" [(ngModel)]="incidentDescription" name="description" required></textarea>
      </mat-form-field>
      <mat-form-field appearance="outline">
        <mat-label>Incident date</mat-label>
        <input matInput type="date" [(ngModel)]="incidentDate" name="date" required />
      </mat-form-field>
      <mat-form-field appearance="outline">
        <mat-label>Location</mat-label>
        <input matInput [(ngModel)]="location" name="location" required />
      </mat-form-field>
      <mat-form-field appearance="outline">
        <mat-label>Estimated loss</mat-label>
        <input matInput type="number" min="0" step="0.01" [(ngModel)]="estimatedLoss" name="loss" required />
      </mat-form-field>
      <button mat-flat-button color="primary" type="submit" [disabled]="saving()">
        {{ saving() ? 'Submitting…' : 'Submit claim' }}
      </button>
      @if (error()) {
        <p class="error">{{ error() }}</p>
      }
    </form>
  `,
  styles: `
    h1 { font-family: 'Source Serif 4', Georgia, serif; color: #102a43; margin-bottom: 0.35rem; }
    p { color: #486581; }
    .form {
      margin-top: 1.5rem;
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 0.75rem 1rem;
      background: #fff;
      padding: 1.25rem;
      max-width: 820px;
    }
    .full { grid-column: 1 / -1; }
    button { grid-column: 1 / -1; width: fit-content; }
    .error { color: #9b1c1c; grid-column: 1 / -1; }
    @media (max-width: 700px) { .form { grid-template-columns: 1fr; } }
  `
})
export class ReportClaimComponent {
  markets: Market[] = ['SG', 'HK', 'MY', 'TH', 'VN', 'AU'];
  market: Market = 'SG';
  lob: Lob = 'MOTOR';
  incidentSummary = '';
  incidentDescription = '';
  incidentDate = new Date().toISOString().slice(0, 10);
  location = '';
  estimatedLoss = 1000;
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  constructor(private readonly api: ClaimsApiService, private readonly router: Router) {}

  submit(): void {
    this.saving.set(true);
    this.error.set(null);
    this.api
      .create({
        market: this.market,
        lob: this.lob,
        incidentSummary: this.incidentSummary,
        incidentDescription: this.incidentDescription,
        incidentDate: this.incidentDate,
        location: this.location,
        estimatedLoss: this.estimatedLoss
      })
      .subscribe({
        next: (claim) => {
          this.saving.set(false);
          void this.router.navigate(['/claimant/claims', claim.id]);
        },
        error: () => {
          this.saving.set(false);
          this.error.set('Could not submit claim.');
        }
      });
  }
}
