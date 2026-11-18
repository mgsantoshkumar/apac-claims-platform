import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ClaimsApiService } from '../../core/claims-api.service';
import { Claim, ClaimStatus, Lob, Market } from '../../shared/models/claim.models';

@Component({
  selector: 'app-my-claims',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatTableModule,
    MatButtonModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule
  ],
  template: `
    <div class="header">
      <div>
        <h1>My claims</h1>
        <p>Track status and respond when the adjuster asks for more information.</p>
      </div>
      <a mat-flat-button color="primary" routerLink="/claimant/new">Report incident</a>
    </div>

    @if (loading()) {
      <p>Loading claims…</p>
    } @else if (error()) {
      <p class="error">{{ error() }}</p>
    } @else if (claims().length === 0) {
      <p class="empty">No claims yet. Report an incident to get started.</p>
    } @else {
      <div class="filters">
        <mat-form-field appearance="outline">
          <mat-label>Claim ID / number</mat-label>
          <input
            matInput
            [ngModel]="claimQuery()"
            (ngModelChange)="claimQuery.set($event)"
            placeholder="Search claim…"
          />
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Status</mat-label>
          <mat-select [ngModel]="statusFilter()" (ngModelChange)="statusFilter.set($event)">
            <mat-option value="">All statuses</mat-option>
            @for (status of statuses; track status) {
              <mat-option [value]="status">{{ status }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Market</mat-label>
          <mat-select [ngModel]="marketFilter()" (ngModelChange)="marketFilter.set($event)">
            <mat-option value="">All markets</mat-option>
            @for (market of markets; track market) {
              <mat-option [value]="market">{{ market }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Line of business</mat-label>
          <mat-select [ngModel]="lobFilter()" (ngModelChange)="lobFilter.set($event)">
            <mat-option value="">All LOBs</mat-option>
            @for (lob of lobs; track lob) {
              <mat-option [value]="lob">{{ lob }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
        <mat-form-field appearance="outline" class="summary">
          <mat-label>Summary</mat-label>
          <input
            matInput
            [ngModel]="summaryQuery()"
            (ngModelChange)="summaryQuery.set($event)"
            placeholder="Search summary…"
          />
        </mat-form-field>
        <button mat-stroked-button type="button" (click)="clearFilters()" [disabled]="!hasActiveFilters()">
          Clear filters
        </button>
      </div>

      <p class="count">Showing {{ filteredClaims().length }} of {{ claims().length }} claims</p>

      @if (filteredClaims().length === 0) {
        <p class="empty">No claims match the current filters.</p>
      } @else {
        <table mat-table [dataSource]="filteredClaims()" class="table">
          <ng-container matColumnDef="claimNumber">
            <th mat-header-cell *matHeaderCellDef>Claim</th>
            <td mat-cell *matCellDef="let row">{{ row.claimNumber }}</td>
          </ng-container>
          <ng-container matColumnDef="summary">
            <th mat-header-cell *matHeaderCellDef>Summary</th>
            <td mat-cell *matCellDef="let row">{{ row.incidentSummary }}</td>
          </ng-container>
          <ng-container matColumnDef="market">
            <th mat-header-cell *matHeaderCellDef>Market</th>
            <td mat-cell *matCellDef="let row">{{ row.market }} / {{ row.lob }}</td>
          </ng-container>
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Status</th>
            <td mat-cell *matCellDef="let row"><span class="chip">{{ row.status }}</span></td>
          </ng-container>
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef></th>
            <td mat-cell *matCellDef="let row">
              <a mat-button [routerLink]="['/claimant/claims', row.id]">Open</a>
            </td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="columns"></tr>
          <tr mat-row *matRowDef="let row; columns: columns"></tr>
        </table>
      }
    }
  `,
  styles: `
    .header { display: flex; justify-content: space-between; gap: 1rem; align-items: end; margin-bottom: 1.5rem; }
    h1 { margin: 0; font-family: 'Source Serif 4', Georgia, serif; color: #102a43; }
    p { color: #486581; margin: 0.35rem 0 0; }
    .filters {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
      gap: 0.75rem 1rem;
      align-items: start;
      margin-bottom: 0.5rem;
    }
    .filters .summary { grid-column: span 2; }
    .filters button { align-self: center; height: 56px; }
    .count { margin: 0 0 0.75rem; font-size: 0.9rem; }
    .table { width: 100%; background: #fff; }
    .chip { background: #d9e2ec; border-radius: 999px; padding: 0.15rem 0.65rem; font-size: 0.8rem; }
    .error { color: #9b1c1c; }
    .empty { padding: 2rem; background: #fff; border: 1px dashed #bcccdc; }
    @media (max-width: 700px) {
      .filters .summary { grid-column: span 1; }
      .filters button { height: auto; }
    }
  `
})
export class MyClaimsComponent implements OnInit {
  readonly claims = signal<Claim[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly claimQuery = signal('');
  readonly statusFilter = signal<ClaimStatus | ''>('');
  readonly marketFilter = signal<Market | ''>('');
  readonly lobFilter = signal<Lob | ''>('');
  readonly summaryQuery = signal('');
  readonly columns = ['claimNumber', 'summary', 'market', 'status', 'actions'];
  readonly statuses: ClaimStatus[] = [
    'SUBMITTED',
    'ASSIGNED',
    'INFO_REQUESTED',
    'UNDER_REVIEW',
    'SETTLED',
    'REJECTED'
  ];
  readonly markets: Market[] = ['SG', 'HK', 'MY', 'TH', 'VN', 'AU'];
  readonly lobs: Lob[] = ['MOTOR', 'PROPERTY'];

  readonly filteredClaims = computed(() => {
    const claimQ = this.claimQuery().trim().toLowerCase();
    const summaryQ = this.summaryQuery().trim().toLowerCase();
    const status = this.statusFilter();
    const market = this.marketFilter();
    const lob = this.lobFilter();

    return this.claims().filter((claim) => {
      if (status && claim.status !== status) {
        return false;
      }
      if (market && claim.market !== market) {
        return false;
      }
      if (lob && claim.lob !== lob) {
        return false;
      }
      if (claimQ) {
        const haystack = `${claim.claimNumber} ${claim.id}`.toLowerCase();
        if (!haystack.includes(claimQ)) {
          return false;
        }
      }
      if (summaryQ && !claim.incidentSummary.toLowerCase().includes(summaryQ)) {
        return false;
      }
      return true;
    });
  });

  readonly hasActiveFilters = computed(
    () =>
      !!this.claimQuery().trim() ||
      !!this.statusFilter() ||
      !!this.marketFilter() ||
      !!this.lobFilter() ||
      !!this.summaryQuery().trim()
  );

  constructor(private readonly api: ClaimsApiService) {}

  ngOnInit(): void {
    this.api.list().subscribe({
      next: (claims) => {
        this.claims.set(claims);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load claims. Is claims-service running?');
        this.loading.set(false);
      }
    });
  }

  clearFilters(): void {
    this.claimQuery.set('');
    this.statusFilter.set('');
    this.marketFilter.set('');
    this.lobFilter.set('');
    this.summaryQuery.set('');
  }
}
