// app/auth/register/page.tsx
"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import AuthLayout from "@/components/auth/AuthLayout";
import RoleTabs from "@/components/auth/RoleTabs";
import { sendVerificationEmail } from "@/lib/api/companyAuth";

// Icons
const EyeIcon = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
  </svg>
);
const EyeOffIcon = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/>
  </svg>
);
const MailIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/>
  </svg>
);
const LockIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>
  </svg>
);
const BuildingIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <rect x="2" y="7" width="20" height="15" rx="2"/><path d="M16 7V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v2"/>
  </svg>
);
const IdIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <rect x="2" y="5" width="20" height="14" rx="2"/><line x1="8" y1="10" x2="8" y2="14"/><line x1="12" y1="10" x2="12" y2="14"/><line x1="16" y1="10" x2="16" y2="14"/>
  </svg>
);

interface FormData {
  name: string;
  email: string;
  companyid: string;
  password: string;
  confirmPassword: string;
}

interface FormErrors {
  name?: string;
  email?: string;
  companyid?: string;
  password?: string;
  confirmPassword?: string;
}

export default function CompanyRegisterPage() {
  const router = useRouter();
  const [form, setForm] = useState<FormData>({
    name: "",
    email: "",
    companyid: "",
    password: "",
    confirmPassword: "",
  });
  const [errors, setErrors] = useState<FormErrors>({});
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [loading, setLoading] = useState(false);
  const [apiError, setApiError] = useState("");

  const update = (field: keyof FormData) => (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm((prev) => ({ ...prev, [field]: e.target.value }));
    if (errors[field]) setErrors((prev) => ({ ...prev, [field]: "" }));
  };

  const validate = (): boolean => {
    const newErrors: FormErrors = {};
    if (!form.name.trim()) newErrors.name = "Company name is required.";
    if (!form.email.trim()) newErrors.email = "Email is required.";
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) newErrors.email = "Enter a valid email address.";
    if (!form.companyid.trim()) newErrors.companyid = "Company ID is required.";
    if (!form.password) newErrors.password = "Password is required.";
    else if (form.password.length < 6) newErrors.password = "Password must be at least 6 characters.";
    if (!form.confirmPassword) newErrors.confirmPassword = "Please confirm your password.";
    else if (form.password !== form.confirmPassword) newErrors.confirmPassword = "Passwords do not match.";
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setApiError("");
    if (!validate()) return;

    setLoading(true);
    try {
      await sendVerificationEmail(form.email);
      // Store registration data in sessionStorage to use in verify step
      sessionStorage.setItem(
        "moe_reg_data",
        JSON.stringify({ name: form.name, email: form.email, companyid: form.companyid, password: form.password })
      );
      router.push("/auth/verify");
    } catch (err: any) {
      setApiError(err.message || "Failed to send verification email. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout>
      <div className="auth-form-container">
        <div className="step-badge">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
          Step 1 of 2
        </div>

        <h1>Create Company Account</h1>
        <p className="subtitle">Register your company with the Ministry of Environment portal.</p>

        <RoleTabs activeRole="company" mode="register" />

        {apiError && (
          <div className="alert alert-error">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            {apiError}
          </div>
        )}

        <form onSubmit={handleSubmit} noValidate>
          {/* Company Name */}
          <div className="field">
            <label htmlFor="name">Company Name</label>
            <div className="input-wrapper">
              <span className="input-icon"><BuildingIcon /></span>
              <input
                id="name"
                type="text"
                placeholder="e.g. My Company Pvt Ltd."
                value={form.name}
                onChange={update("name")}
                className={errors.name ? "error" : ""}
              />
            </div>
            {errors.name && <p className="field-error">{errors.name}</p>}
          </div>

          {/* Email */}
          <div className="field">
            <label htmlFor="email">Company Email</label>
            <div className="input-wrapper">
              <span className="input-icon"><MailIcon /></span>
              <input
                id="email"
                type="email"
                placeholder="e.g. contact@mycompany.com"
                value={form.email}
                onChange={update("email")}
                className={errors.email ? "error" : ""}
                autoComplete="email"
              />
            </div>
            {errors.email && <p className="field-error">{errors.email}</p>}
          </div>

          {/* Company ID */}
          <div className="field">
            <label htmlFor="companyid">Company Registration ID</label>
            <div className="input-wrapper">
              <span className="input-icon"><IdIcon /></span>
              <input
                id="companyid"
                type="text"
                placeholder="e.g. C134G123"
                value={form.companyid}
                onChange={update("companyid")}
                className={errors.companyid ? "error" : ""}
              />
            </div>
            {errors.companyid && <p className="field-error">{errors.companyid}</p>}
          </div>

          {/* Password */}
          <div className="field">
            <label htmlFor="password">Password</label>
            <div className="input-wrapper">
              <span className="input-icon"><LockIcon /></span>
              <input
                id="password"
                type={showPassword ? "text" : "password"}
                placeholder="Min. 6 characters"
                value={form.password}
                onChange={update("password")}
                className={errors.password ? "error" : ""}
                autoComplete="new-password"
              />
              <button type="button" className="input-action" onClick={() => setShowPassword(!showPassword)}>
                {showPassword ? <EyeOffIcon /> : <EyeIcon />}
              </button>
            </div>
            {errors.password && <p className="field-error">{errors.password}</p>}
          </div>

          {/* Confirm Password */}
          <div className="field">
            <label htmlFor="confirmPassword">Confirm Password</label>
            <div className="input-wrapper">
              <span className="input-icon"><LockIcon /></span>
              <input
                id="confirmPassword"
                type={showConfirm ? "text" : "password"}
                placeholder="Re-enter your password"
                value={form.confirmPassword}
                onChange={update("confirmPassword")}
                className={errors.confirmPassword ? "error" : ""}
                autoComplete="new-password"
              />
              <button type="button" className="input-action" onClick={() => setShowConfirm(!showConfirm)}>
                {showConfirm ? <EyeOffIcon /> : <EyeIcon />}
              </button>
            </div>
            {errors.confirmPassword && <p className="field-error">{errors.confirmPassword}</p>}
          </div>

          <button type="submit" className="btn-primary" disabled={loading} style={{ marginTop: 8 }}>
            {loading ? (
              <><div className="spinner" /> Sending Verification Email...</>
            ) : (
              <>Continue <span>→</span></>
            )}
          </button>
        </form>

        <p className="auth-switch">
          Already have an account?{" "}
          <Link href="/auth/login?role=company">Sign In →</Link>
        </p>
      </div>
    </AuthLayout>
  );
}
