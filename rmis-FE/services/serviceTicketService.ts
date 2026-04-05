import { getToken } from "./authService";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:5050";

export interface ServiceTicketResponse {
  id: number;
  ticketNumber: string;
  customerName: string;
  customerEmail: string;
  customerType: string;
  technicianId: number;
  technicianName: string;
  technicianSpecialization: string;
  availabilityId: number;
  scheduledDate: string;
  scheduledStartTime: string;
  scheduledEndTime: string;
  serviceType: string;
  description: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

const authFetch = async (url: string, options: RequestInit = {}) => {
  const token = getToken();
  const response = await fetch(url, {
    ...options,
    headers: {
      ...options.headers,
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    const error = await response.json();
    throw error;
  }

  return response.json();
};

export const getMyTickets = (): Promise<ServiceTicketResponse[]> =>
  authFetch(`${API_BASE_URL}/api/service-tickets/user/my`);

export const getTicketById = (id: number): Promise<ServiceTicketResponse> =>
  authFetch(`${API_BASE_URL}/api/service-tickets/${id}`);
