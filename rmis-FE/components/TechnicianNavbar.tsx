"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import {
  CalendarRange,
  ClipboardList,
  LayoutDashboard,
  LogOut,
  Menu,
  UserRound,
  X,
} from "lucide-react";
import { getToken } from "@/services/authService";

const NAV_LINKS = [
  {
    href: "/technician/dashboard",
    label: "Dashboard",
    icon: LayoutDashboard,
  },
  {
    href: "/technician/bookings",
    label: "Bookings",
    icon: ClipboardList,
  },
  {
    href: "/technician/availability",
    label: "Availability",
    icon: CalendarRange,
  },
  {
    href: "/technician/profile",
    label: "Profile",
    icon: UserRound,
  },
];

export default function TechnicianNavbar() {
  const pathname = usePathname();
  const router = useRouter();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 8);
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  // Close mobile menu on route change
  useEffect(() => {
    setMobileOpen(false);
  }, [pathname]);

  const handleSignOut = () => {
    localStorage.removeItem("accessToken");
    router.push("/login");
  };

  const isActive = (href: string) => pathname === href || pathname.startsWith(href + "/");

  return (
    <>
      {/* ── Main header ── */}
      <header
        className={[
          "sticky top-0 z-40 transition-all duration-300",
          scrolled
            ? "border-b border-slate-200/80 bg-white/95 shadow-sm shadow-slate-900/5 backdrop-blur-xl"
            : "border-b border-slate-200/50 bg-white/80 backdrop-blur-lg",
        ].join(" ")}
      >
        <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-0 sm:px-6 lg:px-8">

          {/* ── Brand ── */}
          <Link
            href="/technician/dashboard"
            className="flex items-center gap-3 py-4 group"
          >
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-emerald-700 text-white shadow-md shadow-emerald-900/20 ring-1 ring-emerald-800 transition group-hover:bg-emerald-600">
              <ClipboardList className="h-5 w-5" />
            </div>
            <div className="leading-none">
              <div className="text-[13px] font-black tracking-[0.22em] text-slate-900 group-hover:text-emerald-700 transition">
                RMIS
              </div>
              <div className="text-[10px] font-semibold tracking-[0.14em] text-slate-400 uppercase">
                Technician Portal
              </div>
            </div>
          </Link>

          {/* ── Desktop nav links ── */}
          <nav className="hidden md:flex items-center">
            {NAV_LINKS.map(({ href, label, icon: Icon }) => {
              const active = isActive(href);
              return (
                <Link
                  key={href}
                  href={href}
                  className={[
                    "relative flex items-center gap-2 px-4 py-5 text-[13px] font-semibold transition-colors duration-150",
                    active
                      ? "text-emerald-700"
                      : "text-slate-500 hover:text-slate-900",
                  ].join(" ")}
                >
                  <Icon
                    className={[
                      "h-4 w-4 transition-colors",
                      active ? "text-emerald-700" : "text-slate-400",
                    ].join(" ")}
                  />
                  {label}
                  {/* Active underline indicator */}
                  {active && (
                    <span className="absolute bottom-0 left-4 right-4 h-[2.5px] rounded-t-full bg-emerald-600" />
                  )}
                </Link>
              );
            })}
          </nav>

          {/* ── Desktop right: sign out ── */}
          <div className="hidden md:flex items-center gap-2">
            <button
              onClick={handleSignOut}
              className="inline-flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-4 py-2 text-[13px] font-semibold text-slate-600 shadow-sm transition hover:border-rose-200 hover:bg-rose-50 hover:text-rose-600"
            >
              <LogOut className="h-3.5 w-3.5" />
              Sign Out
            </button>
          </div>

          {/* ── Mobile: hamburger ── */}
          <button
            className="flex md:hidden items-center justify-center h-10 w-10 rounded-xl border border-slate-200 bg-white text-slate-600 transition hover:bg-slate-50"
            onClick={() => setMobileOpen((v) => !v)}
            aria-label="Toggle menu"
          >
            {mobileOpen ? <X className="h-4 w-4" /> : <Menu className="h-4 w-4" />}
          </button>
        </div>
      </header>

      {/* ── Mobile drawer ── */}
      {/* Backdrop */}
      {mobileOpen && (
        <div
          className="fixed inset-0 z-30 bg-slate-950/30 backdrop-blur-sm md:hidden"
          onClick={() => setMobileOpen(false)}
        />
      )}

      {/* Drawer panel */}
      <div
        className={[
          "fixed top-0 right-0 z-50 flex h-full w-72 flex-col bg-white shadow-2xl transition-transform duration-300 ease-in-out md:hidden",
          mobileOpen ? "translate-x-0" : "translate-x-full",
        ].join(" ")}
      >
        {/* Drawer header */}
        <div className="flex items-center justify-between border-b border-slate-100 px-5 py-4">
          <div className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-emerald-700 text-white">
              <ClipboardList className="h-4 w-4" />
            </div>
            <div className="leading-none">
              <div className="text-[12px] font-black tracking-[0.2em] text-slate-900">RMIS</div>
              <div className="text-[10px] font-semibold text-slate-400 uppercase tracking-wide">Technician Portal</div>
            </div>
          </div>
          <button
            onClick={() => setMobileOpen(false)}
            className="flex h-8 w-8 items-center justify-center rounded-lg border border-slate-200 text-slate-400 transition hover:text-slate-700"
          >
            <X className="h-4 w-4" />
          </button>
        </div>

        {/* Drawer links */}
        <nav className="flex-1 overflow-y-auto px-3 py-4 space-y-1">
          {NAV_LINKS.map(({ href, label, icon: Icon }) => {
            const active = isActive(href);
            return (
              <Link
                key={href}
                href={href}
                className={[
                  "flex items-center gap-3 rounded-xl px-4 py-3 text-[13px] font-semibold transition",
                  active
                    ? "bg-emerald-50 text-emerald-700 ring-1 ring-emerald-100"
                    : "text-slate-600 hover:bg-slate-50 hover:text-slate-900",
                ].join(" ")}
              >
                <div
                  className={[
                    "flex h-8 w-8 items-center justify-center rounded-lg",
                    active ? "bg-emerald-100 text-emerald-700" : "bg-slate-100 text-slate-500",
                  ].join(" ")}
                >
                  <Icon className="h-4 w-4" />
                </div>
                {label}
              </Link>
            );
          })}
        </nav>

        {/* Drawer footer */}
        <div className="border-t border-slate-100 px-4 py-4">
          <button
            onClick={handleSignOut}
            className="flex w-full items-center gap-3 rounded-xl border border-rose-100 bg-rose-50 px-4 py-3 text-[13px] font-semibold text-rose-600 transition hover:bg-rose-100"
          >
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-rose-100">
              <LogOut className="h-4 w-4" />
            </div>
            Sign Out
          </button>
        </div>
      </div>
    </>
  );
}