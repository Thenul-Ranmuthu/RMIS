"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { raiseTicketAsUser, raiseTicketAsCompany } from "@/services/serviceTicketService";

const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:5055";

interface AvailabilitySlot {
  id: number;
  date: string;
  startTime: string;
  endTime: string;
  status: string;
  technicianId?: number;
  technicianName?: string;
}

interface TechnicianProfile {
  id: number;
  firstName: string;
  lastName: string;
  specialization?: string;
  skillLevel?: string;
  district?: string;
  yearsOfExperience?: number;
  phoneNumber?: string;
}

const SERVICE_TYPES = [
  "AC Installation",
  "AC Repair",
  "AC Gas Refill",
  "AC Servicing / Cleaning",
  "AC Inspection",
  "Gas Leak Detection",
  "Gas Pipe Installation",
  "Gas Appliance Repair",
  "General HVAC Service",
  "Other",
];

function groupByDate(slots: AvailabilitySlot[]): Record<string, AvailabilitySlot[]> {
  return slots.reduce<Record<string, AvailabilitySlot[]>>((acc, s) => {
    (acc[s.date] ||= []).push(s);
    return acc;
  }, {});
}

function formatDate(d: string) {
  return new Date(`${d}T00:00:00`).toLocaleDateString("en-US", {
    weekday: "long",
    day: "numeric",
    month: "long",
    year: "numeric",
  });
}

function formatTime(t: string) {
  return t.slice(0, 5);
}

export default function BookTechnicianPage() {
  const { technicianId } = useParams<{ technicianId: string }>();
  const router = useRouter();

  // Auth
  const [role, setRole] = useState<"CUSTOMER" | "COMPANY" | null>(null);
  const [authChecked, setAuthChecked] = useState(false);

  // Technician + slots
  const [technician, setTechnician] = useState<TechnicianProfile | null>(null);
  const [slots, setSlots] = useState<AvailabilitySlot[]>([]);
  const [loadingSlots, setLoadingSlots] = useState(true);
  const [slotsError, setSlotsError] = useState("");

  // Form state
  const [selectedSlot, setSelectedSlot] = useState<AvailabilitySlot | null>(null);
  const [serviceType, setServiceType] = useState("");
  const [description, setDescription] = useState("");

  // UI state
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");
  const [successTicket, setSuccessTicket] = useState<{ ticketNumber: string; id: number } | null>(null);

  // ── Auth check ────────────────────────────────────────────────
  useEffect(() => {
    const raw = localStorage.getItem("user") || sessionStorage.getItem("user");
    const token = localStorage.getItem("token") || sessionStorage.getItem("token")
      || localStorage.getItem("accessToken") || sessionStorage.getItem("accessToken");

    if (!raw || !token) {
      router.push("/");
      return;
    }
    try {
      const parsed = JSON.parse(raw);
      const r = (parsed.role || "").toUpperCase();
      if (r === "CUSTOMER" || r === "ROLE_CUSTOMER" || r === "PUBLIC USER") {
        setRole("CUSTOMER");
      } else if (r === "COMPANY" || r === "ROLE_COMPANY") {
        setRole("COMPANY");
      } else {
        console.warn("User has insufficient permissions or invalid role:", r);
        router.push("/unauthorised");
        return;
      }
    } catch {
      router.push("/");
      return;
    }
    setAuthChecked(true);
  }, [router]);

  // ── Fetch technician + available slots ────────────────────────
  useEffect(() => {
    if (!authChecked || !technicianId) return;

    const fetchData = async () => {
      setLoadingSlots(true);
      setSlotsError("");
      try {
        const [techRes, slotsRes] = await Promise.all([
          fetch(`${API_BASE}/public/technician/${technicianId}`),
          fetch(`${API_BASE}/public/technicians/${technicianId}/availability`),
        ]);
        if (!techRes.ok) throw new Error("Technician not found");
        const techData = await techRes.json();
        setTechnician(techData);

        if (slotsRes.ok) {
          const slotsData = await slotsRes.json();
          setSlots(Array.isArray(slotsData) ? slotsData.filter((s: AvailabilitySlot) => s.status === "AVAILABLE") : []);
        }
      } catch (e: unknown) {
        setSlotsError((e as Error).message || "Failed to load availability.");
      } finally {
        setLoadingSlots(false);
      }
    };

    fetchData();
  }, [authChecked, technicianId]);

  // ── Submit ────────────────────────────────────────────────────
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedSlot) { setSubmitError("Please select a time slot."); return; }
    if (!serviceType) { setSubmitError("Please select a service type."); return; }

    setSubmitting(true);
    setSubmitError("");
    try {
      const fn = role === "COMPANY" ? raiseTicketAsCompany : raiseTicketAsUser;
      const ticket = await fn(selectedSlot.id, serviceType, description);
      setSuccessTicket({ ticketNumber: ticket.ticketNumber, id: ticket.id });
    } catch (err: unknown) {
      const errObj = err as Record<string, string>;
      setSubmitError(errObj?.error || "Failed to create booking. The slot may already be taken.");
    } finally {
      setSubmitting(false);
    }
  };

  const groupedSlots = groupByDate(slots);
  const dateKeys = Object.keys(groupedSlots).sort();

  // ── Success screen ────────────────────────────────────────────
  const dashPath = role === "COMPANY" ? "/company/dashboard" : "/public-user";

  useEffect(() => {
    if (successTicket) {
      const timer = setTimeout(() => {
        router.push(dashPath);
      }, 5000);
      return () => clearTimeout(timer);
    }
  }, [successTicket, dashPath, router]);

  if (successTicket) {
    return (
      <main className="min-h-screen bg-[#0f172a] flex items-center justify-center p-6 font-['Public_Sans']">
        <div className="bg-[#111827] p-10 rounded-[40px] shadow-2xl border border-emerald-500/20 max-w-xl w-full text-center relative overflow-hidden group">
          <div className="absolute top-0 left-0 w-full h-2 bg-emerald-600 shadow-[0_0_20px_rgba(5,150,105,0.4)]" />
          
          <div className="mb-8 flex justify-center">
            <div className="bg-emerald-500/10 rounded-full p-6 relative">
              <span className="material-symbols-outlined text-6xl text-emerald-400 animate-bounce">task_alt</span>
              <div className="absolute inset-0 rounded-full border-4 border-emerald-500/20 animate-ping opacity-20" />
            </div>
          </div>

          <h2 className="text-4xl font-black text-slate-50 tracking-tight mb-4">Booking Successful!</h2>
          <p className="text-slate-400 font-medium mb-8 leading-relaxed max-w-sm mx-auto">
            Your ticket <span className="text-emerald-400 font-black tracking-widest italic">#{successTicket.ticketNumber}</span> has been confirmed.
            The technician has been notified via email.
          </p>

          <div className="bg-slate-800/40 rounded-3xl p-6 mb-10 text-left border border-white/5 backdrop-blur-sm">
             <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-[10px] font-black text-emerald-500/50 uppercase tracking-widest mb-1">Service</p>
                <p className="font-bold text-slate-100">{serviceType}</p>
              </div>
              <div>
                <p className="text-[10px] font-black text-emerald-500/50 uppercase tracking-widest mb-1">Status</p>
                <p className="font-bold text-amber-400 flex items-center gap-1.5 uppercase text-[11px]">
                  <span className="w-1.5 h-1.5 rounded-full bg-amber-400 animate-pulse" />
                  Pending Review
                </p>
              </div>
            </div>
          </div>

          <div className="flex flex-col gap-3">
            <button 
              className="w-full bg-emerald-600 hover:bg-emerald-700 text-white py-4 rounded-2xl font-black text-sm uppercase tracking-widest transition-all duration-300 shadow-lg shadow-emerald-900/40 active:scale-[0.98] flex items-center justify-center gap-3"
              onClick={() => router.push(dashPath)}
            >
              Go to Dashboard
            </button>
            <p className="text-[10px] text-emerald-500/40 font-black uppercase tracking-[0.2em] mt-4 animate-pulse">
              Redirecting in 5 seconds
            </p>
          </div>
        </div>
        <style jsx global>{pageStyles}</style>
      </main>
    );
  }

  // ── Loading / Error ───────────────────────────────────────────
  if (!authChecked || loadingSlots) {
    return (
      <div className="book-page">
        <div className="center-wrap">
          <div className="spinner" />
          <p className="loading-txt">Loading availability…</p>
        </div>
        <style jsx global>{pageStyles}</style>
      </div>
    );
  }

  if (slotsError) {
    return (
      <div className="book-page">
        <div className="center-wrap">
          <div className="err-card">
            <p className="err-title">Could not load data</p>
            <p className="err-msg">{slotsError}</p>
            <button className="btn-back" onClick={() => router.back()}>← Go Back</button>
          </div>
        </div>
        <style jsx global>{pageStyles}</style>
      </div>
    );
  }

  const initials = technician
    ? `${technician.firstName?.[0] || ""}${technician.lastName?.[0] || ""}`.toUpperCase()
    : "T";

  return (
    <div className="book-page">
      {/* ── Back nav ───────────────────────────────────────── */}
      <div className="book-topbar">
        <button className="back-btn" onClick={() => router.back()}>
          <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          Back
        </button>
        <span className="topbar-title">Book a Service</span>
      </div>

      <div className="book-layout">
        {/* ── Left: Technician profile ────────────────────── */}
        <aside className="tech-pane">
          <div className="tech-avatar">{initials}</div>
          <h2 className="tech-name">{technician?.firstName} {technician?.lastName}</h2>
          <p className="tech-spec">{technician?.specialization || "General Technician"}</p>

          <div className="tech-meta-list">
            {technician?.skillLevel && (
              <div className="tech-meta-row">
                <span className="meta-icon">⭐</span>
                <span className="meta-label">Skill Level</span>
                <span className="meta-val">{technician.skillLevel.charAt(0) + technician.skillLevel.slice(1).toLowerCase()}</span>
              </div>
            )}
            {technician?.yearsOfExperience !== undefined && (
              <div className="tech-meta-row">
                <span className="meta-icon">🕐</span>
                <span className="meta-label">Experience</span>
                <span className="meta-val">{technician.yearsOfExperience} yrs</span>
              </div>
            )}
            {technician?.district && (
              <div className="tech-meta-row">
                <span className="meta-icon">📍</span>
                <span className="meta-label">District</span>
                <span className="meta-val">{technician.district}</span>
              </div>
            )}
            {technician?.phoneNumber && (
              <div className="tech-meta-row">
                <span className="meta-icon">📞</span>
                <span className="meta-label">Contact</span>
                <span className="meta-val">{technician.phoneNumber}</span>
              </div>
            )}
          </div>

          {slots.length > 0 && (
            <div className="slot-count-badge">
              {slots.length} slot{slots.length !== 1 ? "s" : ""} available
            </div>
          )}
        </aside>

        {/* ── Right: Booking form ─────────────────────────── */}
        <div className="form-pane">
          <div className="form-header">
            <h1>Book a Service Appointment</h1>
            <p>Select an available time slot, choose your service type, and confirm your booking.</p>
          </div>

          <form onSubmit={handleSubmit} noValidate>

            {/* ── Step 1: Pick a slot ─────────────────── */}
            <div className="form-section">
              <div className="section-label">
                <span className="step-num">1</span>
                Select a Time Slot
              </div>

              {slots.length === 0 ? (
                <div className="no-slots">
                  <svg width="28" height="28" fill="none" stroke="#64748b" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                      d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                  <p>No available slots for this technician right now. Please check back later.</p>
                </div>
              ) : (
                <div className="slots-container">
                  {dateKeys.map((date) => (
                    <div key={date} className="date-group">
                      <div className="date-header">{formatDate(date)}</div>
                      <div className="slots-row">
                        {groupedSlots[date].map((slot) => (
                          <button
                            key={slot.id}
                            type="button"
                            className={`slot-chip${selectedSlot?.id === slot.id ? " slot-chip-selected" : ""}`}
                            onClick={() => setSelectedSlot(slot)}
                          >
                            {formatTime(slot.startTime)} – {formatTime(slot.endTime)}
                          </button>
                        ))}
                      </div>
                    </div>
                  ))}
                </div>
              )}

              {selectedSlot && (
                <div className="selected-slot-badge">
                  ✓ Selected: {formatDate(selectedSlot.date)} · {formatTime(selectedSlot.startTime)} – {formatTime(selectedSlot.endTime)}
                </div>
              )}
            </div>

            {/* ── Step 2: Service type ─────────────────── */}
            <div className="form-section">
              <div className="section-label">
                <span className="step-num">2</span>
                Service Type <span className="required">*</span>
              </div>
              <select
                id="serviceType"
                value={serviceType}
                onChange={(e) => setServiceType(e.target.value)}
                required
                className="form-select"
              >
                <option value="">— Select service type —</option>
                {SERVICE_TYPES.map((t) => (
                  <option key={t} value={t}>{t}</option>
                ))}
              </select>
            </div>

            {/* ── Step 3: Description ──────────────────── */}
            <div className="form-section">
              <div className="section-label">
                <span className="step-num">3</span>
                Description <span className="optional">(optional)</span>
              </div>
              <textarea
                id="description"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                maxLength={500}
                rows={4}
                placeholder="Describe the issue or what service you need…"
                className="form-textarea"
              />
              <div className="char-count">{description.length}/500</div>
            </div>

            {/* ── Error ─────────────────────────────────── */}
            {submitError && (
              <div className="submit-error">
                <svg width="16" height="16" fill="none" stroke="#f87171" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                    d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                {submitError}
              </div>
            )}

            {/* ── Submit ─────────────────────────────────── */}
            <button
              type="submit"
              disabled={submitting || slots.length === 0 || !selectedSlot}
              className="submit-btn"
              id="submit-booking"
            >
              {submitting ? (
                <>
                  <span className="btn-spinner" />
                  Creating Booking…
                </>
              ) : (
                <>
                  <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                      d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  Confirm Booking
                </>
              )}
            </button>
          </form>
        </div>
      </div>

      <style jsx global>{pageStyles}</style>
    </div>
  );
}

const pageStyles = `
  * { box-sizing: border-box; }
  html, body { margin: 0; padding: 0; background: #0f172a; }

  .book-page {
    min-height: 100vh;
    background: #0f172a;
    font-family: var(--font-sans, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif);
    color: #f8fafc;
  }

  /* ── Topbar ──────────────────────────────────── */
  .book-topbar {
    display: flex;
    align-items: center;
    gap: 14px;
    background: #111827;
    border-bottom: 1px solid rgba(4, 120, 87, 0.2);
    padding: 14px 32px;
  }
  .back-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    background: transparent;
    border: 1px solid rgba(255,255,255,0.12);
    color: #94a3b8;
    border-radius: 8px;
    padding: 7px 14px;
    font-size: 13px;
    cursor: pointer;
    transition: all 0.15s ease;
  }
  .back-btn:hover { background: rgba(255,255,255,0.06); color: #e2e8f0; }
  .topbar-title {
    font-size: 14px;
    font-weight: 600;
    color: #cbd5e1;
    letter-spacing: 0.02em;
  }

  /* ── Layout ──────────────────────────────────── */
  .book-layout {
    display: grid;
    grid-template-columns: 280px 1fr;
    gap: 28px;
    max-width: 1100px;
    margin: 32px auto;
    padding: 0 24px 60px;
  }

  /* ── Technician pane ─────────────────────────── */
  .tech-pane {
    background: #111827;
    border: 1px solid rgba(4, 120, 87, 0.2);
    border-radius: 16px;
    padding: 28px 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 8px;
    position: sticky;
    top: 20px;
    align-self: start;
  }
  .tech-avatar {
    width: 72px;
    height: 72px;
    border-radius: 50%;
    background: linear-gradient(135deg, #047857, #065f46);
    border: 3px solid rgba(52, 211, 153, 0.3);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    font-weight: 700;
    color: #fff;
    margin-bottom: 4px;
  }
  .tech-name { font-size: 17px; font-weight: 700; color: #f1f5f9; margin: 0; }
  .tech-spec { font-size: 12px; color: #64748b; margin: 0 0 12px; }

  .tech-meta-list { width: 100%; display: flex; flex-direction: column; gap: 8px; margin: 4px 0 12px; }
  .tech-meta-row {
    display: flex;
    align-items: center;
    gap: 8px;
    background: rgba(4, 120, 87, 0.07);
    border: 1px solid rgba(4, 120, 87, 0.14);
    border-radius: 8px;
    padding: 8px 10px;
    text-align: left;
  }
  .meta-icon { font-size: 13px; flex-shrink: 0; }
  .meta-label { font-size: 10px; color: #64748b; text-transform: uppercase; letter-spacing: 0.06em; flex: 1; }
  .meta-val { font-size: 12px; color: #a7f3d0; font-weight: 600; }

  .slot-count-badge {
    background: rgba(52, 211, 153, 0.1);
    border: 1px solid rgba(52, 211, 153, 0.24);
    color: #34d399;
    border-radius: 999px;
    padding: 5px 14px;
    font-size: 11px;
    font-weight: 700;
    margin-top: 4px;
  }

  /* ── Form pane ───────────────────────────────── */
  .form-pane {
    background: #111827;
    border: 1px solid rgba(255,255,255,0.08);
    border-radius: 16px;
    padding: 32px 36px;
  }
  .form-header { margin-bottom: 28px; }
  .form-header h1 { font-size: 24px; font-weight: 800; color: #f1f5f9; margin: 0 0 6px; }
  .form-header p { font-size: 13px; color: #64748b; margin: 0; line-height: 1.6; }

  .form-section { margin-bottom: 28px; }

  .section-label {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 13px;
    font-weight: 700;
    color: #94a3b8;
    text-transform: uppercase;
    letter-spacing: 0.08em;
    margin-bottom: 14px;
  }
  .step-num {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 22px;
    height: 22px;
    border-radius: 50%;
    background: #047857;
    color: #fff;
    font-size: 11px;
    font-weight: 800;
    flex-shrink: 0;
  }
  .required { color: #f87171; font-size: 12px; }
  .optional { color: #475569; font-size: 11px; font-weight: 400; text-transform: none; letter-spacing: 0; }

  /* Slots */
  .slots-container { display: flex; flex-direction: column; gap: 16px; }

  .date-group {}
  .date-header {
    font-size: 12px;
    font-weight: 700;
    color: #6ee7b7;
    text-transform: uppercase;
    letter-spacing: 0.08em;
    margin-bottom: 8px;
    padding-left: 2px;
  }
  .slots-row { display: flex; flex-wrap: wrap; gap: 8px; }

  .slot-chip {
    background: #1e293b;
    border: 1px solid rgba(255,255,255,0.1);
    color: #cbd5e1;
    border-radius: 9px;
    padding: 9px 16px;
    font-size: 12px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.15s ease;
  }
  .slot-chip:hover { border-color: #047857; color: #34d399; background: rgba(4, 120, 87, 0.1); }
  .slot-chip-selected {
    background: rgba(4, 120, 87, 0.18) !important;
    border-color: #34d399 !important;
    color: #a7f3d0 !important;
    box-shadow: 0 0 0 3px rgba(52, 211, 153, 0.12);
  }

  .no-slots {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
    background: rgba(255,255,255,0.03);
    border: 1px dashed rgba(255,255,255,0.1);
    border-radius: 12px;
    padding: 28px 20px;
    text-align: center;
  }
  .no-slots p { font-size: 13px; color: #475569; margin: 0; }

  .selected-slot-badge {
    margin-top: 12px;
    background: rgba(52, 211, 153, 0.08);
    border: 1px solid rgba(52, 211, 153, 0.22);
    color: #6ee7b7;
    border-radius: 8px;
    padding: 9px 14px;
    font-size: 12px;
    font-weight: 600;
  }

  /* Select & Textarea */
  .form-select, .form-textarea {
    width: 100%;
    background: #1e293b;
    border: 1px solid rgba(255,255,255,0.1);
    color: #e2e8f0;
    border-radius: 10px;
    padding: 12px 14px;
    font-size: 13px;
    outline: none;
    transition: border-color 0.15s ease, box-shadow 0.15s ease;
    font-family: inherit;
    resize: vertical;
    appearance: none;
  }
  .form-select:focus, .form-textarea:focus {
    border-color: #047857;
    box-shadow: 0 0 0 3px rgba(4, 120, 87, 0.15);
  }
  .form-textarea::placeholder { color: #475569; }

  .char-count {
    text-align: right;
    font-size: 10px;
    color: #475569;
    margin-top: 5px;
  }

  /* Error */
  .submit-error {
    display: flex;
    align-items: center;
    gap: 8px;
    background: rgba(239, 68, 68, 0.08);
    border: 1px solid rgba(239, 68, 68, 0.22);
    color: #fca5a5;
    border-radius: 10px;
    padding: 12px 16px;
    font-size: 13px;
    margin-bottom: 16px;
  }

  /* Submit btn */
  .submit-btn {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    background: #047857;
    border: none;
    color: #fff;
    border-radius: 12px;
    padding: 14px 0;
    font-size: 14px;
    font-weight: 700;
    cursor: pointer;
    transition: all 0.2s ease;
    letter-spacing: 0.02em;
  }
  .submit-btn:hover:not(:disabled) { background: #065f46; transform: translateY(-1px); box-shadow: 0 8px 24px rgba(4, 120, 87, 0.35); }
  .submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }
  .btn-spinner {
    width: 16px;
    height: 16px;
    border-radius: 50%;
    border: 2px solid rgba(255,255,255,0.3);
    border-top-color: #fff;
    animation: spin 0.8s linear infinite;
    flex-shrink: 0;
  }

  /* ── Success ─────────────────────────────────── */
  .success-wrap {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 32px 20px;
  }
  .success-card {
    background: #111827;
    border: 1px solid rgba(52, 211, 153, 0.25);
    border-radius: 20px;
    padding: 44px 40px;
    text-align: center;
    max-width: 440px;
    width: 100%;
    box-shadow: 0 24px 64px rgba(0,0,0,0.5);
  }
  .success-icon-ring {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background: rgba(52, 211, 153, 0.1);
    border: 2px solid rgba(52, 211, 153, 0.3);
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 20px;
  }
  .success-card h2 { font-size: 26px; font-weight: 800; color: #f1f5f9; margin: 0 0 8px; }
  .success-card > p { font-size: 14px; color: #64748b; margin: 0 0 20px; }
  .ticket-number {
    background: rgba(4, 120, 87, 0.1);
    border: 1px solid rgba(4, 120, 87, 0.3);
    color: #34d399;
    border-radius: 10px;
    padding: 12px 20px;
    font-size: 18px;
    font-weight: 800;
    letter-spacing: 0.06em;
    margin-bottom: 10px;
  }
  .success-sub { font-size: 13px; color: #64748b; margin: 0 0 28px; }
  .status-pending {
    color: #fbbf24;
    font-weight: 700;
  }
  .success-actions { display: flex; flex-direction: column; gap: 10px; }
  .btn-success-primary {
    background: #047857;
    border: none;
    color: #fff;
    border-radius: 10px;
    padding: 13px 0;
    font-size: 14px;
    font-weight: 700;
    cursor: pointer;
    transition: background 0.15s ease;
  }
  .btn-success-primary:hover { background: #065f46; }
  .btn-success-ghost {
    background: transparent;
    border: 1px solid rgba(255,255,255,0.1);
    color: #64748b;
    border-radius: 10px;
    padding: 12px 0;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.15s ease;
  }
  .btn-success-ghost:hover { border-color: rgba(255,255,255,0.2); color: #94a3b8; }

  /* ── Center / loading ────────────────────────── */
  .center-wrap {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-direction: column;
    gap: 14px;
  }
  .spinner {
    width: 48px; height: 48px; border-radius: 50%;
    border: 4px solid rgba(52, 211, 153, 0.18);
    border-top-color: #34d399;
    animation: spin 0.9s linear infinite;
  }
  .loading-txt { color: #94a3b8; font-size: 13px; margin: 0; }
  .err-card {
    background: #111827;
    border: 1px solid rgba(248, 113, 113, 0.2);
    border-radius: 16px;
    padding: 32px 28px;
    text-align: center;
    max-width: 360px;
  }
  .err-title { font-size: 18px; font-weight: 700; color: #f8fafc; margin: 0 0 8px; }
  .err-msg { font-size: 13px; color: #94a3b8; margin: 0 0 20px; }
  .btn-back {
    background: transparent;
    border: 1px solid rgba(255,255,255,0.12);
    color: #94a3b8;
    border-radius: 8px;
    padding: 9px 20px;
    font-size: 13px;
    cursor: pointer;
    transition: all 0.15s ease;
  }
  .btn-back:hover { background: rgba(255,255,255,0.05); color: #e2e8f0; }

  @keyframes spin { to { transform: rotate(360deg); } }

  @media (max-width: 768px) {
    .book-layout {
      grid-template-columns: 1fr;
      padding: 0 16px 40px;
      margin-top: 20px;
    }
    .tech-pane { position: static; }
    .form-pane { padding: 24px 20px; }
    .book-topbar { padding: 12px 16px; }
  }
`;
