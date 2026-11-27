import { Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Claim, InfoRequest, Paged, describeError } from './core/models';

@Component({
  selector: 'app-claimant',
  standalone: true,
  imports: [FormsModule, DecimalPipe],
  template: `
    <section>
      <h2>Report an incident</h2>
      <form (ngSubmit)="submit()" #f="ngForm">
        <label>Market
          <select name="market" [(ngModel)]="form.market">
            @for (m of markets; track m) { <option [value]="m">{{ m }}</option> }
          </select></label>
        <label>Type
          <select name="claimType" [(ngModel)]="form.claimType">
            <option value="MOTOR">Motor</option><option value="PROPERTY">Property</option>
          </select></label>
        <label>Policy number <input name="policyNumber" [(ngModel)]="form.policyNumber" required></label>
        <label>Incident date <input name="incidentDate" type="date" [(ngModel)]="form.incidentDate" required></label>
        <label>Description <textarea name="description" rows="3" [(ngModel)]="form.description" required></textarea></label>
        <label>Claimed amount <input name="claimedAmount" type="number" step="0.01" [(ngModel)]="form.claimedAmount" required></label>
        <label>Currency
          <select name="currency" [(ngModel)]="form.currency">
            @for (c of currencies; track c) { <option [value]="c">{{ c }}</option> }
          </select></label>
        <button type="submit" [disabled]="f.invalid">Submit claim</button>
        @if (message()) { <p>{{ message() }}</p> }
      </form>
    </section>

    <section>
      <h2>My claims</h2>
      <table>
        <tr><th>Claim</th><th>Status</th><th>Amount</th><th>Submitted</th><th></th></tr>
        @for (c of claims(); track c.id) {
          <tr>
            <td>{{ c.claimNumber }}</td>
            <td><span class="badge">{{ c.status }}</span></td>
            <td>{{ c.claimedAmount | number:'1.2-2' }} {{ c.currency }}</td>
            <td>{{ c.createdAt.substring(0, 10) }}</td>
            <td>@if (c.status === 'INFO_REQUESTED') { <button (click)="respond(c)">Provide information</button> }</td>
          </tr>
        } @empty { <tr><td colspan="5">No claims yet.</td></tr> }
      </table>
    </section>
  `,
})
export class ClaimantComponent implements OnInit {
  private http = inject(HttpClient);
  markets = ['SG', 'HK', 'MY', 'TH', 'ID', 'PH'];      // demo list
  currencies = ['SGD', 'HKD', 'MYR', 'THB', 'IDR', 'PHP'];
  claims = signal<Claim[]>([]);
  message = signal('');
  form = { market: 'SG', claimType: 'MOTOR', policyNumber: '', incidentDate: '', description: '',
           claimedAmount: null as number | null, currency: 'SGD' };

  ngOnInit() { this.load(); }

  load() {
    this.http.get<Paged<Claim>>('/api/claims/mine?size=50').subscribe(p => this.claims.set(p.items));
  }

  submit() {
    this.http.post<Claim>('/api/claims', this.form).subscribe({
      next: c => { this.message.set(`Submitted ${c.claimNumber}`); this.load(); },
      error: e => this.message.set(describeError(e)),
    });
  }

  respond(c: Claim) {
    this.http.get<InfoRequest[]>(`/api/claims/${c.id}/info-requests`).subscribe(list => {
      const open = list.find(r => !r.response);
      if (!open) return;
      const answer = window.prompt(open.question);
      if (!answer) return;
      this.http.post(`/api/claims/${c.id}/info-requests/${open.id}/response`, { response: answer }).subscribe({
        next: () => this.load(),
        error: e => this.message.set(describeError(e)),
      });
    });
  }
}