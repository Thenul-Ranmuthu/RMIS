"use client";

import { useState, useEffect } from "react";
import { getToken } from "@/services/authService";

const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:5050";

interface Certification {
  id: number;
  certificationName: string;
  fileType: string;
  fileUrl: string;
  originalFileName: string;
}

interface Technician {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  address: string;
  district: string;
  specialization: string;
  yearsOfExperience: number;
  skillLevel: string;
  status: string;
  registrationDate: string;
  approvalDate: string | null;
  certifications: Certification[];
}

interface Company {
  id: number;
  name: string;        
  email: string;
  companyid: string;   
  status: string;
  approvalDate: string | null;
}

type TabType = "PENDING" | "ACTIVE" | "REJECTED";
type UserCategory = "TECHNICIANS" | "COMPANIES";

export default function AdminTechnicianPage() {
  const [activeTab, setActiveTab] = useState<TabType>("PENDING");
  const [userCategory, setUserCategory] = useState<UserCategory>("TECHNICIANS");

  // Technician state
  const [technicians, setTechnicians] = useState<Technician[]>([]);
  const [selectedTechnician, setSelectedTechnician] = useState<Technician | null>(null);

  // Company state
  const [companies, setCompanies] = useState<Company[]>([]);
  const [selectedCompany, setSelectedCompany] = useState<Company | null>(null);
  const [quota, setQuota] = useState<string>("");

  // Shared UI state
  const [isLoading, setIsLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [rejectReason, setRejectReason] = useState("");
  const [showRejectModal, setShowRejectModal] = useState(false);
  const [rejectingId, setRejectingId] = useState<number | null>(null);

  // ─── Fetch ────────────────────────────────────────────────────────────────

  const fetchData = async () => {
    setIsLoading(true);
    try {
      const token = getToken();

      if (userCategory === "TECHNICIANS") {
        const res = await fetch(
          `${API_BASE}/admin/technicians/${activeTab.toLowerCase()}`,
          { headers: { Authorization: `Bearer ${token}` } }
        );
        if (!res.ok) throw new Error("Failed to fetch technicians");
        setTechnicians(await res.json());
      } else {
        // Companies: single endpoint returns pending + active
        const res = await fetch(
          `${API_BASE}/admin/companies/pending`,
          { headers: { Authorization: `Bearer ${token}` } }
        );
        if (!res.ok) throw new Error("Failed to fetch companies");
        const all: Company[] = await res.json();
        // Filter client-side to match selected tab (API has no rejected companies)
        const filtered =
          activeTab === "REJECTED"
            ? []
            : all.filter((c) => c.status?.toUpperCase() === activeTab);
        setCompanies(filtered);
      }
    } catch {
      setTechnicians([]);
      setCompanies([]);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
    setSelectedTechnician(null);
    setSelectedCompany(null);
    setQuota("");
  }, [activeTab, userCategory]);

  // ─── Technician actions ───────────────────────────────────────────────────

  const handleApproveTechnician = async (id: number) => {
    setActionLoading(true);
    try {
      const token = getToken();
      const res = await fetch(`${API_BASE}/admin/technicians/${id}/approve`, {
        method: "POST",
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) throw new Error("Failed");
      setSelectedTechnician(null);
      fetchData();
    } catch {
      alert("Failed to approve technician");
    } finally {
      setActionLoading(false);
    }
  };

  // ─── Company actions ──────────────────────────────────────────────────────

  const handleApproveCompany = async (company: Company) => {
  const parsedQuota = parseInt(quota, 10);
  if (!quota || isNaN(parsedQuota) || parsedQuota <= 0) {
    alert("Please enter a valid quota amount before approving");
    return;
  }
  setActionLoading(true);
  try {
    const token = getToken();

    // Assign quota to this company by email
    const quotaRes = await fetch(
      `${API_BASE}/admin/companyQouta/${company.email}/${parsedQuota}`,
      { method: "PATCH", headers: { Authorization: `Bearer ${token}` } }
    );
    if (!quotaRes.ok) throw new Error("Failed to assign company quota");

    setSelectedCompany(null);
    setQuota("");
    fetchData();
  } catch (err: any) {
    alert(err?.message || "Failed to approve company");
  } finally {
    setActionLoading(false);
  }
};

  // ─── Shared reject flow ───────────────────────────────────────────────────

  const handleRejectSubmit = async () => {
    if (!rejectReason.trim() || rejectingId == null) return;
    setActionLoading(true);
    try {
      const token = getToken();
      const endpoint =
        userCategory === "TECHNICIANS" ? "technicians" : "companies";
      const res = await fetch(
        `${API_BASE}/admin/${endpoint}/${rejectingId}/reject?reason=${encodeURIComponent(rejectReason)}`,
        { method: "POST", headers: { Authorization: `Bearer ${token}` } }
      );
      if (!res.ok) throw new Error("Failed");
      setShowRejectModal(false);
      setRejectReason("");
      setRejectingId(null);
      setSelectedTechnician(null);
      setSelectedCompany(null);
      fetchData();
    } catch {
      alert(
        `Failed to reject ${userCategory === "TECHNICIANS" ? "technician" : "company"}`
      );
    } finally {
      setActionLoading(false);
    }
  };

  // ─── Misc helpers ─────────────────────────────────────────────────────────

  const formatDate = (dateStr: string | null) => {
    if (!dateStr) return "—";
    return new Date(dateStr).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  };

  const currentCount =
    userCategory === "TECHNICIANS" ? technicians.length : companies.length;

  // ─── Render ───────────────────────────────────────────────────────────────

  return (
    <>
      <div className="page-header">
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "flex-start",
          }}
        >
          <div>
            <h2>User Verification</h2>
            <p>
              Review, approve, or reject{" "}
              {userCategory === "TECHNICIANS" ? "technician" : "company"}{" "}
              registration applications.
            </p>
          </div>
          <div
            style={{
              fontSize: "12px",
              color: "var(--text-muted)",
              fontWeight: 300,
            }}
          >
            {new Date().toLocaleDateString("en-US", {
              month: "long",
              day: "numeric",
              year: "numeric",
            })}
          </div>
        </div>
      </div>

      {/* Summary cards */}
      <div className="summary-cards-container">
        <div className="summary-card">
          <div className="summary-icon yellow">
            <span className="material-symbols-outlined">pending_actions</span>
          </div>
          <div className="summary-info">
            <h3>Pending</h3>
            <div className="summary-value">
              {activeTab === "PENDING" ? currentCount : "—"}
            </div>
          </div>
        </div>
        <div className="summary-card highlight">
          <div className="summary-icon green">
            <span className="material-symbols-outlined">how_to_reg</span>
          </div>
          <div className="summary-info">
            <h3>Active</h3>
            <div className="summary-value">
              {activeTab === "ACTIVE" ? currentCount : "—"}
            </div>
          </div>
        </div>
        <div className="summary-card">
          <div
            className="summary-icon"
            style={{ backgroundColor: "#f0c9c9", color: "#a33b3b" }}
          >
            <span className="material-symbols-outlined">person_off</span>
          </div>
          <div className="summary-info">
            <h3>Rejected</h3>
            <div className="summary-value">
              {activeTab === "REJECTED" ? currentCount : "—"}
            </div>
          </div>
        </div>
      </div>

      {/* Main table card */}
      <div className="master-table-card" style={{ marginTop: "24px" }}>
        <div
          className="filters-section"
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
          }}
        >
          {/* Category toggle */}
          <div style={{ display: "flex", gap: "8px" }}>
            <button
              onClick={() => setUserCategory("TECHNICIANS")}
              className={`page-btn ${userCategory === "TECHNICIANS" ? "active-tab" : ""}`}
              style={{
                borderRadius: "8px",
                padding: "8px 16px",
                backgroundColor:
                  userCategory === "TECHNICIANS" ? "#1a4a38" : "white",
                color: userCategory === "TECHNICIANS" ? "white" : "#4b5563",
                border:
                  userCategory === "TECHNICIANS" ? "none" : "1px solid #e5e7eb",
                fontWeight: 600,
              }}
            >
              Technicians
            </button>
            <button
              onClick={() => setUserCategory("COMPANIES")}
              className={`page-btn ${userCategory === "COMPANIES" ? "active-tab" : ""}`}
              style={{
                borderRadius: "8px",
                padding: "8px 16px",
                backgroundColor:
                  userCategory === "COMPANIES" ? "#1a4a38" : "white",
                color: userCategory === "COMPANIES" ? "white" : "#4b5563",
                border:
                  userCategory === "COMPANIES" ? "none" : "1px solid #e5e7eb",
                fontWeight: 600,
              }}
            >
              Company Users
            </button>
          </div>

          {/* Status filter */}
          <div className="filters-grid" style={{ margin: 0 }}>
            <div
              className="filter-group"
              style={{
                flexDirection: "row",
                alignItems: "center",
                gap: "12px",
              }}
            >
              <label style={{ whiteSpace: "nowrap", marginBottom: 0 }}>
                Filter by Status:
              </label>
              <div className="filter-input" style={{ minWidth: "180px" }}>
                <select
                  value={activeTab}
                  onChange={(e) => setActiveTab(e.target.value as TabType)}
                >
                  <option value="PENDING">Pending Approval</option>
                  <option value="ACTIVE">Active Records</option>
                  {/* Companies have no rejected state from the API */}
                  {userCategory === "TECHNICIANS" && (
                    <option value="REJECTED">Rejected Applications</option>
                  )}
                </select>
                <span className="material-symbols-outlined select-icon">
                  expand_more
                </span>
              </div>
            </div>
          </div>
        </div>

        <div className="table-section">
          <table className="data-table">
            <thead>
              <tr>
                <th>
                  {userCategory === "TECHNICIANS" ? "Technician" : "Company Name"}
                </th>
                <th>
                  {userCategory === "TECHNICIANS" ? "Specialization" : "Reg. Number"}
                </th>
                <th>
                  {userCategory === "TECHNICIANS" ? "Experience" : "Contact Email"}
                </th>
                {userCategory === "TECHNICIANS" && <th>Registration Date</th>}
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr>
                  <td colSpan={6} style={{ textAlign: "center", padding: "40px" }}>
                    Loading {userCategory === "TECHNICIANS" ? "technicians" : "companies"}...
                  </td>
                </tr>
              ) : userCategory === "TECHNICIANS" ? (
                technicians.length === 0 ? (
                  <tr>
                    <td colSpan={6} style={{ textAlign: "center", padding: "40px" }}>
                      No technicians found.
                    </td>
                  </tr>
                ) : (
                  technicians.map((t) => (
                    <tr key={t.id} onClick={() => setSelectedTechnician(t)}>
                      <td>
                        <div className="req-id">
                          {t.firstName} {t.lastName}
                        </div>
                        <div style={{ fontSize: "11px", opacity: 0.7 }}>
                          {t.email}
                        </div>
                      </td>
                      <td>{t.specialization || "—"}</td>
                      <td>{t.yearsOfExperience || 0} yrs</td>
                      <td>{formatDate(t.registrationDate)}</td>
                      <td>
                        <span
                          className={`status-badge ${
                            t.status === "ACTIVE"
                              ? "status-approved"
                              : t.status === "PENDING"
                              ? "status-pending"
                              : "status-rejected"
                          }`}
                        >
                          {t.status}
                        </span>
                      </td>
                      <td>
                        <span className="action-link">Review</span>
                      </td>
                    </tr>
                  ))
                )
              ) : companies.length === 0 ? (
                <tr>
                  <td colSpan={6} style={{ textAlign: "center", padding: "40px" }}>
                    No companies found.
                  </td>
                </tr>
              ) : (
                companies.map((c) => (
                  <tr key={c.id} onClick={() => setSelectedCompany(c)}>
                    <td>
                      <div className="req-id">{c.name}</div>
                      <div style={{ fontSize: "11px", opacity: 0.7 }}>
                        {c.email}
                      </div>
                    </td>
                    <td>{c.companyid}</td>
                    <td>{c.email}</td>
                    <td>
                      <span
                        className={`status-badge ${
                          c.status === "ACTIVE"
                            ? "status-approved"
                            : c.status === "PENDING"
                            ? "status-pending"
                            : "status-rejected"
                        }`}
                      >
                        {c.status}
                      </span>
                    </td>
                    <td>
                      <span className="action-link">Review</span>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>

          <div className="table-footer" style={{ padding: "16px 32px" }}>
            <div className="pagination-info" style={{ marginLeft: "8px" }}>
              Count: {currentCount}
            </div>
          </div>
        </div>
      </div>

      {/* ── Technician detail modal ── */}
      {selectedTechnician && (
        <div
          style={{
            position: "fixed",
            inset: 0,
            backgroundColor: "rgba(0,0,0,0.4)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            zIndex: 50,
            backdropFilter: "blur(4px)",
          }}
        >
          <div
            className="master-table-card"
            style={{ width: "600px", background: "white", padding: "32px" }}
          >
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                marginBottom: "24px",
              }}
            >
              <h3>
                Technician Profile: {selectedTechnician.firstName}{" "}
                {selectedTechnician.lastName}
              </h3>
              <button
                onClick={() => setSelectedTechnician(null)}
                style={{ border: "none", background: "none", cursor: "pointer" }}
              >
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>

            <div
              style={{
                display: "grid",
                gridTemplateColumns: "1fr 1fr",
                gap: "16px",
                marginBottom: "24px",
              }}
            >
              <p><strong>Email:</strong> {selectedTechnician.email}</p>
              <p><strong>Phone:</strong> {selectedTechnician.phoneNumber}</p>
              <p><strong>Specialization:</strong> {selectedTechnician.specialization || "—"}</p>
              <p><strong>District:</strong> {selectedTechnician.district || "—"}</p>
              <p><strong>Experience:</strong> {selectedTechnician.yearsOfExperience} years</p>
              <p><strong>Status:</strong> {selectedTechnician.status}</p>
            </div>

            {selectedTechnician.status === "PENDING" && (
              <div
                style={{
                  padding: "20px",
                  background: "#f8fafc",
                  borderRadius: "12px",
                  marginBottom: "24px",
                }}
              >
                <p style={{ marginBottom: "16px", fontWeight: 600 }}>
                  Approval Action
                </p>
                <div style={{ display: "flex", gap: "12px" }}>
                  <button
                    onClick={() => handleApproveTechnician(selectedTechnician.id)}
                    className="btn-primary"
                    disabled={actionLoading}
                  >
                    {actionLoading ? "Processing..." : "Approve Application"}
                  </button>
                  <button
                    onClick={() => {
                      setRejectingId(selectedTechnician.id);
                      setShowRejectModal(true);
                    }}
                    className="btn-primary"
                    style={{ backgroundColor: "#dc2626" }}
                    disabled={actionLoading}
                  >
                    Reject
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* ── Company detail modal ── */}
      {/* ── Company detail modal ── */}
      {selectedCompany && (
        <div
          style={{
            position: "fixed",
            inset: 0,
            backgroundColor: "rgba(0,0,0,0.4)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            zIndex: 50,
            backdropFilter: "blur(4px)",
          }}
        >
          <div
            className="master-table-card"
            style={{ width: "440px", background: "white", padding: "32px" }}
          >
            {/* Header */}
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                marginBottom: "24px",
              }}
            >
              <h3 style={{ margin: 0 }}>Company Application</h3>
              <button
                onClick={() => {
                  setSelectedCompany(null);
                  setQuota("");
                }}
                style={{ border: "none", background: "none", cursor: "pointer" }}
              >
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>

            {/* Company info — name, id, email only */}
            <div
              style={{
                background: "#f8fafc",
                borderRadius: "10px",
                padding: "16px 20px",
                marginBottom: "24px",
                display: "flex",
                flexDirection: "column",
                gap: "10px",
              }}
            >
              <div style={{ display: "flex", justifyContent: "space-between" }}>
                <span style={{ fontSize: "13px", color: "#6b7280" }}>Company Name</span>
                <span style={{ fontSize: "13px", fontWeight: 600 }}>{selectedCompany.name}</span>
              </div>
              <div style={{ display: "flex", justifyContent: "space-between" }}>
                <span style={{ fontSize: "13px", color: "#6b7280" }}>Company ID</span>
                <span style={{ fontSize: "13px", fontWeight: 600 }}>{selectedCompany.id}</span>
              </div>
              <div style={{ display: "flex", justifyContent: "space-between" }}>
                <span style={{ fontSize: "13px", color: "#6b7280" }}>Email</span>
                <span style={{ fontSize: "13px", fontWeight: 600 }}>{selectedCompany.email}</span>
              </div>
            </div>

            {/* Quota + actions — only shown for PENDING */}
            {selectedCompany.status === "PENDING" && (
              <>
                <div className="filter-group" style={{ marginBottom: "20px" }}>
                  <label style={{ fontSize: "13px", fontWeight: 600, marginBottom: "6px", display: "block" }}>
                    Allocate Quota
                  </label>
                  <input
                    type="number"
                    min={1}
                    value={quota}
                    onChange={(e) => setQuota(e.target.value)}
                    placeholder="Enter quota amount..."
                    style={{
                      width: "100%",
                      padding: "10px 14px",
                      borderRadius: "8px",
                      border: "1px solid #ddd",
                      fontSize: "14px",
                      outline: "none",
                      boxSizing: "border-box",
                    }}
                  />
                </div>

                <div style={{ display: "flex", gap: "12px" }}>
                  <button
                    onClick={() => handleApproveCompany(selectedCompany)}
                    className="btn-primary"
                    disabled={actionLoading || !quota}
                    style={{ flex: 1 }}
                  >
                    {actionLoading ? "Processing..." : "Approve & Allocate"}
                  </button>
                  <button
                    onClick={() => {
                      setRejectingId(selectedCompany.id);
                      setShowRejectModal(true);
                    }}
                    className="btn-primary"
                    style={{ flex: 1, backgroundColor: "#dc2626" }}
                    disabled={actionLoading}
                  >
                    Reject
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      )}
      
      {/* ── Reject modal ── */}
      {showRejectModal && (
        <div
          style={{
            position: "fixed",
            inset: 0,
            backgroundColor: "rgba(0,0,0,0.4)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            zIndex: 60,
            backdropFilter: "blur(4px)",
          }}
        >
          <div
            className="master-table-card"
            style={{ width: "400px", background: "white", padding: "24px" }}
          >
            <h3 style={{ marginBottom: "16px" }}>
              Reject {userCategory === "TECHNICIANS" ? "Technician" : "Company"} Application
            </h3>
            <textarea
              style={{
                width: "100%",
                padding: "12px",
                borderRadius: "8px",
                border: "1px solid #ddd",
                marginBottom: "16px",
                resize: "none",
              }}
              placeholder="Reason for rejection..."
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              rows={4}
            />
            <div
              style={{
                display: "flex",
                gap: "12px",
                justifyContent: "flex-end",
              }}
            >
              <button
                onClick={() => {
                  setShowRejectModal(false);
                  setRejectReason("");
                  setRejectingId(null);
                }}
                className="page-btn"
                disabled={actionLoading}
              >
                Cancel
              </button>
              <button
                onClick={handleRejectSubmit}
                className="btn-primary"
                style={{ backgroundColor: "#dc2626" }}
                disabled={!rejectReason.trim() || actionLoading}
              >
                {actionLoading ? "Submitting..." : "Submit Rejection"}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}