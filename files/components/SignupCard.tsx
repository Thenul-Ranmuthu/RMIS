"use client";

import { useState } from "react";
import Link from "next/link";

export default function SignupCard() {
    const [role, setRole] = useState("Technician"); // Default to Technician as per user request
    const [showPassword, setShowPassword] = useState(false);
    const [agreeTerms, setAgreeTerms] = useState(false);

    const roles = ["Public User", "Technician", "Company"];

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
                    {/* Company Name */}
                    <div className="mb-4">
                        <label className="block text-sm font-semibold text-gray-700 mb-1.5">Company Name</label>
                        <div className="relative">
                            <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400">
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-4.5 w-4.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                                </svg>
                            </span>
                            <input
                                type="text"
                                placeholder="Legal business name"
                                className="w-full rounded-xl border border-gray-200 bg-gray-50 pl-11 pr-4 py-3.5 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none transition placeholder-gray-400"
                            />
                        </div>
                    </div>

                    {/* Company Registration Number */}
                    <div className="mb-4">
                        <label className="block text-sm font-semibold text-gray-700 mb-1.5">Company Registration Number</label>
                        <div className="relative">
                            <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400">
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-4.5 w-4.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                                </svg>
                            </span>
                            <input
                                type="text"
                                placeholder="CRN-0000-0000"
                                className="w-full rounded-xl border border-gray-200 bg-gray-50 pl-11 pr-4 py-3.5 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none transition placeholder-gray-400"
                            />
                        </div>
                    </div>

                    {/* Business Email Address */}
                    <div className="mb-4">
                        <label className="block text-sm font-semibold text-gray-700 mb-1.5">Business Email Address</label>
                        <div className="relative">
                            <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400">
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-4.5 w-4.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                                </svg>
                            </span>
                            <input
                                type="email"
                                placeholder="contact@company.com"
                                className="w-full rounded-xl border border-gray-200 bg-gray-50 pl-11 pr-4 py-3.5 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none transition placeholder-gray-400"
                            />
                        </div>
                    </div>
                </>
            );
        }

        if (role === "Technician") {
            return (
                <>
                    {/* Full Name */}
                    <div className="mb-4">
                        <label className="block text-sm font-semibold text-gray-700 mb-1.5">Full Name</label>
                        <div className="relative">
                            <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400">
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-4.5 w-4.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                                </svg>
                            </span>
                            <input
                                type="text"
                                placeholder="Johnathan Doe"
                                className="w-full rounded-xl border border-gray-200 bg-gray-50 pl-11 pr-4 py-3.5 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none transition placeholder-gray-400"
                            />
                        </div>
                    </div>

                    {/* Email Address */}
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
                                placeholder="john.doe@tech-env.com"
                                className="w-full rounded-xl border border-gray-200 bg-gray-50 pl-11 pr-4 py-3.5 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none transition placeholder-gray-400"
                            />
                        </div>
                    </div>

                    {/* Province & District Row */}
                    <div className="flex gap-4 mb-4">
                        <div className="flex-1">
                            <label className="block text-sm font-semibold text-gray-700 mb-1.5">Province</label>
                            <div className="relative">
                                <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400">
                                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4.5 w-4.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 20l-5.447-2.724A2 2 0 013 15.488V5.512a2 2 0 011.553-1.954L9 1l6 3 6-3 4.447 2.224a2 2 0 011.553 1.954v9.976a2 2 0 01-1.553 1.954L15 19l-6 1z" />
                                    </svg>
                                </span>
                                <select className="w-full rounded-xl border border-gray-200 bg-gray-50 pl-11 pr-10 py-3.5 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none transition appearance-none text-gray-500">
                                    <option>Select Province</option>
                                </select>
                                <span className="absolute right-3.5 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none">
                                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                                    </svg>
                                </span>
                            </div>
                        </div>
                        <div className="flex-1">
                            <label className="block text-sm font-semibold text-gray-700 mb-1.5">District</label>
                            <div className="relative">
                                <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400">
                                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4.5 w-4.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                                    </svg>
                                </span>
                                <select className="w-full rounded-xl border border-gray-200 bg-gray-50 pl-11 pr-10 py-3.5 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none transition appearance-none text-gray-500">
                                    <option>Select District</option>
                                </select>
                                <span className="absolute right-3.5 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none">
                                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                                    </svg>
                                </span>
                            </div>
                        </div>
                    </div>

                    {/* Professional Certifications Upload */}
                    <div className="mb-4">
                        <label className="block text-sm font-semibold text-gray-700 mb-1.5">Professional Certifications</label>
                        <div className="border-2 border-dashed border-gray-200 rounded-2xl p-6 flex flex-col items-center justify-center bg-gray-50/50 hover:bg-gray-50 transition-all cursor-pointer">
                            <div className="h-10 w-10 bg-white rounded-xl shadow-sm flex items-center justify-center mb-3">
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 13h6m-3-3v6m5 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                                </svg>
                            </div>
                            <p className="text-sm font-semibold text-gray-700">Click to upload or drag and drop</p>
                            <p className="text-xs text-gray-400 mt-1">PDF, PNG or JPG (max. 10MB)</p>
                        </div>
                    </div>
                </>
            );
        }

        // Default: Public User
        return (
            <>
                {/* Full Name */}
                <div className="mb-4">
                    <label className="block text-sm font-semibold text-gray-700 mb-1.5">Full Name</label>
                    <div className="relative">
                        <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400">
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-4.5 w-4.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                            </svg>
                        </span>
                        <input
                            type="text"
                            placeholder="e.g. John Doe"
                            className="w-full rounded-xl border border-gray-200 bg-gray-50 pl-11 pr-4 py-3.5 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none transition placeholder-gray-400"
                        />
                    </div>
                </div>

                {/* Email */}
                <div className="mb-4">
                    <label className="block text-sm font-semibold text-gray-700 mb-1.5">Email address</label>
                    <div className="relative">
                        <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400">
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-4.5 w-4.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 00-2 2z" />
                            </svg>
                        </span>
                        <input
                            type="email"
                            placeholder="e.g. john.doe@example.com"
                            className="w-full rounded-xl border border-gray-200 bg-gray-50 pl-11 pr-4 py-3.5 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none transition placeholder-gray-400"
                        />
                    </div>
                </div>
            </>
        );
    };

    const renderFooterLinks = () => {
        switch (role) {
            case "Technician":
                return (
                    <p className="text-xs text-gray-500 leading-relaxed cursor-pointer select-none" onClick={() => setAgreeTerms(!agreeTerms)}>
                        I agree to the{" "}
                        <Link href="#" className="text-emerald-500 font-semibold hover:text-emerald-600 transition">Terms of Service</Link>{" "}
                        and{" "}
                        <Link href="#" className="text-emerald-500 font-semibold hover:text-emerald-600 transition">Professional Code of Conduct</Link>
                    </p>
                );
            default:
                return (
                    <p className="text-xs text-gray-500 leading-relaxed cursor-pointer select-none" onClick={() => setAgreeTerms(!agreeTerms)}>
                        By signing up, I agree to the Ministry's{" "}
                        <Link href="#" className="text-emerald-500 font-semibold hover:text-emerald-600 transition underline decoration-emerald-200 underline-offset-2">Terms of Service</Link>{" "}
                        and{" "}
                        <Link href="#" className="text-emerald-500 font-semibold hover:text-emerald-600 transition underline decoration-emerald-200 underline-offset-2">Data Protection Policy</Link>.
                    </p>
                );
        }
    };

    return (
        <div className="bg-white rounded-3xl shadow-2xl w-[480px] p-10 py-8">
            {renderHeader()}

            {/* Role Tabs */}
            <div className="flex bg-gray-100 rounded-xl p-1 mb-6 gap-1">
                {roles.map((item) => (
                    <button
                        key={item}
                        onClick={() => setRole(item)}
                        className={`flex-1 py-1.5 px-3 rounded-lg text-sm font-medium transition-all duration-200 ${role === item
                            ? "bg-white shadow text-gray-900"
                            : "text-gray-500 hover:text-gray-700"
                            }`}
                    >
                        {item}
                    </button>
                ))}
            </div>

            <div className="overflow-y-auto max-h-[60vh] pr-1 -mr-1 scrollbar-hide">
                {renderFields()}

                {/* Password Group */}
                <div className={role === "Technician" ? "flex gap-4 mb-4" : "mb-4"}>
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
                                placeholder="••••••••"
                                className="w-full rounded-xl border border-gray-200 bg-gray-50 pl-11 pr-4 py-3.5 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none transition placeholder-gray-400"
                            />
                        </div>
                    </div>
                    {role === "Technician" && (
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
                                    placeholder="••••••••"
                                    className="w-full rounded-xl border border-gray-200 bg-gray-50 pl-11 pr-4 py-3.5 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none transition placeholder-gray-400"
                                />
                            </div>
                        </div>
                    )}
                </div>

                {role !== "Technician" && (
                    <div className="mb-6">
                        <label className="block text-sm font-semibold text-gray-700 mb-1.5">Confirm Password</label>
                        <div className="relative">
                            <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400">
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-4.5 w-4.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                                </svg>
                            </span>
                            <input
                                type="password"
                                placeholder="••••••••"
                                className="w-full rounded-xl border border-gray-200 bg-gray-50 pl-11 pr-4 py-3.5 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none transition placeholder-gray-400"
                            />
                        </div>
                    </div>
                )}

                {/* Terms Checkbox */}
                <div className="flex items-start gap-3 mb-6">
                    <button
                        type="button"
                        onClick={() => setAgreeTerms(!agreeTerms)}
                        className={`h-5 w-5 rounded border-2 flex items-center justify-center transition-all flex-shrink-0 mt-0.5 ${agreeTerms
                            ? "bg-emerald-600 border-emerald-600"
                            : "border-gray-300 bg-white"
                            }`}
                    >
                        {agreeTerms && (
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-3.5 w-3.5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={3}>
                                <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                            </svg>
                        )}
                    </button>
                    {renderFooterLinks()}
                </div>

                <button className="w-full bg-[#10d354] hover:bg-[#0ebf4c] active:scale-[0.98] text-white py-4 rounded-xl text-base font-bold shadow-lg shadow-emerald-100 transition-all duration-200 flex items-center justify-center gap-2 mb-6">
                    {role === "Technician" ? "Create Technician Account" : role === "Company" ? "Create Company Account" : "Create Account"}
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
                    </svg>
                </button>

                <p className="text-center text-sm text-gray-500 pb-2">
                    Already have an account?{" "}
                    <Link href="/" className="text-emerald-500 font-bold hover:text-emerald-600 transition inline-flex items-center gap-1">
                        Sign In →
                    </Link>
                </p>
            </div>
        </div>
    );
}
