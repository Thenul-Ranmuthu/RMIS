"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";

export default function RegistrationSuccessPage() {
  const searchParams = useSearchParams();
  const email = searchParams.get("email");

  return (
    <main className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 to-teal-50">
      <div className="bg-white rounded-3xl shadow-2xl w-[500px] p-12 text-center">
        <div className="w-20 h-20 bg-emerald-100 rounded-full flex items-center justify-center mx-auto mb-6">
          <svg xmlns="http://www.w3.org/2000/svg" className="h-10 w-10 text-emerald-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
          </svg>
        </div>
        
        <h1 className="text-3xl font-black text-gray-900 mb-4">Registration Submitted!</h1>
        
        <div className="bg-emerald-50 rounded-xl p-4 mb-6">
          <p className="text-emerald-800 text-sm">
            A verification email has been sent to <span className="font-bold">{email}</span>
          </p>
        </div>
        
        <p className="text-gray-600 mb-8">
          Your technician account is now <span className="font-semibold text-amber-600">Pending Approval</span>. 
          We'll notify you once an admin reviews and verifies your certifications.
        </p>
        
        <div className="space-y-3">
          <Link
            href="/"
            className="block w-full bg-emerald-600 hover:bg-emerald-700 text-white py-4 rounded-xl text-base font-bold shadow-lg shadow-emerald-100 transition-all duration-200"
          >
            Return to Home
          </Link>
          
          <p className="text-sm text-gray-500">
            Need help? <Link href="/contact" className="text-emerald-600 font-semibold hover:underline">Contact Support</Link>
          </p>
        </div>
      </div>
    </main>
  );
}