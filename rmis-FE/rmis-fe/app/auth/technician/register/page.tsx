// app/auth/technician/register/page.tsx
"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import AuthLayout from "@/components/auth/AuthLayout";
import RoleTabs from "@/components/auth/RoleTabs";
import CertificationUpload from "@/components/auth/CertificationUpload";

// Icons
const EyeIcon = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
  </svg>
);
const EyeOffIcon = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/>
  </svg>
);
const MailIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/>
  </svg>
);
const LockIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>
  </svg>
);
const UserIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
  </svg>
);
const PhoneIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <rect x="5" y="2" width="14" height="20" rx="2"/><line x1="12" y1="18" x2="12.01" y2="18"/>
  </svg>
);
const MapPinIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>
  </svg>
);
const BriefcaseIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/>
  </svg>
);
const AwardIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <circle cx="12" cy="8" r="7"/><polyline points="8 21 12 17 16 21"/><line x1="12" y1="17" x2="12" y2="12"/>
  </svg>
);

interface TechnicianFormData {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  password: string;
  confirmPassword: string;
  address: string;
  specialization: string;
  yearsOfExperience: number | "";
  certifications: Array<{
    certificationName: string;
    issuingAuthority: string;
    file: File | null;
  }>;
}

interface FormErrors {
  firstName?: string;
  lastName?: string;
  email?: string;
  phoneNumber?: string;
  password?: string;
  confirmPassword?: string;
  address?: string;
  specialization?: string;
  yearsOfExperience?: string;
  certifications?: string;
}

export default function TechnicianRegisterPage() {
  const router = useRouter();
  const [form, setForm] = useState<TechnicianFormData>({
    firstName: "",
    lastName: "",
    email: "",
    phoneNumber: "",
    password: "",
    confirmPassword: "",
    address: "",
    specialization: "",
    yearsOfExperience: "",
    certifications: [],
  });
  const [errors, setErrors] = useState<FormErrors>({});
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [loading, setLoading] = useState(false);
  const [apiError, setApiError] = useState("");
  const [registrationSuccess, setRegistrationSuccess] = useState(false);

  const update = (field: keyof TechnicianFormData) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const value = field === "yearsOfExperience" && e.target.value === "" ? "" : e.target.value;
    setForm((prev) => ({ ...prev, [field]: value }));
    if (errors[field as keyof FormErrors]) setErrors((prev) => ({ ...prev, [field]: "" }));
  };

  const handleCertificationsChange = (certifications: TechnicianFormData["certifications"]) => {
    setForm((prev) => ({ ...prev, certifications }));
    if (errors.certifications) setErrors((prev) => ({ ...prev, certifications: "" }));
  };

  const validate = (): boolean => {
    const newErrors: FormErrors = {};

    if (!form.firstName.trim()) newErrors.firstName = "First name is required.";
    else if (form.firstName.length < 2) newErrors.firstName = "First name must be at least 2 characters.";

    if (!form.lastName.trim()) newErrors.lastName = "Last name is required.";
    else if (form.lastName.length < 2) newErrors.lastName = "Last name must be at least 2 characters.";

    if (!form.email.trim()) newErrors.email = "Email is required.";
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) newErrors.email = "Enter a valid email address.";

    if (!form.phoneNumber.trim()) newErrors.phoneNumber = "Phone number is required.";
    else if (!/^[0-9]{10,15}$/.test(form.phoneNumber.replace(/[\s-]/g, ''))) {
      newErrors.phoneNumber = "Phone number must be 10-15 digits.";
    }

    if (!form.password) newErrors.password = "Password is required.";
    else if (form.password.length < 6) newErrors.password = "Password must be at least 6 characters.";

    if (!form.confirmPassword) newErrors.confirmPassword = "Please confirm your password.";
    else if (form.password !== form.confirmPassword) newErrors.confirmPassword = "Passwords do not match.";

    if (!form.address.trim()) newErrors.address = "Address is required.";

    if (!form.specialization.trim()) newErrors.specialization = "Specialization is required.";

    if (form.yearsOfExperience === "" || form.yearsOfExperience < 0) {
      newErrors.yearsOfExperience = "Years of experience must be 0 or more.";
    } else if (form.yearsOfExperience > 50) {
      newErrors.yearsOfExperience = "Years of experience cannot exceed 50.";
    }

    if (form.certifications.length === 0) {
      newErrors.certifications = "At least one certification is required.";
    } else {
      const hasInvalidCert = form.certifications.some(
        cert => !cert.certificationName.trim() || !cert.issuingAuthority.trim() || !cert.file
      );
      if (hasInvalidCert) {
        newErrors.certifications = "All certification fields must be completed.";
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  setApiError("");
  
  if (!validate()) return;

  setLoading(true);
  try {
    const formData = new FormData();
    
    // Append basic fields
    formData.append("firstName", form.firstName);
    formData.append("lastName", form.lastName);
    formData.append("email", form.email);
    formData.append("phoneNumber", form.phoneNumber.replace(/[\s-]/g, ''));
    formData.append("password", form.password);
    formData.append("address", form.address);
    formData.append("specialization", form.specialization);
    formData.append("yearsOfExperience", String(form.yearsOfExperience));

    // IMPORTANT: Match your backend's expected format
    // Your backend expects a List<CertificationDto> with MultipartFile
    form.certifications.forEach((cert, index) => {
      if (cert.file) {
        // Append certification details as part of the certifications array
        // The backend expects these fields in the CertificationDto
        formData.append(`certifications[${index}].certificationName`, cert.certificationName);
        formData.append(`certifications[${index}].issuingAuthority`, cert.issuingAuthority);
        formData.append(`certifications[${index}].file`, cert.file);
      }
    });

    // Debug: Log FormData contents
    console.log("Submitting form data:");
    for (let pair of formData.entries()) {
      console.log(pair[0], pair[1]);
    }

    const response = await fetch("http://localhost:8080/auth/technician/register", {
      method: "POST",
      body: formData,
      // Don't set Content-Type header - browser will set it with boundary
    });

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.error || data.message || "Registration failed");
    }

    setRegistrationSuccess(true);
    setTimeout(() => {
      router.push("/auth/technician/login?pending=true");
    }, 3000);
  } catch (err: any) {
    console.error("Registration error:", err);
    setApiError(err.message || "Registration failed. Please try again.");
  } finally {
    setLoading(false);
  }
};

  if (registrationSuccess) {
    return (
      <AuthLayout>
        <div className="auth-form-container">
          <div className="success-state">
            <div className="success-icon">
              <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#10b981" strokeWidth="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                <polyline points="22 4 12 14.01 9 11.01"/>
              </svg>
            </div>
            <h2>Registration Submitted!</h2>
            <p className="success-message">
              Your technician account has been created and is pending admin approval.
              You'll receive an email once your certifications are verified.
            </p>
            <p className="redirect-note">
              Redirecting to login page...
            </p>
          </div>
        </div>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout>
      <div className="auth-form-container technician-registration">
        <h1>Register as Technician</h1>
        <p className="subtitle">Create your account and upload certifications for verification.</p>

        <RoleTabs activeRole="tech" mode="register" />

        {apiError && (
          <div className="alert alert-error">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            {apiError}
          </div>
        )}

        <form onSubmit={handleSubmit} noValidate className="technician-form">
          {/* Personal Information Section */}
          <div className="form-section">
            <h3>Personal Information</h3>
            
            <div className="row">
              <div className="field">
                <label htmlFor="firstName">First Name *</label>
                <div className="input-wrapper">
                  <span className="input-icon"><UserIcon /></span>
                  <input
                    id="firstName"
                    type="text"
                    placeholder="e.g. John"
                    value={form.firstName}
                    onChange={update("firstName")}
                    className={errors.firstName ? "error" : ""}
                  />
                </div>
                {errors.firstName && <p className="field-error">{errors.firstName}</p>}
              </div>

              <div className="field">
                <label htmlFor="lastName">Last Name *</label>
                <div className="input-wrapper">
                  <span className="input-icon"><UserIcon /></span>
                  <input
                    id="lastName"
                    type="text"
                    placeholder="e.g. Doe"
                    value={form.lastName}
                    onChange={update("lastName")}
                    className={errors.lastName ? "error" : ""}
                  />
                </div>
                {errors.lastName && <p className="field-error">{errors.lastName}</p>}
              </div>
            </div>

            <div className="row">
              <div className="field">
                <label htmlFor="email">Email *</label>
                <div className="input-wrapper">
                  <span className="input-icon"><MailIcon /></span>
                  <input
                    id="email"
                    type="email"
                    placeholder="e.g. john.doe@example.com"
                    value={form.email}
                    onChange={update("email")}
                    className={errors.email ? "error" : ""}
                    autoComplete="email"
                  />
                </div>
                {errors.email && <p className="field-error">{errors.email}</p>}
              </div>

              <div className="field">
                <label htmlFor="phoneNumber">Phone Number *</label>
                <div className="input-wrapper">
                  <span className="input-icon"><PhoneIcon /></span>
                  <input
                    id="phoneNumber"
                    type="tel"
                    placeholder="e.g. 1234567890"
                    value={form.phoneNumber}
                    onChange={update("phoneNumber")}
                    className={errors.phoneNumber ? "error" : ""}
                  />
                </div>
                {errors.phoneNumber && <p className="field-error">{errors.phoneNumber}</p>}
              </div>
            </div>
          </div>

          {/* Account Security Section */}
          <div className="form-section">
            <h3>Account Security</h3>
            
            <div className="row">
              <div className="field">
                <label htmlFor="password">Password *</label>
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

              <div className="field">
                <label htmlFor="confirmPassword">Confirm Password *</label>
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
            </div>
          </div>

          {/* Professional Information Section */}
          <div className="form-section">
            <h3>Professional Information</h3>
            
            <div className="field">
              <label htmlFor="address">Address *</label>
              <div className="input-wrapper">
                <span className="input-icon"><MapPinIcon /></span>
                <input
                  id="address"
                  type="text"
                  placeholder="e.g. 123 Main St, City"
                  value={form.address}
                  onChange={update("address")}
                  className={errors.address ? "error" : ""}
                />
              </div>
              {errors.address && <p className="field-error">{errors.address}</p>}
            </div>

            <div className="row">
              <div className="field">
                <label htmlFor="specialization">Specialization *</label>
                <div className="input-wrapper">
                  <span className="input-icon"><BriefcaseIcon /></span>
                  <input
                    id="specialization"
                    type="text"
                    placeholder="e.g. HVAC, Electrical, Plumbing"
                    value={form.specialization}
                    onChange={update("specialization")}
                    className={errors.specialization ? "error" : ""}
                  />
                </div>
                {errors.specialization && <p className="field-error">{errors.specialization}</p>}
              </div>

              <div className="field">
                <label htmlFor="yearsOfExperience">Years of Experience *</label>
                <div className="input-wrapper">
                  <span className="input-icon"><AwardIcon /></span>
                  <input
                    id="yearsOfExperience"
                    type="number"
                    min="0"
                    max="50"
                    placeholder="e.g. 5"
                    value={form.yearsOfExperience}
                    onChange={update("yearsOfExperience")}
                    className={errors.yearsOfExperience ? "error" : ""}
                  />
                </div>
                {errors.yearsOfExperience && <p className="field-error">{errors.yearsOfExperience}</p>}
              </div>
            </div>
          </div>

          {/* Certifications Section */}
          <div className="form-section">
            <h3>Certifications *</h3>
            <p className="section-hint">Upload your professional certifications (PDF, JPG, PNG, max 5MB each)</p>
            
            <CertificationUpload
              certifications={form.certifications}
              onChange={handleCertificationsChange}
              error={errors.certifications}
            />
          </div>

          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? (
              <><div className="spinner" /> Submitting Application...</>
            ) : (
              <>Submit for Review <span>→</span></>
            )}
          </button>
        </form>

        <p className="auth-switch">
          Already have an account?{" "}
          <Link href="/auth/login?role=technician">Sign In →</Link>
        </p>
      </div>
    </AuthLayout>
  );
}