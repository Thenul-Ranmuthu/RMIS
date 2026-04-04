// RMIS-FE/services/quotaService.ts

import { QuotaFilters, QuotaPaginatedResponse, QuotaRequestDetail } from "@/types/quota";
import { getToken } from "@/services/authService";

const BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

// ── Helper: build auth headers ─────────────────────────────────────────────
const authHeaders = (token?: string | null) => ({
    "Content-Type": "application/json",
    Authorization: `Bearer ${token ?? getToken()}`,
});

// ─── Interfaces (company-side) ────────────────────────────────────────────

export interface CompanyQuotaRequest {
    id?: number | string;
    companyEmail?: string;
    requestedQuota: number;
    status?: string;
    createdAt?: string;
    updatedAt?: string;
    [key: string]: unknown;
}

export interface QuotaSummary {
    currentAvailableQuota: number | null;
    remainingYearlyQuota: number | null;
}

export interface QuotaListResponse {
    summary: QuotaSummary;
    requests: CompanyQuotaRequest[];
}

export interface AddQuotaPayload {
    companyEmail: string;
    requestedQuota: number;
}

// ─── Company-side response parser ─────────────────────────────────────────

const parseQuotaListResponse = (data: unknown): QuotaListResponse => {
    if (Array.isArray(data)) {
        return {
            summary: { currentAvailableQuota: null, remainingYearlyQuota: null },
            requests: data as CompanyQuotaRequest[],
        };
    }
    if (data && typeof data === "object") {
        const obj = data as Record<string, unknown>;
        const requests: CompanyQuotaRequest[] = Array.isArray(obj.quotas)
            ? (obj.quotas as CompanyQuotaRequest[])
            : Array.isArray(obj.quotaRequests)
                ? (obj.quotaRequests as CompanyQuotaRequest[])
                : Array.isArray(obj.data)
                    ? (obj.data as CompanyQuotaRequest[])
                    : [];
        const summary: QuotaSummary = {
            currentAvailableQuota:
                typeof obj.currentAvailableQuota === "number"
                    ? obj.currentAvailableQuota
                    : typeof obj.availableQuota === "number"
                        ? obj.availableQuota
                        : null,
            remainingYearlyQuota:
                typeof obj.remainingYearlyQuota === "number"
                    ? obj.remainingYearlyQuota
                    : typeof obj.yearlyQuota === "number"
                        ? obj.yearlyQuota
                        : null,
        };
        return { summary, requests };
    }
    return {
        summary: { currentAvailableQuota: null, remainingYearlyQuota: null },
        requests: [],
    };
};

// ─── Company-side API ─────────────────────────────────────────────────────

export const getQuotas = async (token: string): Promise<QuotaListResponse> => {
    const response = await fetch(`${BASE_URL}/quotaHeader/getQuotas`, {
        method: "GET",
        headers: authHeaders(token),
    });
    if (!response.ok) {
        const err = new Error(`Failed to fetch quotas: ${response.status}`);
        (err as Error & { status: number }).status = response.status;
        throw err;
    }
    const data = await response.json();
    return parseQuotaListResponse(data);
};

export const addQuota = async (
    token: string,
    payload: AddQuotaPayload
): Promise<unknown> => {
    const response = await fetch(`${BASE_URL}/quotaHeader/addQuota`, {
        method: "POST",
        headers: authHeaders(token),
        body: JSON.stringify(payload),
    });
    if (!response.ok) {
        let message = `Request failed: ${response.status}`;
        try {
            const err = await response.json();
            message = err.message || err.error || message;
        } catch {
            /* keep default */
        }
        throw new Error(message);
    }
    const text = await response.text();
    try {
        return JSON.parse(text);
    } catch {
        return { message: text };
    }
};

// ─── Ministry-side API ────────────────────────────────────────────────────

// ── Get paginated / filtered list ──────────────────────────────────────────
export const getQuotaRequests = async (
    filters: QuotaFilters,
    page: number = 1,
    pageSize: number = 5
): Promise<QuotaPaginatedResponse> => {
    const params = new URLSearchParams();
    params.set("page", String(page));
    params.set("limit", String(pageSize));

    if (filters.companyName)    params.set("company_name",    filters.companyName);
    if (filters.status)         params.set("status",          filters.status);
    if (filters.submissionDate) params.set("submission_date", filters.submissionDate);

    const hasFilters = filters.companyName || filters.status || filters.submissionDate;
    const endpoint = hasFilters ? "filter" : "paginated";
    const url = `${BASE_URL}/ministry/quota-requests/${endpoint}?${params}`;

    const response = await fetch(url, { headers: authHeaders() });

    if (!response.ok) {
        const text = await response.text();
        console.error("Status:", response.status, "Body:", text);
        throw new Error("Failed to fetch quota requests");
    }

    return response.json();
};

// ── Get single request detail by UUID ─────────────────────────────────────
export const getQuotaRequestById = async (id: string): Promise<QuotaRequestDetail> => {
    const url = `${BASE_URL}/ministry/quota-requests/${id}`;
    const response = await fetch(url, { headers: authHeaders() });
    if (!response.ok) throw new Error("Failed to fetch quota request detail");
    return response.json();
};

// ── Approve a quota request ────────────────────────────────────────────────
export const approveRequest = async (
    token: string,
    id: string
): Promise<string> => {
    const response = await fetch(
        `${BASE_URL}/ministry/quota-requests/statusApprove/${id}`,
        {
            method: "PATCH",
            headers: { Authorization: `Bearer ${token}` },
        }
    );
    const text = await response.text();
    if (!response.ok) {
        throw new Error(text || `Approval failed: ${response.status}`);
    }
    return text;
};

// ── Reject a quota request ─────────────────────────────────────────────────
export const rejectRequest = async (
    token: string,
    id: string
): Promise<string> => {
    const response = await fetch(
        `${BASE_URL}/ministry/quota-requests/statusReject/${id}`,
        {
            method: "PATCH",
            headers: authHeaders(token),
        }
    );
    const text = await response.text();
    if (!response.ok) {
        throw new Error(text || `Rejection failed: ${response.status}`);
    }
    return text;
};
