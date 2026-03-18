export type QuotaStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface QuotaRequest {
    request_id: string;
    company_name: string;
    requested_quota: number;      
    status: QuotaStatus;
    submission_date: string;
}

export interface QuotaPaginatedResponse {
    data: QuotaRequest[];
    totalRecords: number;  
    totalPages: number;    
    currentPage: number;   
}

export interface QuotaFilters {
    companyName: string;
    status: QuotaStatus | '';
    submissionDate: string;
}