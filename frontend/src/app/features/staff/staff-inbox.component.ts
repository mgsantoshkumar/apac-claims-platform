import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { ClaimsApiService } from '../../core/claims-api.service';
import { Claim } from '../../shared/models/claim.models';

@Component({
  selector: 'app-staff-inbox',
  standalone: true,
  imports: [CommonModule, RouterLink, MatTableModule, MatButtonModule],
  template: `
    <h1>Claims inbox</h1>
    <p>Pick up unassigned claims, review your queue, and progress cases to settlement or rejection.</p>

    @if (loading()) {
      <p>Loading…</p>
    } @else if (error()) {
      <p class="error">{{ error() }}</p>
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
          <td mat-cell *matCellDef="let row">{{ row.status }}</td>
        </ng-container>
        <ng-container matColumnDef="actions">
          <th mat-header-cell *matHeaderCellDef></th>
          <td mat-cell *matCellDef="let row">
            <a mat-button [routerLink]="['/staff/claims', row.id]">Assess</a>
          </td>
        </ng-container>
        <tr mat-header-row *matHeaderRowDef="columns"></tr>
        <tr mat-row *matRowDef="let row; columns: columns"></tr>
      </table>
    }
  `,
  styles: `
    h1 { font-family: 'Source Serif 4', Georgia, serif; color: #102a43; margin-bottom: 0.35rem; }
    p { color: #486581; }
    .table { width: 100%; background: #fff; margin-top: 1rem; }
    .error { color: #9b1c1c; }
  `
})
export class StaffInboxComponent implements OnInit {
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
        this.error.set('Could not load inbox.');
        this.loading.set(false);
      }
    });
  }
}
