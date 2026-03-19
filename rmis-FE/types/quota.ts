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