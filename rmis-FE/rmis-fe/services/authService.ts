// RMIS/files/services/authService.ts

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:5051';

// ─── Interfaces ───────────────────────────────────────────────

export interface PublicUserRegisterPayload {
    firstName: string;
    lastName: string;
    email: string;
    phone: string;
    password: string;
    confirmPassword: string;
}

export interface TechnicianRegisterPayload {
    fullName: string;
    email: string;
    province: string;
    district: string;
    password: string;
    confirmPassword: string;
}

export interface CompanyRegisterPayload {
    companyName: string;
    registrationNumber: string;
    email: string;
    password: string;
    confirmPassword: string;
}

export interface RegisterResponse {
    token: string;
    user: {
        id: string;
        email: string;
        role: string;
    };
}

// ─── Helper ───────────────────────────────────────────────────

const post = async (url: string, payload: object): Promise<RegisterResponse> => {
    const response = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
    });

    if (!response.ok) {
        const error = await response.json();
        throw error;
    }

    return response.json();
};

// ─── Register Functions ───────────────────────────────────────

export const registerPublicUser = (payload: PublicUserRegisterPayload) =>
    post(`${API_BASE_URL}/auth/user/register`, payload);

export const registerTechnician = (payload: TechnicianRegisterPayload) =>
    post(`${API_BASE_URL}/auth/technician/register`, payload);

export const registerCompany = (payload: CompanyRegisterPayload) =>
    post(`${API_BASE_URL}/auth/company/register`, payload);

// ─── Login Functions ──────────────────────────────────────────

export const loginPublicUser = (email: string, password: string) =>
    post(`${API_BASE_URL}/auth/user/login`, { email, password });

export const loginTechnician = (email: string, password: string) =>
    post(`${API_BASE_URL}/auth/technician/login`, { email, password });

export const loginCompany = (email: string, password: string) =>
    post(`${API_BASE_URL}/auth/company/login`, { email, password });