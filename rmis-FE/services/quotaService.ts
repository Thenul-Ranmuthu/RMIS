// RMIS/files/services/quotaService.ts

import { QuotaFilters, QuotaPaginatedResponse} from '@/types/quota';
import { getToken } from '@/services/authService';


const BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:5050';

export const getQuotaRequests = async (
    filters: QuotaFilters,
    page: number = 1,
    pageSize: number = 5,
): Promise<QuotaPaginatedResponse> => {
    const params = new URLSearchParams();
    params.set('page', String(page));
    params.set('limit', String(pageSize));

    if (filters.companyName)    params.set('company_name',    filters.companyName);
    if (filters.status)         params.set('status',          filters.status);
    if (filters.submissionDate) params.set('submission_date', filters.submissionDate);

    const hasFilters = filters.companyName || filters.status || filters.submissionDate;
    const endpoint = hasFilters ? 'filter' : 'paginated';
    const url = `${BASE_URL}/ministry/quota-requests/${endpoint}?${params}`;

    // ← attach token to every request
    const token = getToken();
    const response = await fetch(url, {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
    });

    if (!response.ok) throw new Error('Failed to fetch quota requests');
    return response.json();
};