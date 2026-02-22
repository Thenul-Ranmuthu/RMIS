// app/auth/verify/page.tsx
"use client";

import { useState, useRef, useEffect, KeyboardEvent, ClipboardEvent } from "react";
import { useRouter } from "next/navigation";
import AuthLayout from "@/components/auth/AuthLayout";
import { registerCompany, sendVerificationEmail } from "@/lib/api/companyAuth";
import { saveToken } from "@/lib/auth/tokenUtils";

export default function VerifyPage() {
  const router = useRouter();
  const [digits, setDigits] = useState<string[]>(["", "", "", "", "", ""]);
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);
  const [error, setError] = useState("");
  const [resendSuccess, setResendSuccess] = useState("");
  const [resendCooldown, setResendCooldown] = useState(0);
  const [regData, setRegData] = useState<{
    name: string; email: string; companyid: string; password: string;
  } | null>(null);

  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

  // Load registration data from session storage
  useEffect(() => {
    const stored = sessionStorage.getItem("moe_reg_data");
    if (!stored) {
      router.replace("/auth/register");
      return;
    }
    setRegData(JSON.parse(stored));
    inputRefs.current[0]?.focus();
  }, [router]);

  // Cooldown timer for resend
  useEffect(() => {
    if (resendCooldown <= 0) return;
    const t = setTimeout(() => setResendCooldown((c) => c - 1), 1000);
    return () => clearTimeout(t);
  }, [resendCooldown]);

  const handleDigitChange = (index: number, value: string) => {
    const char = value.replace(/\D/g, "").slice(-1);
    const newDigits = [...digits];
    newDigits[index] = char;
    setDigits(newDigits);
    setError("");

    if (char && index < 5) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  const handleKeyDown = (index: number, e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Backspace" && !digits[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
    if (e.key === "ArrowLeft" && index > 0) inputRefs.current[index - 1]?.focus();
    if (e.key === "ArrowRight" && index < 5) inputRefs.current[index + 1]?.focus();
  };

  const handlePaste = (e: ClipboardEvent<HTMLInputElement>) => {
    e.preventDefault();
    const pasted = e.clipboardData.getData("text").replace(/\D/g, "").slice(0, 6);
    if (pasted.length === 6) {
      setDigits(pasted.split(""));
      inputRefs.current[5]?.focus();
    }
  };

  const handleVerify = async () => {
    const code = digits.join("");
    if (code.length < 6) {
      setError("Please enter all 6 digits of the verification code.");
      return;
    }
    if (!regData) return;

    setLoading(true);
    setError("");
    try {
      const { name, email, companyid, password } = regData;
      const data = await registerCompany(code, { name, email, companyid, password });
      saveToken(data.accessToken, data.tokenType);
      sessionStorage.removeItem("moe_reg_data");
      router.push("/company");
    } catch (err: any) {
      setError(err.message || "Invalid verification code. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    if (!regData || resendCooldown > 0) return;
    setResending(true);
    setResendSuccess("");
    setError("");
    try {
      await sendVerificationEmail(regData.email);
      setResendSuccess("A new verification code has been sent to your email.");
      setResendCooldown(60);
      setDigits(["", "", "", "", "", ""]);
      inputRefs.current[0]?.focus();
    } catch (err: any) {
      setError(err.message || "Failed to resend code. Please try again.");
    } finally {
      setResending(false);
    }
  };

  const code = digits.join("");

  return (
    <AuthLayout>
      <div className="auth-form-container">
        <div className="step-badge">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5"><polyline points="20 6 9 17 4 12"/></svg>
          Step 2 of 2 — Verify Email
        </div>

        <h1>Enter Verification Code</h1>
        <p className="subtitle">
          We&apos;ve sent a 6-digit code to your company email. Please enter it below to complete registration.
        </p>

        {regData && (
          <div className="email-highlight">
            📧 {regData.email}
          </div>
        )}

        {error && (
          <div className="alert alert-error">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            {error}
          </div>
        )}

        {resendSuccess && (
          <div className="alert alert-success">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="20 6 9 17 4 12"/></svg>
            {resendSuccess}
          </div>
        )}

        {/* OTP Digit Inputs */}
        <div className="otp-container">
          {digits.map((digit, i) => (
            <input
              key={i}
              ref={(el) => { inputRefs.current[i] = el; }}
              className={`otp-input ${digit ? "filled" : ""}`}
              type="text"
              inputMode="numeric"
              maxLength={1}
              value={digit}
              onChange={(e) => handleDigitChange(i, e.target.value)}
              onKeyDown={(e) => handleKeyDown(i, e)}
              onPaste={handlePaste}
              autoComplete="one-time-code"
            />
          ))}
        </div>

        <p style={{ textAlign: "center", fontSize: 13, color: "var(--text-muted)", marginBottom: 20 }}>
          You can also paste your 6-digit code directly.
        </p>

        {/* Verify Button */}
        <button
          className="btn-primary"
          onClick={handleVerify}
          disabled={loading || code.length < 6}
        >
          {loading ? (
            <><div className="spinner" /> Verifying...</>
          ) : (
            <>Verify & Create Account <span>→</span></>
          )}
        </button>

        {/* Resend Code Button */}
        <button
          className="btn-secondary"
          onClick={handleResend}
          disabled={resending || resendCooldown > 0}
        >
          {resending ? (
            <><div className="spinner" style={{ borderTopColor: "var(--green-dark)" }} /> Resending...</>
          ) : resendCooldown > 0 ? (
            <>Resend Code ({resendCooldown}s)</>
          ) : (
            <>
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 1 0 .49-3.51"/>
              </svg>
              Resend Verification Code
            </>
          )}
        </button>

        <div className="divider" />

        <p className="auth-switch">
          Wrong email?{" "}
          <a
            href="/auth/register"
            onClick={() => sessionStorage.removeItem("moe_reg_data")}
          >
            ← Go back to Registration
          </a>
        </p>
      </div>
    </AuthLayout>
  );
}
