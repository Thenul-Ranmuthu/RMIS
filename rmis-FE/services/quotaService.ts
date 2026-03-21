// services/quotaService.ts

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:5050";

// ─── Interfaces ───────────────────────────────────────────────

export interface QuotaRequest {
  id?: number | string;
  companyEmail?: string;
  requestQuata: number;
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

// Normalise the backend response — the endpoint might return an array
// or an object with metadata + a list. We handle both gracefully.
const parseQuotaListResponse = (data: unknown): QuotaListResponse => {
  // Array response: backend returns QuotaRequest[]
  if (Array.isArray(data)) {
    return {
      summary: { currentAvailableQuota: null, remainingYearlyQuota: null },
      requests: data as QuotaRequest[],
    };
  }

  // Object response
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

// ─── API Functions ────────────────────────────────────────────

/**
 * Fetch all quota requests for the logged-in company.
 * Endpoint: GET /quotaHeader/geetQuotas
 */
export const getQuotas = async (token: string): Promise<QuotaListResponse> => {
  const response = await fetch(`${API_BASE_URL}/quotaHeader/getQuotas`, {
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

/**
 * Add a new quota request.
 * Endpoint: POST /quotaHeader/addQuota
 */
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
      // keep default message
    }
    throw new Error(message);
  }

  // return response.json();
  const text = await response.text();
  try {
    return JSON.parse(text);
  } catch {
    return { message: text }; // backend returned plain text like "Quota saved"
  }
};
