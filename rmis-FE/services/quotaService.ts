import { QuotaFilters, QuotaPaginatedResponse } from "@/types/quota";
import { getToken } from "@/services/authService";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:5050";

// ─── Interfaces (company-side) ────────────────────────────────

// export interface QuotaRequest {
//   id?: number | string;
//   companyEmail?: string;
//   requestedQuota: number;
//   status?: string;
//   createdAt?: string;
//   updatedAt?: string;
//   [key: string]: unknown;
// }

export interface QuotaRequest {
  requestId: string;
  requestNumber: number;
  companyName: string;
  requestedQuota: number;
  submissionDate: string;
  status: string;
  [key: string]: unknown;
}

export interface QuotaSummary {
  currentAvailableQuota: number | null;
  remainingYearlyQuota: number | null;
}

export interface QuotaListResponse {
  summary: QuotaSummary;
  requests: QuotaRequest[];
}

export interface AddQuotaPayload {
  companyEmail: string;
  requestedQuota: number;
}

// ─── Helpers ──────────────────────────────────────────────────

const authHeaders = (token: string) => ({
  "Content-Type": "application/json",
  Authorization: `Bearer ${token}`,
});

const parseQuotaListResponse = (data: unknown): QuotaListResponse => {
  if (Array.isArray(data)) {
    return {
      summary: { currentAvailableQuota: null, remainingYearlyQuota: null },
      requests: data as QuotaRequest[],
    };
  }
  if (data && typeof data === "object") {
    const obj = data as Record<string, unknown>;
    const requests: QuotaRequest[] = Array.isArray(obj.quotas)
      ? (obj.quotas as QuotaRequest[])
      : Array.isArray(obj.quotaRequests)
        ? (obj.quotaRequests as QuotaRequest[])
        : Array.isArray(obj.data)
          ? (obj.data as QuotaRequest[])
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

// ─── Company-side API ─────────────────────────────────────────

export const getQuotas = async (token: string): Promise<QuotaListResponse> => {
  const response = await fetch(`${API_BASE_URL}/company/getQuotas`, {
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
  payload: AddQuotaPayload,
): Promise<unknown> => {
  const response = await fetch(`${API_BASE_URL}/quotaHeader/addQuota`, {
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

// ─── Admin-side API ───────────────────────────────────────────

export const getQuotaRequests = async (
  filters: QuotaFilters,
  page: number = 1,
  pageSize: number = 5,
): Promise<QuotaPaginatedResponse> => {
  const params = new URLSearchParams();
  params.set("page", String(page));
  params.set("limit", String(pageSize));
  if (filters.companyName) params.set("company_name", filters.companyName);
  if (filters.status) params.set("status", filters.status);
  if (filters.submissionDate)
    params.set("submission_date", filters.submissionDate);

  const hasFilters =
    filters.companyName || filters.status || filters.submissionDate;
  const endpoint = hasFilters ? "filter" : "paginated";
  const url = `${API_BASE_URL}/ministry/quota-requests/${endpoint}?${params}`;

  const token = getToken();
  const response = await fetch(url, {
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  });

  if (!response.ok) {
    const text = await response.text();
    console.log("Status:", response.status, "Body:", text);
    throw new Error("Failed to fetch quota requests");
  }
  return response.json();
};

/**
 * Approve a quota request.
 * Endpoint: PATCH /ministry/statusApprove/{requestId}
 */
// export const approveRequest = async (
//   token: string,
//   requestId: string,
// ): Promise<string> => {
//   const response = await fetch(
//     `${API_BASE_URL}/ministry/statusApprove/${requestId}`,
//     {
//       method: "PATCH",
//       headers: authHeaders(token),
//     },
//   );
//   const text = await response.text();
//   console.log("approve status:", response.status, "body:", text);
//   if (!response.ok) {
//     throw new Error(text || `Approval failed: ${response.status}`);
//   }
//   return text; // e.g. "Status set to APPROVED"
// };
// export const approveRequest = async (
//   token: string,
//   requestId: string,
// ): Promise<string> => {
//   const url = `${API_BASE_URL}/ministry/statusApprove/${requestId}`;
//   console.log("PATCH URL:", url);
//   const response = await fetch(url, {
//     method: "PATCH",
//     headers: { Authorization: `Bearer ${token}` },
//   });
//   console.log("response.ok:", response.ok, "status:", response.status);
//   const text = await response.text();
//   console.log("response text:", text);
//   if (!response.ok) {
//     throw new Error(text || `Approval failed: ${response.status}`);
//   }
//   return text;
// };

export const approveRequest = async (
  token: string,
  requestId: string,
): Promise<string> => {
  const response = await fetch(
    `${API_BASE_URL}/ministry/statusApprove/${requestId}`,
    {
      method: "PATCH",
      headers: { Authorization: `Bearer ${token}` },
    },
  );
  const text = await response.text();
  return text; // ← remove the !response.ok check entirely
};
/**
 * Reject a quota request.
 * Endpoint: PATCH /ministry/statusReject/{requestId}
 */
export const rejectRequest = async (
  token: string,
  requestId: string,
): Promise<string> => {
  const response = await fetch(
    `${API_BASE_URL}/ministry/statusReject/${requestId}`,
    {
      method: "PATCH",
      headers: authHeaders(token),
    },
  );
  const text = await response.text();
  if (!response.ok) {
    throw new Error(text || `Rejection failed: ${response.status}`);
  }
  return text; // e.g. "Status set to REJECTED"
};
