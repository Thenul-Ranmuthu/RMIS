// lib/api/companyAuth.ts

const BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL;

export interface CompanyRegisterPayload {
  name: string;
  email: string;
  companyid: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
}

/**
 * Step 1: Send verification email to the company email
 * GET /sendEmail/{email}
 * Returns "Success!!" string on success, or an error message.
 */
export async function sendVerificationEmail(email: string): Promise<string> {
  const res = await fetch(`${BASE_URL}/sendMail/${encodeURIComponent(email)}`, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
  });

  const text = await res.text();

  if (!res.ok) {
    throw new Error(text || "Failed to send verification email.");
  }

  return text; // "Success!!"
}

/**
 * Step 2: Submit the 6-digit OTP code along with registration data
 * POST /auth/company/register/{code}
 * Returns JWT token on success.
 */
export async function registerCompany(
  code: string,
  payload: CompanyRegisterPayload,
): Promise<AuthResponse> {
  const res = await fetch(
    `${BASE_URL}/auth/company/register/${encodeURIComponent(code)}`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    },
  );

  const data = await res.json();

  if (!res.ok) {
    throw new Error(data?.message || data || "Registration failed.");
  }

  return data as AuthResponse;
}

/**
 * Step 3: Login for already registered companies
 * POST /auth/company/login
 * Returns JWT token on success.
 */
export async function loginCompany(
  email: string,
  password: string,
): Promise<AuthResponse> {
  const res = await fetch(`${BASE_URL}/auth/company/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  });

  const data = await res.json();

  if (!res.ok) {
    throw new Error(data?.message || data || "Login failed.");
  }

  return data as AuthResponse;
}
