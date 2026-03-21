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
