// RMIS/files/services/quotaService.ts

import { QuotaFilters, QuotaPaginatedResponse} from '@/types/quota';

const BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:5050';
//const PAGE_SIZE = 20;

// export const getQuotaRequests = async (
//     filters: QuotaFilters,
//     //page: number
// ): Promise<QuotaPaginatedResponse> => {
//     const params = new URLSearchParams({
//         //page: String(page),
//         //pageSize: String(PAGE_SIZE),
//     });

//     if (filters.companyName)    params.set('companyName',    filters.companyName);
//     if (filters.status)         params.set('status',         filters.status);
//     if (filters.submissionDate) params.set('submissionDate', filters.submissionDate);

//     //const response = await fetch(`${BASE_URL}/ministry/quota-requests?${params}`);
//     const response = await fetch(`${BASE_URL}/ministry/quota-requests?`);

//     if (!response.ok) throw new Error('Failed to fetch quota requests');

//     return response.json();
// };

export const getQuotaRequests = async (
    filters: QuotaFilters,
): Promise<QuotaPaginatedResponse> => {
    const params = new URLSearchParams();

    if (filters.companyName)    params.set('companyName',    filters.companyName);
    if (filters.status)         params.set('status',         filters.status);
    if (filters.submissionDate) params.set('submissionDate', filters.submissionDate);

    const url = `${BASE_URL}/ministry/quota-requests?${params}`;
    const response = await fetch(url);

    if (!response.ok) throw new Error('Failed to fetch quota requests');

    const array = await response.json();

    // Backend returns a plain array — wrap it into the shape the frontend expects
    return {
        data: array,
        total: array.length,
    };
};

// export const getQuotaStats = async (): Promise<QuotaStats> => {
//     const response = await fetch(`${BASE_URL}/ministry/quota-requests/stats`);

//     if (!response.ok) throw new Error('Failed to fetch quota stats');

//     return response.json();
// };