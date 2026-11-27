import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AuthService } from './core/auth.service';
import { Claim, ExposureLine, Paged, WorkloadSummary, describeError } from './core/models';

@Component({
  selector: 'app-officer',
  standalone: true,
  imports: [DecimalPipe, DatePipe],
  template: `
    @if (message()) { <p class="error">{{ message() }}</p> }

    <section>
      <h2>Unassigned queue (oldest first)</h2>
      <table>
        <tr><th>Claim</th><th>Market</th><th>Type</th><th>Amount</th><th>Submitted</th><th></th></tr>
        @for (c of queue(); track c.id) {
          <tr>
            <td>{{ c.claimNumber }}</td><td>{{ c.market }}</td><td>{{ c.claimType }}</td>
            <td>{{ c.claimedAmount | number:'1.2-2' }} {{ c.currency }}</td>
            <td>{{ c.createdAt | date:'short' }}</td>
            <td>@if (isOfficer) { <button (click)="act(c, 'pick-up')">Pick up</button> }</td>
          </tr>
        } @empty { <tr><td colspan="6">Queue is empty.</td></tr> }
      </table>
    </section>

    @if (isOfficer) {
      <section>
        <h2>My open claims</h2>
        <table>
          <tr><th>Claim</th><th>Status</th><th>Claimed</th><th>Reserve</th><th>Actions</th></tr>
          @for (c of mine(); track c.id) {
            <tr>
              <td>{{ c.claimNumber }}</td>
              <td><span class="badge">{{ c.status }}</span></td>
              <td>{{ c.claimedAmount | number:'1.2-2' }} {{ c.currency }}</td>
              <td>{{ c.reserveAmount ?? '-' }}</td>
              <td>
                @switch (c.status) {
                  @case ('UNDER_REVIEW') {
                    <button (click)="assess(c)">Assess</button>
                    <button (click)="requestInfo(c)">Request info</button>
                  }
                  @case ('INFO_REQUESTED') { Waiting for claimant }
                  @case ('ASSESSED') {
                    <button (click)="act(c, 'approve')">Approve</button>
                    <button (click)="reject(c)">Reject</button>
                  }
                  @case ('APPROVED') { <button (click)="act(c, 'settle')">Settle</button> }
                }
              </td>
            </tr>
          } @empty { <tr><td colspan="5">Nothing assigned to you.</td></tr> }
        </table>
      </section>
    }

    <section>
      <h2>Team workload</h2>
      @if (workload(); as w) {
        <p>Unassigned claims: <strong>{{ w.unassignedClaims }}</strong></p>
        <table>
          <tr><th>Officer</th><th>Open claims</th><th>Oldest open claim</th></tr>
          @for (o of w.officers; track o.officerId) {
            <tr><td>{{ o.officerId }}</td><td>{{ o.openClaims }}</td><td>{{ o.oldestOpenClaim | date:'short' }}</td></tr>
          }
        </table>
      }
    </section>

    @if (!isOfficer) {
      <section>
        <h2>Outstanding liability exposure</h2>
        <table>
          <tr><th>Market</th><th>Currency</th><th>Open claims</th><th>Exposure</th></tr>
          @for (x of exposure(); track x.market + x.currency) {
            <tr><td>{{ x.market }}</td><td>{{ x.currency }}</td><td>{{ x.openClaims }}</td><td>{{ x.exposure | number:'1.2-2' }}</td></tr>
          }
        </table>
      </section>
    }
  `,
})
export class OfficerComponent implements OnInit {
  private http = inject(HttpClient);
  private auth = inject(AuthService);
  isOfficer = this.auth.user()?.role === 'OFFICER';
  queue = signal<Claim[]>([]);
  mine = signal<Claim[]>([]);
  workload = signal<WorkloadSummary | null>(null);
  exposure = signal<ExposureLine[]>([]);
  message = signal('');

  ngOnInit() { this.refresh(); }

  refresh() {
    this.http.get<Paged<Claim>>('/api/claims?unassigned=true&size=50').subscribe(p => this.queue.set(p.items));
    this.http.get<WorkloadSummary>('/api/dashboard/workload').subscribe(w => this.workload.set(w));
    if (this.isOfficer) {
      const me = this.auth.user()!.userId;
      this.http.get<Paged<Claim>>(`/api/claims?assignedTo=${me}&size=50`).subscribe(p =>
        this.mine.set(p.items.filter(c => c.status !== 'SETTLED' && c.status !== 'REJECTED')));
    } else {
      this.http.get<ExposureLine[]>('/api/dashboard/exposure').subscribe(e => this.exposure.set(e));
    }
  }

  act(c: Claim, action: string, body: object = {}) {
    this.http.post(`/api/claims/${c.id}/${action}`, body).subscribe({
      next: () => { this.message.set(''); this.refresh(); },
      error: e => { this.message.set(describeError(e)); this.refresh(); },   // e.g. 409 when someone else picked it up first
    });
  }

  assess(c: Claim) {
    const v = window.prompt(`Reserve amount (${c.currency})`, String(c.claimedAmount));
    if (v) this.act(c, 'assess', { reserveAmount: Number(v) });
  }
  requestInfo(c: Claim) {
    const q = window.prompt('Question for the claimant?');
    if (q) this.act(c, 'info-requests', { question: q });
  }
  reject(c: Claim) {
    const r = window.prompt('Rejection reason?');
    if (r) this.act(c, 'reject', { reason: r });
  }
}