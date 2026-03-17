// RMIS/files/components/quota-requests/QuotaTable.tsx

import { QuotaRequest, QuotaStatus } from '@/types/quota';

interface QuotaTableProps {
    data: QuotaRequest[];
    isLoading: boolean;
}

const StatusBadge = ({ status }: { status: QuotaStatus }) => {
    const styles: Record<QuotaStatus, { badge: string; dot: string; label: string }> = {
        PENDING: {
            badge: 'bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-400',
            dot: 'bg-amber-500',
            label: 'Pending',
        },
        APPROVED: {
            badge: 'bg-emerald-100 text-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-400',
            dot: 'bg-emerald-500',
            label: 'Approved',
        },
        REJECTED: {
            badge: 'bg-rose-100 text-rose-800 dark:bg-rose-900/30 dark:text-rose-400',
            dot: 'bg-rose-500',
            label: 'Rejected',
        },
    };

    const s = styles[status];
    return (
        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${s.badge}`}>
            <span className={`size-1.5 rounded-full ${s.dot} mr-1.5`} />
            {s.label}
        </span>
    );
};

const ActionButton = ({ status }: { status: QuotaStatus }) => {
    const label = status === 'PENDING' ? 'Review' : status === 'APPROVED' ? 'Details' : 'View Log';
    return (
        <button className="text-primary hover:text-primary/80 font-semibold text-sm transition-colors">
            {label}
        </button>
    );
};

// Loading skeleton rows
const SkeletonRow = () => (
    <tr>
        {Array.from({ length: 6 }).map((_, i) => (
            <td key={i} className="px-6 py-4">
                <div className="h-4 bg-slate-200 dark:bg-slate-700 rounded animate-pulse w-3/4" />
            </td>
        ))}
    </tr>
);

export default function QuotaTable({ data, isLoading }: QuotaTableProps) {
    return (
        <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
                <thead>
                    <tr className="bg-slate-50 dark:bg-slate-800/50 border-b border-slate-200 dark:border-slate-800">
                        {['Request ID', 'Company Name', 'Requested Quota', 'Submission Date', 'Status', 'Actions'].map((col) => (
                            <th
                                key={col}
                                className={`px-6 py-4 text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider ${col === 'Actions' ? 'text-right' : ''}`}
                            >
                                {col}
                            </th>
                        ))}
                    </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 dark:divide-slate-800">

                    {/* Loading state */}
                    {isLoading && Array.from({ length: 5 }).map((_, i) => <SkeletonRow key={i} />)}

                    {/* Empty state */}
                    {!isLoading && data.length === 0 && (
                        <tr>
                            <td colSpan={6} className="px-6 py-16 text-center">
                                <div className="flex flex-col items-center gap-3 text-slate-400">
                                    <span className="material-symbols-outlined text-5xl">inbox</span>
                                    <p className="font-semibold text-slate-500 dark:text-slate-400">No quota requests found</p>
                                    <p className="text-sm">Try adjusting your filters or check back later.</p>
                                </div>
                            </td>
                        </tr>
                    )}

                    {/* Data rows */}
                    {!isLoading && data.map((row) => (
                        <tr
                            key={row.request_id}
                            className="hover:bg-slate-50/50 dark:hover:bg-slate-800/30 transition-colors"
                        >
                            <td className="px-6 py-4 text-sm font-medium text-slate-900 dark:text-slate-100">
                                #{row.request_id}
                            </td>
                            <td className="px-6 py-4 text-sm text-slate-600 dark:text-slate-400">
                                {row.company_name}
                            </td>
                            <td className="px-6 py-4 text-sm text-slate-600 dark:text-slate-400">
                                {row.requested_quota}
                            </td>
                            <td className="px-6 py-4 text-sm text-slate-600 dark:text-slate-400">
                                {new Date(row.submission_date).toLocaleDateString('en-US', {
                                    month: 'short', day: 'numeric', year: 'numeric',
                                })}
                            </td>
                            <td className="px-6 py-4">
                                <StatusBadge status={row.status} />
                            </td>
                            <td className="px-6 py-4 text-right">
                                <ActionButton status={row.status} />
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}