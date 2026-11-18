import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ClaimsApiService } from '../../core/claims-api.service';
import { WorkloadSummary } from '../../shared/models/claim.models';

@Component({
  selector: 'app-workload',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h1>Team workload</h1>
    <p>Live counts from Redis, rebuilt on claim transitions. Open reserve shows liability still on the books.</p>

    @if (loading()) {
      <p>Loading summary…</p>
    } @else if (error()) {
      <p class="error">{{ error() }}</p>
    } @else if (summary()) {
      <div class="metrics">
        <div>
          <span>Unassigned</span>
          <strong>{{ summary()!.unassignedCount }}</strong>
        </div>
        <div>
          <span>My open queue</span>
          <strong>{{ summary()!.myAssignedCount }}</strong>
        </div>
        <div>
          <span>Open reserve exposure</span>
          <strong>{{ summary()!.openReserveExposure | number:'1.2-2' }}</strong>
        </div>
      </div>

      <div class="panels">
        <section>
          <h2>By status</h2>
          <ul>
            @for (entry of statusEntries(); track entry[0]) {
              <li><span>{{ entry[0] }}</span><strong>{{ entry[1] }}</strong></li>
            }
          </ul>
        </section>
        <section>
          <h2>By market</h2>
          <ul>
            @for (entry of marketEntries(); track entry[0]) {
              <li><span>{{ entry[0] }}</span><strong>{{ entry[1] }}</strong></li>
            }
          </ul>
        </section>
      </div>
    }
  `,
  styles: `
    h1 { font-family: 'Source Serif 4', Georgia, serif; color: #102a43; margin-bottom: 0.35rem; }
    p { color: #486581; }
    .metrics {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 1rem;
      margin: 1.5rem 0;
    }
    .metrics div, section {
      background: #fff;
      padding: 1.25rem;
    }
    .metrics span, li span { color: #627d98; display: block; font-size: 0.9rem; }
    .metrics strong { font-size: 1.8rem; color: #102a43; }
    .panels { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
    ul { list-style: none; padding: 0; margin: 0; display: grid; gap: 0.65rem; }
    li { display: flex; justify-content: space-between; border-bottom: 1px solid #d9e2ec; padding-bottom: 0.35rem; }
    .error { color: #9b1c1c; }
    @media (max-width: 800px) {
      .metrics, .panels { grid-template-columns: 1fr; }
    }
  `
})
export class WorkloadComponent implements OnInit {
  readonly summary = signal<WorkloadSummary | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  constructor(private readonly api: ClaimsApiService) {}

  ngOnInit(): void {
    this.api.workload().subscribe({
      next: (summary) => {
        this.summary.set(summary);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load workload summary.');
        this.loading.set(false);
      }
    });
  }

  statusEntries(): [string, number][] {
    return Object.entries(this.summary()?.byStatus ?? {});
  }

  marketEntries(): [string, number][] {
    return Object.entries(this.summary()?.byMarket ?? {});
  }
}
