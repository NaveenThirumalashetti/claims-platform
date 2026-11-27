export interface Claim {
  id: string; claimNumber: string; market: string; claimType: string; status: string;
  policyNumber: string; incidentDate: string; description: string;
  claimedAmount: number; reserveAmount: number | null; currency: string;
  assignedOfficerId: string | null; version: number; createdAt: string;
}
export interface Paged<T> { items: T[]; page: number; size: number; totalElements: number; totalPages: number; }
export interface InfoRequest { id: string; requestedBy: string; question: string; response: string | null; }
export interface OfficerWorkload { officerId: string; openClaims: number; oldestOpenClaim: string; }
export interface WorkloadSummary { unassignedClaims: number; officers: OfficerWorkload[]; }
export interface ExposureLine { market: string; currency: string; openClaims: number; exposure: number; }

/** Turns the API's RFC 7807 error body (with optional field errors) into a readable message. */
export function describeError(e: any): string {
  const fields = e?.error?.errors;
  if (fields) return Object.entries(fields).map(([k, v]) => `${k}: ${v}`).join('; ');
  return e?.error?.detail ?? 'Request failed';
}