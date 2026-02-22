// lib/auth/tokenUtils.ts
// Utilities for managing JWT tokens in the browser

export const TOKEN_KEY = "moe_access_token";
export const TOKEN_TYPE_KEY = "moe_token_type";

export function saveToken(accessToken: string, tokenType: string = "Bearer") {
  if (typeof window !== "undefined") {
    localStorage.setItem(TOKEN_KEY, accessToken);
    localStorage.setItem(TOKEN_TYPE_KEY, tokenType);
  }
}

export function getToken(): string | null {
  if (typeof window !== "undefined") {
    return localStorage.getItem(TOKEN_KEY);
  }
  return null;
}

export function getAuthHeader(): string | null {
  const token = getToken();
  const type = localStorage.getItem(TOKEN_TYPE_KEY) || "Bearer";
  if (!token) return null;
  return `${type} ${token}`;
}

export function clearToken() {
  if (typeof window !== "undefined") {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(TOKEN_TYPE_KEY);
  }
}

export function isLoggedIn(): boolean {
  return !!getToken();
}
