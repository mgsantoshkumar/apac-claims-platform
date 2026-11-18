export type Role = 'CLAIMANT' | 'ADJUSTER' | 'MANAGER';
export type Market = 'SG' | 'HK' | 'MY' | 'TH' | 'VN' | 'AU';
export type Lob = 'MOTOR' | 'PROPERTY';
export type ClaimStatus =
  | 'SUBMITTED'
  | 'ASSIGNED'
  | 'INFO_REQUESTED'
  | 'UNDER_REVIEW'
  | 'SETTLED'
  | 'REJECTED';

export interface LoginResponse {
  token: string;
  userId: string;
  username: string;
  displayName: string;
  role: Role;
  team?: string | null;
}

export interface Claim {
  id: string;
  claimNumber: string;
  market: Market;
  lob: Lob;
  status: ClaimStatus;
  claimantUserId: string;
  adjusterUserId?: string | null;
  incidentSummary: string;
  incidentDescription: string;
  incidentDate: string;
  location: string;
  estimatedLoss: number;
  reserveAmount?: number | null;
  settlementAmount?: number | null;
  infoRequestMessage?: string | null;
  infoResponseMessage?: string | null;
  decisionReason?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface WorkloadSummary {
  byStatus: Record<string, number>;
  byMarket: Record<string, number>;
  openReserveExposure: number;
  unassignedCount: number;
  myAssignedCount: number;
}

export interface TimelineEntry {
  id: string;
  claimId: string;
  claimNumber: string;
  eventType: string;
  status: ClaimStatus;
  actorUserId?: string;
  message?: string;
  occurredAt: string;
}

export interface NotificationItem {
  id: string;
  userId: string;
  claimId: string;
  claimNumber: string;
  eventType: string;
  title: string;
  body: string;
  read: boolean;
  createdAt: string;
}
