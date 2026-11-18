import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { ClaimsApiService } from '../../core/claims-api.service';
import { Claim } from '../../shared/models/claim.models';

@Component({
  selector: 'app-my-claims',
  standalone: true,
  imports: [CommonModule, RouterLink, MatTableModule, MatButtonModule, MatChipsModule],
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
      <table mat-table [dataSource]="claims()" class="table">
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
  `,
  styles: `
    .header { display: flex; justify-content: space-between; gap: 1rem; align-items: end; margin-bottom: 1.5rem; }
    h1 { margin: 0; font-family: 'Source Serif 4', Georgia, serif; color: #102a43; }
    p { color: #486581; margin: 0.35rem 0 0; }
    .table { width: 100%; background: #fff; }
    .chip { background: #d9e2ec; border-radius: 999px; padding: 0.15rem 0.65rem; font-size: 0.8rem; }
    .error { color: #9b1c1c; }
    .empty { padding: 2rem; background: #fff; border: 1px dashed #bcccdc; }
  `
})
export class MyClaimsComponent implements OnInit {
  readonly claims = signal<Claim[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly columns = ['claimNumber', 'summary', 'market', 'status', 'actions'];

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
}
