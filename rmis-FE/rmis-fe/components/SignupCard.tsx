"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";

interface CertificationFile {
  certificationName: string;
  issuingAuthority: string;
  file: File | null;
}

export default function SignupCard() {
  const router = useRouter();
  const [role, setRole] = useState("Technician");
  const [showPassword, setShowPassword] = useState(false);
  const [agreeTerms, setAgreeTerms] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [certifications, setCertifications] = useState<CertificationFile[]>([
    { certificationName: "", issuingAuthority: "", file: null }
  ]);

  // Form state
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    phoneNumber: "",
    address: "",
    specialization: "",
    yearsOfExperience: "",
    password: "",
    confirmPassword: "",
    province: "",
    district: ""
  });

  const roles = ["Public User", "Technician", "Company"];

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    // Clear error for this field
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: "" }));
    }
  };

  const handleCertificationChange = (index: number, field: string, value: string) => {
    const updated = [...certifications];
    updated[index] = { ...updated[index], [field]: value };
    setCertifications(updated);
  };

  const handleFileChange = (index: number, file: File | null) => {
    if (!file) return;

    // Validate file type
    const allowedTypes = ['application/pdf', 'image/jpeg', 'image/png', 'image/jpg'];
    if (!allowedTypes.includes(file.type)) {
      setErrors(prev => ({ 
        ...prev, 
        [`cert_file_${index}`]: "Invalid file format. Only PDF, JPG, or PNG allowed." 
      }));
      return;
    }

    // Validate file size (max 5MB from backend)
    if (file.size > 5 * 1024 * 1024) {
      setErrors(prev => ({ 
        ...prev, 
        [`cert_file_${index}`]: "File size exceeds 5MB limit." 
      }));
      return;
    }

    const updated = [...certifications];
    updated[index] = { ...updated[index], file };
    setCertifications(updated);
    setErrors(prev => ({ ...prev, [`cert_file_${index}`]: "" }));
  };

  const addCertification = () => {
    setCertifications([...certifications, { certificationName: "", issuingAuthority: "", file: null }]);
  };

  const removeCertification = (index: number) => {
    if (certifications.length > 1) {
      setCertifications(certifications.filter((_, i) => i !== index));
    }
  };

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    // Required fields validation
    if (!formData.firstName) newErrors.firstName = "First name is required";
    if (!formData.lastName) newErrors.lastName = "Last name is required";
    if (!formData.email) newErrors.email = "Email is required";
    else if (!/\S+@\S+\.\S+/.test(formData.email)) newErrors.email = "Invalid email format";
    
    if (!formData.phoneNumber) newErrors.phoneNumber = "Phone number is required";
    else if (!/^\d{10,15}$/.test(formData.phoneNumber)) {
      newErrors.phoneNumber = "Phone number must be 10-15 digits";
    }
    
    if (!formData.password) newErrors.password = "Password is required";
    else if (formData.password.length < 6) {
      newErrors.password = "Password must be at least 6 characters";
    }
    
    if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = "Passwords do not match";
    }

    // Validate certifications
    certifications.forEach((cert, index) => {
      if (!cert.certificationName) {
        newErrors[`cert_name_${index}`] = "Certification name is required";
      }
      if (!cert.issuingAuthority) {
        newErrors[`cert_authority_${index}`] = "Issuing authority is required";
      }
      if (!cert.file) {
        newErrors[`cert_file_${index}`] = "Certification file is required";
      }
    });

    if (!agreeTerms) {
      newErrors.terms = "You must agree to the terms";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) return;
    
    setIsLoading(true);
    
    try {
      // Create FormData object for multipart/form-data
      const submitData = new FormData();
      
      // Append basic info
      submitData.append("firstName", formData.firstName);
      submitData.append("lastName", formData.lastName);
      submitData.append("email", formData.email);
      submitData.append("phoneNumber", formData.phoneNumber);
      submitData.append("password", formData.password);
      submitData.append("address", formData.address || "");
      submitData.append("specialization", formData.specialization || "");
      submitData.append("yearsOfExperience", formData.yearsOfExperience || "0");
      
      // Append certifications
      certifications.forEach((cert, index) => {
        submitData.append(`certifications[${index}].certificationName`, cert.certificationName);
        submitData.append(`certifications[${index}].issuingAuthority`, cert.issuingAuthority);
        if (cert.file) {
          submitData.append(`certifications[${index}].file`, cert.file);
        }
      });

      const response = await fetch("http://localhost:8080/auth/technician/register", {
        method: "POST",
        body: submitData,
        // Don't set Content-Type header - browser will set it with boundary
      });

      const data = await response.json();

      if (response.ok) {
        // Registration successful
        router.push("/auth/technician/registration/success?email=" + encodeURIComponent(formData.email));
      } else {
        // Handle error
        setErrors({ submit: data.error || "Registration failed. Please try again." });
      }
    } catch (error) {
      setErrors({ submit: "Network error. Please check your connection." });
    } finally {
      setIsLoading(false);
    }
  };

  const renderHeader = () => {
    switch (role) {
      case "Company":
        return (
          <>
            <h2 className="text-3xl font-black text-gray-900 leading-tight">Company Sign Up</h2>
            <p className="text-gray-500 mt-2 mb-7 text-sm">Create a corporate account to manage your environmental compliance.</p>
          </>
        );
      case "Technician":
        return (
          <>
            <h2 className="text-3xl font-black text-gray-900 leading-tight">Technician Registration</h2>
            <p className="text-gray-500 mt-2 mb-7 text-sm">Complete the form below to create your professional account.</p>
          </>
        );
      default:
        return (
          <>
            <h2 className="text-3xl font-black text-gray-900 leading-tight">Create Your Account</h2>
            <p className="text-gray-500 mt-2 mb-7 text-sm">Join our platform and help protect the environment.</p>
          </>
        );
    }
  };

  const renderFields = () => {
    if (role === "Company") {
      return (
        <>
          {/* Company fields - as before */}
        </>
      );
    }

    if (role === "Technician") {
      return (
        <>
          {/* Name Row */}
          <div className="flex gap-4 mb-4">
            <div className="flex-1">
              <label className="block text-sm font-semibold text-gray-700 mb-1.5">First Name</label>
              <div className="relative">
                <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-4.5 w-4.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                </span>
                <input
                  type="text"
                  name="firstName"
                  value={formData.firstName}
                  onChange={handleInputChange}
                  placeholder="John"
                  className={`w-full rounded-xl border ${errors.firstName ? 'border-red-500' : 'border-gray-200'} bg-gray-50 pl-11 pr-4 py-3.5 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none transition placeholder-gray-400`}
                />
              </div>
              {errors.firstName && <p className="text-red-500 text-xs mt-1">{errors.firstName}</p>}
            </div>
            <div className="flex-1">
              <label className="block text-sm font-semibold text-gray-700 mb-1.5">Last Name</label>
              <div className="relative">
                <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-4.5 w-4.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                </span>
                <input
                  type="text"
                  name="lastName"
                  value={formData.lastName}
                  onChange={handleInputChange}
                  placeholder="Doe"
                  className={`w-full rounded-xl border ${errors.lastName ? 'border-red-500' : 'border-gray-200'} bg-gray-50 pl-11 pr-4 py-3.5 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none transition placeholder-gray-400`}
                />
              </div>
              {errors.lastName && <p className="text-red-500 text-xs mt-1">{errors.lastName}</p>}
            </div>
          </div>

          {/* Email */}
          <div className="mb-4">
            <label className="block text-sm font-semibold text-gray-700 mb-1.5">Email Address</label>
            <div className="relative">
              <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-4.5 w-4.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                </svg>
              </span>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleInputChange}
                placeholder="john.doe@tech-env.com"
                className={`w-full rounded-xl border ${errors.email ? 'border-red-500' : 'border-gray-200'} bg-gray-50 pl-11 pr-4 py-3.5 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none transition placeholder-gray-400`}
              />
            </div>
            {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email}</p>}
          </div>

          {/* Phone Number */}
          <div className="mb-4">
            <label className="block text-sm font-semibold text-gray-700 mb-1.5">Phone Number</label>
            <div className="relative">
              <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-4.5 w-4.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                </svg>
              </span>
              <input
                type="tel"
                name="phoneNumber"
                value={formData.phoneNumber}
                onChange={handleInputChange}
                placeholder="+1234567890"
                className={`w-full rounded-xl border ${errors.phoneNumber ? 'border-red-500' : 'border-gray-200'} bg-gray-50 pl-11 pr-4 py-3.5 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none transition placeholder-gray-400`}
              />
            </div>
            {errors.phoneNumber && <p className="text-red-500 text-xs mt-1">{errors.phoneNumber}</p>}
          </div>

          {/* Address */}
          <div className="mb-4">
            <label className="block text-sm font-semibold text-gray-700 mb-1.5">Address</label>
            <input
              type="text"
              name="address"
              value={formData.address}
              onChange={handleInputChange}
              placeholder="123 Main St, City"
              className="w-full rounded-xl border border-gray-200 bg-gray-50 px-4 py-3.5 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none transition placeholder-gray-400"
            />
          </div>

          {/* Specialization */}
          <div className="mb-4">
            <label className="block text-sm font-semibold text-gray-700 mb-1.5">Specialization</label>
            <input
              type="text"
              name="specialization"
              value={formData.specialization}
              onChange={handleInputChange}
              placeholder="e.g., HVAC, Electrical, Plumbing"
              className="w-full rounded-xl border border-gray-200 bg-gray-50 px-4 py-3.5 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none transition placeholder-gray-400"
            />
          </div>

          {/* Years of Experience */}
          <div className="mb-4">
            <label className="block text-sm font-semibold text-gray-700 mb-1.5">Years of Experience</label>
            <input
              type="number"
              name="yearsOfExperience"
              value={formData.yearsOfExperience}
              onChange={handleInputChange}
              min="0"
              max="50"
              placeholder="5"
              className="w-full rounded-xl border border-gray-200 bg-gray-50 px-4 py-3.5 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none transition placeholder-gray-400"
            />
          </div>

          {/* Certifications */}
          <div className="mb-6">
            <label className="block text-sm font-semibold text-gray-700 mb-3">Professional Certifications</label>
            
            {certifications.map((cert, index) => (
              <div key={index} className="mb-6 p-4 border border-gray-100 rounded-2xl bg-gray-50/50">
                <div className="flex justify-between items-center mb-3">
                  <span className="text-sm font-medium text-gray-600">Certification #{index + 1}</span>
                  {certifications.length > 1 && (
                    <button
                      type="button"
                      onClick={() => removeCertification(index)}
                      className="text-red-500 hover:text-red-600 text-sm"
                    >
                      Remove
                    </button>
                  )}
                </div>

                {/* Certification Name */}
                <div className="mb-3">
                  <input
                    type="text"
                    placeholder="Certification Name"
                    value={cert.certificationName}
                    onChange={(e) => handleCertificationChange(index, "certificationName", e.target.value)}
                    className={`w-full rounded-xl border ${errors[`cert_name_${index}`] ? 'border-red-500' : 'border-gray-200'} bg-white px-4 py-3 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none transition`}
                  />
                  {errors[`cert_name_${index}`] && <p className="text-red-500 text-xs mt-1">{errors[`cert_name_${index}`]}</p>}
                </div>

                {/* Issuing Authority */}
                <div className="mb-3">
                  <input
                    type="text"
                    placeholder="Issuing Authority"
                    value={cert.issuingAuthority}
                    onChange={(e) => handleCertificationChange(index, "issuingAuthority", e.target.value)}
                    className={`w-full rounded-xl border ${errors[`cert_authority_${index}`] ? 'border-red-500' : 'border-gray-200'} bg-white px-4 py-3 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none transition`}
                  />
                  {errors[`cert_authority_${index}`] && <p className="text-red-500 text-xs mt-1">{errors[`cert_authority_${index}`]}</p>}
                </div>

                {/* File Upload */}
                <div>
                  <div className={`border-2 border-dashed rounded-xl p-4 transition-all ${errors[`cert_file_${index}`] ? 'border-red-300 bg-red-50/30' : 'border-gray-200 hover:border-emerald-300 bg-white hover:bg-emerald-50/20'}`}>
                    <input
                      type="file"
                      id={`file-${index}`}
                      className="hidden"
                      accept=".pdf,.jpg,.jpeg,.png"
                      onChange={(e) => handleFileChange(index, e.target.files?.[0] || null)}
                    />
                    <label htmlFor={`file-${index}`} className="cursor-pointer block text-center">
                      {cert.file ? (
                        <div className="flex items-center justify-center gap-2 text-emerald-600">
                          <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                          </svg>
                          <span className="text-sm font-medium">{cert.file.name}</span>
                        </div>
                      ) : (
                        <div className="flex flex-col items-center">
                          <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-gray-400 mb-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 13h6m-3-3v6m5 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                          </svg>
                          <p className="text-sm text-gray-600">Click to upload PDF, JPG or PNG (max 5MB)</p>
                        </div>
                      )}
                    </label>
                  </div>
                  {errors[`cert_file_${index}`] && <p className="text-red-500 text-xs mt-1">{errors[`cert_file_${index}`]}</p>}
                </div>
              </div>
            ))}

            <button
              type="button"
              onClick={addCertification}
              className="mt-2 text-emerald-600 hover:text-emerald-700 text-sm font-medium flex items-center gap-1"
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
              </svg>
              Add Another Certification
            </button>
          </div>
        </>
      );
    }

    // Default: Public User
    return (
      <>
        {/* Public user fields - as before */}
      </>
    );
  };

  return (
    <form onSubmit={handleSubmit} className="bg-white rounded-3xl shadow-2xl w-[600px] p-10 py-8 max-h-[90vh] overflow-y-auto">
      {renderHeader()}

      {/* Role Tabs */}
      <div className="flex bg-gray-100 rounded-xl p-1 mb-6 gap-1">
        {roles.map((item) => (
          <button
            key={item}
            type="button"
            onClick={() => setRole(item)}
            className={`flex-1 py-1.5 px-3 rounded-lg text-sm font-medium transition-all duration-200 ${
              role === item
                ? "bg-white shadow text-gray-900"
                : "text-gray-500 hover:text-gray-700"
            }`}
          >
            {item}
          </button>
        ))}
      </div>

      {errors.submit && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-xl">
          <p className="text-red-600 text-sm">{errors.submit}</p>
        </div>
      )}

      {renderFields()}

      {/* Password Group */}
      <div className="flex gap-4 mb-4">
        <div className="flex-1">
          <label className="block text-sm font-semibold text-gray-700 mb-1.5">Password</label>
          <div className="relative">
            <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-4.5 w-4.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
              </svg>
            </span>
            <input
              type={showPassword ? "text" : "password"}
              name="password"
              value={formData.password}
              onChange={handleInputChange}
              placeholder="••••••••"
              className={`w-full rounded-xl border ${errors.password ? 'border-red-500' : 'border-gray-200'} bg-gray-50 pl-11 pr-4 py-3.5 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none transition placeholder-gray-400`}
            />
          </div>
          {errors.password && <p className="text-red-500 text-xs mt-1">{errors.password}</p>}
        </div>
        <div className="flex-1">
          <label className="block text-sm font-semibold text-gray-700 mb-1.5">Confirm Password</label>
          <div className="relative">
            <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-4.5 w-4.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
              </svg>
            </span>
            <input
              type="password"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleInputChange}
              placeholder="••••••••"
              className={`w-full rounded-xl border ${errors.confirmPassword ? 'border-red-500' : 'border-gray-200'} bg-gray-50 pl-11 pr-4 py-3.5 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none transition placeholder-gray-400`}
            />
          </div>
          {errors.confirmPassword && <p className="text-red-500 text-xs mt-1">{errors.confirmPassword}</p>}
        </div>
      </div>

      {/* Show Password Toggle */}
      <div className="mb-6">
        <button
          type="button"
          onClick={() => setShowPassword(!showPassword)}
          className="text-sm text-gray-500 hover:text-gray-700 flex items-center gap-1"
        >
          <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
          </svg>
          {showPassword ? "Hide" : "Show"} password
        </button>
      </div>

      {/* Terms Checkbox */}
      <div className="flex items-start gap-3 mb-6">
        <button
          type="button"
          onClick={() => setAgreeTerms(!agreeTerms)}
          className={`h-5 w-5 rounded border-2 flex items-center justify-center transition-all flex-shrink-0 mt-0.5 ${
            agreeTerms
              ? "bg-emerald-600 border-emerald-600"
              : errors.terms
                ? "border-red-500 bg-white"
                : "border-gray-300 bg-white"
          }`}
        >
          {agreeTerms && (
            <svg xmlns="http://www.w3.org/2000/svg" className="h-3.5 w-3.5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={3}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
            </svg>
          )}
        </button>
        <p className="text-xs text-gray-500 leading-relaxed cursor-pointer select-none" onClick={() => setAgreeTerms(!agreeTerms)}>
          I agree to the{" "}
          <Link href="#" className="text-emerald-500 font-semibold hover:text-emerald-600 transition">Terms of Service</Link>{" "}
          and{" "}
          <Link href="#" className="text-emerald-500 font-semibold hover:text-emerald-600 transition">Professional Code of Conduct</Link>
        </p>
      </div>
      {errors.terms && <p className="text-red-500 text-xs -mt-4 mb-4">{errors.terms}</p>}

      <button
        type="submit"
        disabled={isLoading}
        className={`w-full bg-[#10d354] hover:bg-[#0ebf4c] text-white py-4 rounded-xl text-base font-bold shadow-lg shadow-emerald-100 transition-all duration-200 flex items-center justify-center gap-2 mb-6 ${
          isLoading ? 'opacity-50 cursor-not-allowed' : 'active:scale-[0.98]'
        }`}
      >
        {isLoading ? (
          <>
            <svg className="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            Creating Account...
          </>
        ) : (
          <>
            Create Technician Account
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
            </svg>
          </>
        )}
      </button>

      <p className="text-center text-sm text-gray-500 pb-2">
        Already have an account?{" "}
        <Link href="/" className="text-emerald-500 font-bold hover:text-emerald-600 transition inline-flex items-center gap-1">
          Sign In →
        </Link>
      </p>
    </form>
  );
}