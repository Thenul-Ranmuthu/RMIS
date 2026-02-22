// app/company/page.tsx
// Company homepage — replace this with your actual dashboard content.
"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getToken, clearToken } from "@/lib/auth/tokenUtils";

export default function CompanyHomePage() {
  const router = useRouter();
  const [ready, setReady] = useState(false);

  useEffect(() => {
    if (!getToken()) {
      router.replace("/auth/login?role=company");
    } else {
      setReady(true);
    }
  }, [router]);

  const handleLogout = () => {
    clearToken();
    router.push("/auth/login?role=company");
  };

  if (!ready) return null;

  return (
    <div style={{
      minHeight: "100vh",
      display: "flex",
      flexDirection: "column",
      alignItems: "center",
      justifyContent: "center",
      fontFamily: "'DM Sans', sans-serif",
      background: "#F7F7F2",
      gap: 24,
    }}>
      <div style={{
        background: "#fff",
        borderRadius: 16,
        padding: "48px 56px",
        boxShadow: "0 4px 32px rgba(0,0,0,0.08)",
        textAlign: "center",
        maxWidth: 480,
      }}>
        <div style={{
          width: 64, height: 64, borderRadius: 16,
          background: "#22C55E",
          display: "flex", alignItems: "center", justifyContent: "center",
          margin: "0 auto 20px",
        }}>
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
        </div>
        <h1 style={{ fontFamily: "'Outfit', sans-serif", fontSize: 26, fontWeight: 700, marginBottom: 8, color: "#1A2E1A" }}>
          Welcome to Your Dashboard
        </h1>
        <p style={{ color: "#6B7280", fontSize: 14, lineHeight: 1.6, marginBottom: 32 }}>
          You are successfully logged in to the Ministry of Environment Company Portal.
          This is where your company dashboard content will appear.
        </p>
        <button
          onClick={handleLogout}
          style={{
            padding: "11px 28px",
            background: "transparent",
            border: "1.5px solid #E5E7EB",
            borderRadius: 10,
            fontFamily: "'DM Sans', sans-serif",
            fontSize: 14,
            fontWeight: 500,
            color: "#374151",
            cursor: "pointer",
          }}
        >
          Sign Out
        </button>
      </div>
    </div>
  );
}
