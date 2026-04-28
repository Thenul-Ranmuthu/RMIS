"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import Image from "next/image";
import {
  AlertCircle,
  ArrowRight,
  CalendarDays,
  CalendarRange,
  CheckCircle2,
  ClipboardList,
  Clock3,
  Settings2,
  UserRound,
} from "lucide-react";
import { getToken, getRole } from "@/services/authService";

const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:5050";

interface Booking {
  id: number;
  ticketNumber?: string;
  serviceType: string;
  customerName: string;
  scheduledDate: string;
  scheduledStartTime?: string;
  scheduledEndTime?: string;
  status: string;
}

interface Slot {
  id: number;
  date: string;
  startTime: string;
  endTime: string;
  status?: string;
}

type LoadState = "idle" | "loading" | "ready" | "error";

function authHeaders() {
  const token = getToken();
  return {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
}

function formatDate(dateStr: string) {
  const d = new Date(`${dateStr}T00:00:00`);
  return d.toLocaleDateString("en-US", {
    weekday: "short",
    year: "numeric",
    month: "short",
    day: "numeric",
  });
}

function formatTime(timeStr?: string) {
  if (!timeStr) return "—";
  const [h, m] = timeStr.split(":");
  const hour = Number.parseInt(h, 10);
  const ampm = hour >= 12 ? "PM" : "AM";
  const h12 = hour % 12 || 12;
  return `${h12}:${m} ${ampm}`;
}

function isFutureOrToday(dateStr: string) {
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const d = new Date(`${dateStr}T00:00:00`);
  d.setHours(0, 0, 0, 0);

  return d >= today;
}

function statusLabel(status: string) {
  switch (status.toUpperCase()) {
    case "PENDING":
      return "Pending";
    case "ACCEPTED":
      return "Accepted";
    case "COMPLETED":
      return "Completed";
    case "CANCELLED":
      return "Cancelled";
    case "BOOKED":
      return "Booked";
    default:
      return status;
  }
}

function statusStyles(status: string) {
  switch (status.toUpperCase()) {
    case "PENDING":
      return "border-amber-200 bg-amber-50 text-amber-700";
    case "ACCEPTED":
    case "BOOKED":
      return "border-emerald-200 bg-emerald-50 text-emerald-700";
    case "COMPLETED":
      return "border-sky-200 bg-sky-50 text-sky-700";
    case "CANCELLED":
      return "border-rose-200 bg-rose-50 text-rose-700";
    default:
      return "border-slate-200 bg-slate-50 text-slate-700";
  }
}

export default function TechnicianDashboardPage() {
  const router = useRouter();

  const [bookings, setBookings] = useState<Booking[]>([]);
  const [slots, setSlots] = useState<Slot[]>([]);
  const [bookingsState, setBookingsState] = useState<LoadState>("loading");
  const [slotsState, setSlotsState] = useState<LoadState>("loading");
  const [pageError, setPageError] = useState("");

  useEffect(() => {
    const token = getToken();
    const role = getRole();

    if (!token) {
      router.replace("/login");
      return;
    }

    if (role !== "ROLE_TECHNICIAN" && role !== "TECHNICIAN") {
      router.replace("/");
      return;
    }

    fetchData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const fetchData = async () => {
    setPageError("");
    setBookingsState("loading");
    setSlotsState("loading");

    try {
      const [bookingRes, slotRes] = await Promise.allSettled([
        fetch(`${API_BASE}/technician/bookings`, {
          headers: authHeaders(),
        }),
        fetch(`${API_BASE}/technician/availability`, {
          headers: authHeaders(),
        }),
      ]);

      if (bookingRes.status === "fulfilled") {
        if (!bookingRes.value.ok) {
          throw new Error("Failed to load bookings.");
        }
        const bookingData = await bookingRes.value.json();
        setBookings(Array.isArray(bookingData) ? bookingData : []);
        setBookingsState("ready");
      } else {
        setBookings([]);
        setBookingsState("error");
      }

      if (slotRes.status === "fulfilled") {
        if (!slotRes.value.ok) {
          throw new Error("Failed to load availability.");
        }
        const slotData = await slotRes.value.json();
        setSlots(Array.isArray(slotData) ? slotData : []);
        setSlotsState("ready");
      } else {
        setSlots([]);
        setSlotsState("error");
      }
    } catch (error) {
      setPageError(
        error instanceof Error
          ? error.message
          : "Unable to load dashboard data.",
      );
      setBookings([]);
      setSlots([]);
      setBookingsState("error");
      setSlotsState("error");
    }
  };

  const upcomingBookings = useMemo(() => {
    return [...bookings]
      .filter((b) => {
        const status = (b.status || "").toUpperCase();
        return (
          isFutureOrToday(b.scheduledDate) &&
          (status === "PENDING" || status === "ACCEPTED" || status === "BOOKED")
        );
      })
      .sort((a, b) => {
        const d1 = new Date(
          `${a.scheduledDate}T${a.scheduledStartTime || "00:00:00"}`,
        ).getTime();
        const d2 = new Date(
          `${b.scheduledDate}T${b.scheduledStartTime || "00:00:00"}`,
        ).getTime();
        return d1 - d2;
      });
  }, [bookings]);

  const openSlots = useMemo(() => {
    return [...slots]
      .filter((s) => {
        const status = (s.status || "AVAILABLE").toUpperCase();
        return (
          isFutureOrToday(s.date) &&
          (status === "AVAILABLE" || status === "OPEN")
        );
      })
      .sort((a, b) => {
        const d1 = new Date(`${a.date}T${a.startTime}`).getTime();
        const d2 = new Date(`${b.date}T${b.startTime}`).getTime();
        return d1 - d2;
      });
  }, [slots]);

  const pendingCount = bookings.filter(
    (b) => (b.status || "").toUpperCase() === "PENDING",
  ).length;

  const acceptedCount = bookings.filter((b) => {
    const status = (b.status || "").toUpperCase();
    return status === "ACCEPTED" || status === "BOOKED";
  }).length;

  const completedCount = bookings.filter(
    (b) => (b.status || "").toUpperCase() === "COMPLETED",
  ).length;

  const totalOpenSlots = openSlots.length;
  const isLoading = bookingsState === "loading" || slotsState === "loading";

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">
      <main className="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8 lg:py-8">
        <section className="relative overflow-hidden rounded-[2rem] border border-slate-200 shadow-[0_25px_70px_rgba(15,23,42,0.16)]">
          <div className="absolute inset-0">
            <Image
              src="/TechDashboardHero.jpeg"
              alt="Technician dashboard hero"
              fill
              priority
              className="object-cover"
            />
            <div className="absolute inset-0 bg-black/55" />
          </div>

          <div className="relative grid gap-8 p-6 sm:p-8 lg:grid-cols-[1.25fr_0.95fr] lg:p-10">
            <div className="text-white">
              <div className="mb-4 inline-flex items-center gap-2 rounded-full border border-white/15 bg-white/10 px-4 py-2 text-xs font-semibold uppercase tracking-[0.18em] text-white/80 backdrop-blur">
                <span className="h-2 w-2 rounded-full bg-emerald-300" />
                Technician Dashboard
              </div>

              <h1 className="text-3xl font-black tracking-tight sm:text-4xl lg:text-5xl">
                Manage bookings and availability in one place.
              </h1>

              <p className="mt-4 max-w-2xl text-sm leading-7 text-white/75 sm:text-base">
                Review assigned bookings, maintain open time slots, and keep
                your schedule aligned with public service requirements.
              </p>
            </div>

            <div className="grid gap-4 rounded-[1.75rem] border border-white/10 bg-white/10 p-4 backdrop-blur-md">
              <div className="rounded-2xl border border-white/10 bg-white/10 p-5 text-white">
                <div className="text-xs font-semibold uppercase tracking-[0.18em] text-white/60">
                  Current Overview
                </div>
                <div className="mt-3 grid grid-cols-2 gap-3">
                  <SummaryMetric label="Pending" value={pendingCount} />
                  <SummaryMetric label="Accepted" value={acceptedCount} />
                  <SummaryMetric label="Completed" value={completedCount} />
                  <SummaryMetric label="Open Slots" value={totalOpenSlots} />
                </div>
              </div>

              <div className="rounded-2xl border border-white/10 bg-slate-950/20 p-5 text-sm leading-6 text-slate-100">
                Keep upcoming work visible, use future availability only, and
                avoid maintaining inactive or completed entries here.
              </div>
            </div>
          </div>
        </section>

        {pageError && (
          <div className="mt-6 flex items-start gap-3 rounded-2xl border border-rose-200 bg-rose-50 px-4 py-4 text-sm text-rose-700">
            <AlertCircle className="mt-0.5 h-4 w-4 shrink-0" />
            <div>{pageError}</div>
          </div>
        )}

        <section className="mt-6 grid gap-4 lg:grid-cols-4">
          <QuickLinkCard
            href="/technician/availability"
            title="Manage Availability"
            description="Add, edit, or remove future slots."
            icon={<CalendarRange className="h-5 w-5" />}
          />
          <QuickLinkCard
            href="/technician/bookings"
            title="View Bookings"
            description="Review assigned service requests."
            icon={<ClipboardList className="h-5 w-5" />}
          />
          <QuickLinkCard
            href="/technician/profile"
            title="Profile"
            description="Update personal details and credentials."
            icon={<UserRound className="h-5 w-5" />}
          />
          <QuickLinkCard
            href="#"
            title="Settings"
            description="Notification and account preferences."
            icon={<Settings2 className="h-5 w-5" />}
          />
        </section>

        <section className="mt-6 grid gap-6 lg:grid-cols-2">
          <Panel
            title="Upcoming Bookings"
            subtitle="Future bookings with pending or accepted status."
            actionHref="/technician/bookings"
            actionLabel="View all"
          >
            {isLoading ? (
              <DashboardSkeleton rows={4} />
            ) : upcomingBookings.length === 0 ? (
              <EmptyState
                title="No upcoming bookings"
                description="There are currently no future bookings assigned to this technician."
              />
            ) : (
              <div className="space-y-3">
                {upcomingBookings.slice(0, 4).map((booking) => (
                  <div
                    key={booking.id}
                    className="flex flex-col gap-4 rounded-2xl border border-slate-200 bg-white p-4 transition hover:border-emerald-200 hover:shadow-sm sm:flex-row sm:items-center sm:justify-between"
                  >
                    <div className="min-w-0">
                      <div className="flex items-center gap-2">
                        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-emerald-50 text-emerald-700 ring-1 ring-emerald-100">
                          <CalendarDays className="h-4 w-4" />
                        </div>
                        <div className="min-w-0">
                          <div className="truncate text-sm font-extrabold text-slate-900">
                            {booking.serviceType}
                          </div>
                          <div className="mt-0.5 text-sm text-slate-500">
                            {booking.customerName}
                          </div>
                        </div>
                      </div>

                      <div className="mt-3 flex flex-wrap gap-3 text-xs text-slate-500">
                        <span className="inline-flex items-center gap-1.5 rounded-full border border-slate-200 bg-slate-50 px-3 py-1">
                          <CalendarDays className="h-3.5 w-3.5" />
                          {formatDate(booking.scheduledDate)}
                        </span>
                        <span className="inline-flex items-center gap-1.5 rounded-full border border-slate-200 bg-slate-50 px-3 py-1">
                          <Clock3 className="h-3.5 w-3.5" />
                          {formatTime(booking.scheduledStartTime)} –{" "}
                          {formatTime(booking.scheduledEndTime)}
                        </span>
                        {booking.ticketNumber ? (
                          <span className="inline-flex items-center gap-1.5 rounded-full border border-slate-200 bg-slate-50 px-3 py-1">
                            Ticket {booking.ticketNumber}
                          </span>
                        ) : null}
                      </div>
                    </div>

                    <div className="flex shrink-0 items-center gap-2">
                      <span
                        className={`inline-flex items-center rounded-full border px-3 py-1 text-xs font-bold uppercase tracking-[0.12em] ${statusStyles(
                          booking.status,
                        )}`}
                      >
                        {statusLabel(booking.status)}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </Panel>

          <Panel
            title="Open Availability"
            subtitle="Future availability slots currently shown to the booking system."
            actionHref="/technician/availability"
            actionLabel="Manage"
          >
            {isLoading ? (
              <DashboardSkeleton rows={3} />
            ) : openSlots.length === 0 ? (
              <EmptyState
                title="No open slots"
                description="There are no available future time slots at the moment."
              />
            ) : (
              <div className="space-y-3">
                {openSlots.slice(0, 3).map((slot) => (
                  <div
                    key={slot.id}
                    className="flex flex-col gap-4 rounded-2xl border border-emerald-100 bg-emerald-50/50 p-4 transition hover:border-emerald-200 hover:shadow-sm sm:flex-row sm:items-center sm:justify-between"
                  >
                    <div className="min-w-0">
                      <div className="flex items-center gap-2">
                        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-white text-emerald-700 ring-1 ring-emerald-100">
                          <CalendarRange className="h-4 w-4" />
                        </div>
                        <div className="min-w-0">
                          <div className="truncate text-sm font-extrabold text-slate-900">
                            {formatDate(slot.date)}
                          </div>
                          <div className="mt-0.5 text-sm text-slate-500">
                            {formatTime(slot.startTime)} –{" "}
                            {formatTime(slot.endTime)}
                          </div>
                        </div>
                      </div>
                    </div>

                    <div className="flex shrink-0 items-center gap-2">
                      <span className="inline-flex items-center rounded-full border border-emerald-200 bg-white px-3 py-1 text-xs font-bold uppercase tracking-[0.12em] text-emerald-700">
                        Open
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </Panel>
        </section>

        <footer className="mt-8 flex flex-col gap-3 border-t border-slate-200 px-1 pt-6 text-sm text-slate-500 sm:flex-row sm:items-center sm:justify-between">
          <p>
            © {new Date().getFullYear()} Ministry of Environment. All Rights
            Reserved.
          </p>
          <div className="flex gap-5">
            <Link href="#" className="transition hover:text-emerald-700">
              Privacy Policy
            </Link>
            <Link href="#" className="transition hover:text-emerald-700">
              Help Center
            </Link>
          </div>
        </footer>
      </main>
    </div>
  );
}

function SummaryMetric({
  label,
  value,
}: {
  label: string;
  value: number;
}) {
  return (
    <div className="rounded-2xl border border-white/10 bg-white/10 px-4 py-4">
      <div className="text-2xl font-black text-white">{value}</div>
      <div className="mt-1 text-xs font-semibold uppercase tracking-[0.16em] text-white/60">
        {label}
      </div>
    </div>
  );
}

function QuickLinkCard({
  href,
  title,
  description,
  icon,
}: {
  href: string;
  title: string;
  description: string;
  icon: React.ReactNode;
}) {
  const card = (
    <div className="group h-full rounded-[1.5rem] border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:border-emerald-200 hover:shadow-md">
      <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-emerald-50 ring-1 ring-emerald-100 text-emerald-700">
        {icon}
      </div>
      <div className="mt-4 text-base font-extrabold text-slate-900">
        {title}
      </div>
      <div className="mt-1 text-sm leading-6 text-slate-500">{description}</div>
    </div>
  );

  if (href === "#") return card;

  return (
    <Link href={href} className="block h-full">
      {card}
    </Link>
  );
}

function Panel({
  title,
  subtitle,
  actionHref,
  actionLabel,
  children,
}: {
  title: string;
  subtitle: string;
  actionHref: string;
  actionLabel: string;
  children: React.ReactNode;
}) {
  return (
    <section className="rounded-[2rem] border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
      <div className="mb-5 flex items-start justify-between gap-4">
        <div>
          <h2 className="text-xl font-black text-slate-900">{title}</h2>
          <p className="mt-1 text-sm leading-6 text-slate-500">{subtitle}</p>
        </div>
        <Link
          href={actionHref}
          className="inline-flex items-center gap-1.5 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-2 text-sm font-semibold text-emerald-700 transition hover:bg-emerald-100"
        >
          {actionLabel}
          <ArrowRight className="h-4 w-4" />
        </Link>
      </div>
      {children}
    </section>
  );
}

function DashboardSkeleton({ rows }: { rows: number }) {
  return (
    <div className="space-y-3">
      {Array.from({ length: rows }).map((_, i) => (
        <div
          key={i}
          className="rounded-2xl border border-slate-200 bg-slate-50 p-4"
        >
          <div className="flex items-center gap-3">
            <div className="h-10 w-10 animate-pulse rounded-xl bg-slate-200" />
            <div className="flex-1">
              <div className="h-4 w-44 animate-pulse rounded bg-slate-200" />
              <div className="mt-2 h-3 w-32 animate-pulse rounded bg-slate-200" />
            </div>
            <div className="h-7 w-24 animate-pulse rounded-full bg-slate-200" />
          </div>
        </div>
      ))}
    </div>
  );
}

function EmptyState({
  title,
  description,
}: {
  title: string;
  description: string;
}) {
  return (
    <div className="rounded-2xl border border-dashed border-slate-200 bg-slate-50 px-6 py-12 text-center">
      <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-white text-slate-400 ring-1 ring-slate-200">
        <CheckCircle2 className="h-5 w-5" />
      </div>
      <h3 className="mt-4 text-base font-bold text-slate-900">{title}</h3>
      <p className="mx-auto mt-2 max-w-md text-sm leading-6 text-slate-500">
        {description}
      </p>
    </div>
  );
}