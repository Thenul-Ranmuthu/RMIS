export type QuotaStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface QuotaRequest {
    request_id: string;
    company_name: string;
    requested_quota: number;      // ← was requested_quota_amount
    status: QuotaStatus;
    submission_date: string;
}

// Backend returns a plain array, not a paginated object
export interface QuotaPaginatedResponse {
    data: QuotaRequest[];         // we'll map the array into this
    total: number;
}

export interface QuotaFilters {
    companyName: string;
    status: QuotaStatus | '';
    submissionDate: string;
}