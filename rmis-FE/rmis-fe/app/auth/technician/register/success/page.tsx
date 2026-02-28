// app/auth/technician/register/success/page.tsx
"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import AuthLayout from "@/components/auth/AuthLayout";

const CheckIcon = () => (
  <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#10b981" strokeWidth="2">
    <circle cx="12" cy="12" r="10"/>
    <path d="M8 12l3 3 6-6"/>
  </svg>
);

export default function TechnicianRegisterSuccessPage() {
  const [email, setEmail] = useState("");

  useEffect(() => {
    const stored = sessionStorage.getItem("registered_tech_email");
    if (stored) setEmail(stored);
  }, []);

  return (
    <AuthLayout>
      <div className="auth-form-container" style={{ textAlign: "center" }}>
        <div style={{ marginBottom: 24 }}>
          <CheckIcon />
        </div>

        <h1>Registration Submitted!</h1>
        <p className="subtitle" style={{ marginBottom: 16 }}>
          Thank you for registering as a technician.
        </p>

        <div className="alert alert-info" style={{ textAlign: "left", marginBottom: 24 }}>
          <strong>Next Steps:</strong>
          <ul style={{ marginTop: 8, paddingLeft: 20 }}>
            <li>Your application is now pending admin review</li>
            <li>You will receive an email notification once your certifications are verified</li>
            <li>After approval, you will be visible in the public technician directory</li>
            <li>You can then log in and start receiving service requests</li>
          </ul>
        </div>

        {email && (
          <p style={{ marginBottom: 24, color: "var(--text-light)" }}>
            A confirmation has been sent to: <strong>{email}</strong>
          </p>
        )}

        <div style={{ display: "flex", gap: 16, justifyContent: "center" }}>
          <Link href="/" className="btn-secondary">
            Go to Homepage
          </Link>
          <Link href="/auth/login?role=technician" className="btn-primary">
            Sign In →
          </Link>
        </div>
      </div>
    </AuthLayout>
  );
}