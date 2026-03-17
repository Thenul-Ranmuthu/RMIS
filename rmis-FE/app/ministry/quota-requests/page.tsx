// RMIS/files/app/ministry/quota-requests/page.tsx

'use client';

import { useState, useEffect } from 'react';
//import { useRouter } from 'next/navigation';
import Link from 'next/link';
//import { useQuotaRequests, useQuotaStats } from '@/hooks/useQuotaRequests';
import QuotaTable from '@/components/quota-requests/QuotaTable';
import QuotaFiltersPanel from '@/components/quota-requests/QuotaFilters';
import QuotaPagination from '@/components/quota-requests/QuotaPagination';
//import UnauthorisedMessage from '@/components/quota-requests/UnauthorisedMessage';
import { QuotaFilters, QuotaPaginatedResponse } from '@/types/quota';
import { getQuotaRequests} from '@/services/quotaService';

const PAGE_SIZE = 20;

const EMPTY_FILTERS: QuotaFilters = {
    companyName: '',
    status: '',
    submissionDate: '',
};

export default function QuotaRequestsPage() {
    const [filters, setFilters] = useState<QuotaFilters>(EMPTY_FILTERS);
    const [page, setPage] = useState(1);
    const [data, setData] = useState<QuotaPaginatedResponse | null>(null);
    //const [stats, setStats] = useState<QuotaStats | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    // Fetch requests whenever filters or page changes
    useEffect(() => {
        console.log('useEffect triggered, filters:', filters);
        const load = async () => {
            setIsLoading(true);
            try {
                // const result = await getQuotaRequests(filters, page);
                const result = await getQuotaRequests(filters);
                console.log('result:', result); // ← add this to see the full response
                setData(result);
            } catch (err) {
                console.error(err);
            } finally {
                setIsLoading(false);
            }
        };
        load();
    }, [filters]);

    // Fetch stats once on mount
    // useEffect(() => {
    //     getQuotaStats().then(setStats).catch(console.error);
    // }, []);
    const rows = data?.data ?? [];

    const approvedTons = rows
        .filter(r => r.status === 'APPROVED')
        .reduce((sum, r) => sum + parseFloat(r.requested_quota.toString()), 0);

    const pendingCount = rows.filter(r => r.status === 'PENDING').length;

    const totalCount = rows.length;
    const complianceRate = totalCount > 0
        ? ((rows.filter(r => r.status !== 'REJECTED').length / totalCount) * 100).toFixed(1)
        : '—';

    // const handleFilterChange = (newFilters: QuotaFilters) => {
    //     setFilters(newFilters);
    //     setPage(1);
    // };

    // // ── 401 / 403 check ──────────────────────────────────────────────────────
    // const isUnauthorised =
    //     error &&
    //     'response' in (error as any) &&
    //     [401, 403].includes((error as any).response?.status);

    // // ── Render guards ─────────────────────────────────────────────────────────
    // if (isAuthorised === null) return null; // still checking token, render nothing
    // if (isUnauthorised) return <UnauthorisedMessage />;

    return (
        <div className="flex flex-col min-h-screen font-display bg-background-light dark:bg-background-dark text-slate-900 dark:text-slate-100">

            {/* ── Header ────────────────────────────────────────────────────── */}
            <header className="border-b border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 sticky top-0 z-50">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex items-center justify-between h-16">

                        {/* Logo */}
                        <div className="flex items-center gap-4">
                            <div className="flex items-center justify-center size-10 rounded-lg bg-primary/10 text-primary">
                                <span className="material-symbols-outlined text-3xl">eco</span>
                            </div>
                            <div className="flex flex-col">
                                <h2 className="text-slate-900 dark:text-white text-lg font-bold leading-tight">
                                    Ministry of Environment
                                </h2>
                                <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">
                                    Environmental Quota Division
                                </span>
                            </div>
                        </div>

                        {/* Nav */}
                        <nav className="hidden md:flex items-center gap-6">
                            <Link href="/ministry/dashboard" className="text-slate-600 dark:text-slate-400 hover:text-primary text-sm font-medium transition-colors">
                                Dashboard
                            </Link>
                            <Link href="/ministry/quota-requests" className="text-primary text-sm font-semibold border-b-2 border-primary pb-1">
                                Quota Requests
                            </Link>
                            <Link href="/ministry/reports" className="text-slate-600 dark:text-slate-400 hover:text-primary text-sm font-medium transition-colors">
                                Reports
                            </Link>
                            <Link href="/ministry/settings" className="text-slate-600 dark:text-slate-400 hover:text-primary text-sm font-medium transition-colors">
                                Settings
                            </Link>
                        </nav>

                        {/* Right side */}
                        <div className="flex items-center gap-4">
                            <button className="bg-primary text-white text-sm font-bold px-4 py-2 rounded-xl hover:bg-primary/90 transition-all flex items-center gap-2">
                                <span className="material-symbols-outlined text-sm">admin_panel_settings</span>
                                Admin Dashboard
                            </button>
                            <div className="size-10 rounded-full border-2 border-slate-200 dark:border-slate-700 bg-slate-100 dark:bg-slate-800 flex items-center justify-center overflow-hidden">
                                <span className="material-symbols-outlined text-slate-400">person</span>
                            </div>
                        </div>
                    </div>
                </div>
            </header>

            {/* ── Main ──────────────────────────────────────────────────────── */}
            <main className="flex-1 max-w-7xl mx-auto w-full px-4 sm:px-6 lg:px-8 py-8">
                <div className="flex flex-col gap-8">

                    {/* Page title */}
                    <div className="flex flex-col gap-1">
                        <h1 className="text-3xl font-extrabold tracking-tight text-slate-900 dark:text-white">
                            Quota Requests Management
                        </h1>
                        <p className="text-slate-500 dark:text-slate-400">
                            Review, approve, or reject industrial environmental quota applications.
                        </p>
                    </div>

                    {/* Filters
                    <QuotaFiltersPanel onFilterChange={handleFilterChange} /> */}

                    {/* Table + Pagination */}
                    <section className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden">
                        <QuotaTable
                            data={data?.data ?? []}
                            isLoading={isLoading}
                        />
                        {/* {data && (
                            <QuotaPagination
                                currentPage={page}
                                totalCount={data.total}
                                pageSize={PAGE_SIZE}
                                onPageChange={setPage}
                            />
                        )} */}
                    </section>

                    {/* Stats cards */}
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                        <div className="bg-emerald-50 dark:bg-emerald-900/10 border border-emerald-100 dark:border-emerald-900/30 p-4 rounded-xl flex items-center gap-4">
                            <div className="size-12 rounded-full bg-emerald-100 dark:bg-emerald-900/50 flex items-center justify-center text-emerald-600 dark:text-emerald-400">
                                <span className="material-symbols-outlined text-3xl">check_circle</span>
                            </div>
                            <div>
                                <p className="text-xs font-bold text-emerald-600 dark:text-emerald-500 uppercase tracking-wider">
                                    Approved Quotas
                                </p>
                                <h4 className="text-2xl font-black text-slate-900 dark:text-white">
                                    {approvedTons.toLocaleString()}{' '}
                                    <span className="text-sm font-normal text-slate-400">Tons</span>
                                </h4>
                            </div>
                        </div>

                        <div className="bg-amber-50 dark:bg-amber-900/10 border border-amber-100 dark:border-amber-900/30 p-4 rounded-xl flex items-center gap-4">
                            <div className="size-12 rounded-full bg-amber-100 dark:bg-amber-900/50 flex items-center justify-center text-amber-600 dark:text-amber-400">
                                <span className="material-symbols-outlined text-3xl">pending_actions</span>
                            </div>
                            <div>
                                <p className="text-xs font-bold text-amber-600 dark:text-amber-500 uppercase tracking-wider">
                                    Pending Review
                                </p>
                                <h4 className="text-2xl font-black text-slate-900 dark:text-white">
                                    {pendingCount}{' '}
                                    <span className="text-sm font-normal text-slate-400">Requests</span>
                                </h4>
                            </div>
                        </div>

                        <div className="bg-primary/5 border border-primary/20 p-4 rounded-xl flex items-center gap-4">
                            <div className="size-12 rounded-full bg-primary/10 flex items-center justify-center text-primary">
                                <span className="material-symbols-outlined text-3xl">monitoring</span>
                            </div>
                            <div>
                                <p className="text-xs font-bold text-primary uppercase tracking-wider">
                                    Total Compliance
                                </p>
                                <h4 className="text-2xl font-black text-slate-900 dark:text-white">
                                    {complianceRate}%
                                </h4>
                            </div>
                        </div>
                    </div>
                </div>
            </main>

            {/* ── Footer ────────────────────────────────────────────────────── */}
            <footer className="border-t border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 mt-12 py-8">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col md:flex-row justify-between items-center gap-4">
                    <div className="flex items-center gap-2 text-slate-500 dark:text-slate-400 text-sm">
                        <span className="material-symbols-outlined text-lg">policy</span>
                        © 2024 Ministry of Environment. All Rights Reserved.
                    </div>
                    <div className="flex gap-6">
                        <Link href="#" className="text-slate-500 hover:text-primary text-sm transition-colors">Privacy Policy</Link>
                        <Link href="#" className="text-slate-500 hover:text-primary text-sm transition-colors">Terms of Service</Link>
                        <Link href="#" className="text-slate-500 hover:text-primary text-sm transition-colors">Help Center</Link>
                    </div>
                </div>
            </footer>
        </div>
    );
}