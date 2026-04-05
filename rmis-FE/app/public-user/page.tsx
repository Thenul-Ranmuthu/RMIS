"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getMyTickets, cancelTicket, ServiceTicketResponse } from "../../services/serviceTicketService";
import { MyBookingsList } from "@/components/MyBookingsList";

export default function PublicUserDashboard() {
    const router = useRouter();
    const [user, setUser] = useState<{ email: string; role: string } | null>(null);
    const [tickets, setTickets] = useState<ServiceTicketResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchTickets = async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await getMyTickets();
            setTickets(data);
        } catch (err: any) {
            console.error("Error fetching tickets:", err);
            setError("Failed to load your booking history.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const userData = localStorage.getItem('user') || sessionStorage.getItem('user');
        if (!userData) {
            router.push('/');
            return;
        }
        setUser(JSON.parse(userData));
        fetchTickets();
    }, []);

    const handleSignOut = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        sessionStorage.removeItem('token');
        sessionStorage.removeItem('user');
        router.push('/');
    };

    const handleCancel = async (id: number) => {
        const confirmCancel = window.confirm("Are you sure you want to cancel this booking?");
        if (!confirmCancel) return;

        try {
            const reason = window.prompt("Reason for cancellation (optional):") || "Cancelled by user";
            await cancelTicket(id, reason);
            fetchTickets(); // Refresh list
        } catch (err: any) {
            console.error("Error cancelling ticket:", err);
            alert(err.error || "Failed to cancel booking.");
        }
    };

    return (
        <main className="min-h-screen bg-gray-50 flex flex-col md:flex-row font-['Public_Sans']">
            {/* Sidebar / Profile Section */}
            <aside className="w-full md:w-80 bg-white border-r border-gray-100 flex flex-col p-8 shrink-0">
                <div className="flex-1">
                    <div className="flex justify-center mb-6">
                        <div className="bg-emerald-100 rounded-3xl p-5 shadow-inner">
                            <span className="material-symbols-outlined text-4xl text-emerald-600">person</span>
                        </div>
                    </div>
                    
                    <div className="text-center mb-10">
                        <h1 className="text-2xl font-black text-gray-900 tracking-tight">Public Portal</h1>
                        <p className="text-sm font-bold text-gray-400 mt-1 uppercase tracking-widest">Customer Dashboard</p>
                    </div>

                    <div className="bg-gray-50 rounded-2xl p-5 mb-8 border border-gray-100">
                        <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">Authenticated Account</p>
                        <p className="text-emerald-700 font-bold truncate text-sm">{user?.email}</p>
                    </div>

                    <nav className="space-y-2">
                        <button
                            onClick={() => router.push('/public/directory')}
                            className="w-full flex items-center gap-3 bg-emerald-600 hover:bg-emerald-700 text-white px-5 py-4 rounded-xl font-bold transition shadow-lg shadow-emerald-600/20"
                        >
                            <span className="material-symbols-outlined">add_circle</span>
                            Book Service
                        </button>
                        <button
                            onClick={handleSignOut}
                            className="w-full flex items-center gap-3 bg-white hover:bg-red-50 text-red-500 border border-gray-100 px-5 py-4 rounded-xl font-bold transition"
                        >
                            <span className="material-symbols-outlined">logout</span>
                            Sign Out
                        </button>
                    </nav>
                </div>
                
                <div className="mt-8 pt-8 border-t border-gray-50 flex flex-col gap-2">
                   <p className="text-[10px] text-gray-400 font-bold uppercase tracking-widest">Version 2.1.0-STABLE</p>
                </div>
            </aside>

            {/* Main Content / Bookings List */}
            <section className="flex-1 p-6 md:p-12 max-w-5xl">
                <div className="flex items-center justify-between mb-8">
                    <div>
                        <h2 className="text-3xl font-black text-gray-900 tracking-tight">Your Service History</h2>
                        <p className="text-gray-500 mt-1">Manage and track your active service tickets</p>
                    </div>
                    {tickets.length > 0 && (
                        <button 
                            onClick={fetchTickets}
                            className="bg-white hover:bg-gray-50 text-gray-700 border border-gray-200 px-4 py-2 rounded-xl font-bold transition text-xs flex items-center gap-2"
                        >
                            <span className={`material-symbols-outlined text-base ${loading ? 'animate-spin' : ''}`}>sync</span>
                            Refresh
                        </button>
                    )}
                </div>

                {error && (
                    <div className="bg-red-50 border border-red-100 text-red-700 p-4 rounded-xl mb-8 flex items-center gap-3">
                        <span className="material-symbols-outlined">error</span>
                        <p className="text-sm font-bold">{error}</p>
                    </div>
                )}

                <div className="min-h-[500px]">
                    {loading && tickets.length === 0 ? (
                        <div className="py-20 flex flex-col items-center justify-center">
                            <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-emerald-600"></div>
                        </div>
                    ) : (
                        <MyBookingsList 
                            tickets={tickets} 
                            loading={loading} 
                            onViewDirectory={() => router.push('/public/directory')}
                            onViewDetails={(num) => alert(`Ticket ${num} active.`)}
                            onCancel={handleCancel}
                        />
                    )}
                </div>
            </section>
        </main>
    );
}