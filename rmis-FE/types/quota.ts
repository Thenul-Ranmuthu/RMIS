// rmis 23
// export type QuotaStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

// export interface QuotaRequest {
//     request_id: string;
//     company_name: string;
//     requested_quota: number;
//     status: QuotaStatus;
//     submission_date: string;
// }

// export interface QuotaPaginatedResponse {
//     data: QuotaRequest[];
//     totalRecords: number;
//     totalPages: number;
//     currentPage: number;
// }

// export interface QuotaFilters {
//     companyName: string;
//     status: QuotaStatus | '';
//     submissionDate: string;
// }

export type QuotaStatus = "PENDING" | "APPROVED" | "REJECTED";

export interface QuotaRequest {
  request_id: string;
  request_number?: string;
  company_id?: string | number;
  company_name: string;
  requested_quota: number;
  status: QuotaStatus;
  submission_date: string;
  created_at?: string;
  updated_at?: string;
  reviewed_at?: string;
  reviewed_by?: string;
}

export interface QuotaPaginatedResponse {
  data: QuotaRequest[];
  totalRecords: number;
  totalPages: number;
  currentPage: number;
}

export interface QuotaFilters {
  companyName: string;
  status: QuotaStatus | "";
  submissionDate: string;
}
// rmis 27
// RMIS-FE/types/quota.ts

export type QuotaStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface QuotaRequest {
    id: string;              // ← UUID for API calls
    request_id: string;      // ← REQ-0001 for display
    company_name: string;
    requested_quota: number;
    status: QuotaStatus;
    submission_date: string;
}

export interface QuotaRequestDetail {
    id: string;
    request_id: string;
    company_name: string;
    company_email: string;
    company_id: string;
    requested_quota: number;
    submission_date: string;
    status: QuotaStatus;
    reviewed_by: string | null;
    reviewed_at: string | null;
}

export interface QuotaFilters {
    companyName: string;
    status: QuotaStatus | '';
    submissionDate: string;
}

export interface QuotaPaginatedResponse {
    data: QuotaRequest[];
    totalRecords: number;
    totalPages: number;
    currentPage: number;
}

export interface QuotaStats {
    approvedTons: number;
    pendingCount: number;
    complianceRate: number;
}
