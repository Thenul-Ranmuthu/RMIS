"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

export default function PublicUserDashboard() {
    const router = useRouter();
    const [user, setUser] = useState<{ email: string; role: string } | null>(null);

    useEffect(() => {
        const userData = localStorage.getItem('user') || sessionStorage.getItem('user');
        if (!userData) {
            router.push('/');
            return;
        }
        setUser(JSON.parse(userData));
    }, []);

    const handleSignOut = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        sessionStorage.removeItem('token');
        sessionStorage.removeItem('user');
        router.push('/');
    };

    return (
        <main
            className="min-h-screen flex items-center justify-center p-8"
            style={{
                backgroundImage: "url('/backgrounds/public-bg.jpg')",
                backgroundSize: "cover",
                backgroundPosition: "center",
                backgroundRepeat: "no-repeat",
            }}
        >
            <div className="absolute inset-0 bg-black/40" />

            <div className="relative bg-white/90 backdrop-blur-sm p-10 rounded-2xl shadow-xl max-w-md w-full text-center">
                <div className="flex justify-center mb-4">
                    <div className="bg-emerald-100 rounded-full p-4">
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-emerald-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                        </svg>
                    </div>
                </div>
                <h1 className="text-2xl font-black text-gray-900 mb-1">Welcome Back!</h1>
                <p className="text-sm text-gray-500 mb-1">You are logged in as a Public User</p>
                {user && (
                    <p className="text-sm font-semibold text-emerald-600 mb-6">{user.email}</p>
                )}
                <p className="text-gray-600 text-sm mb-8">
                    Browse and report environmental issues in your area.
                </p>
                <button
                    onClick={handleSignOut}
                    className="w-full bg-red-500 hover:bg-red-600 text-white px-6 py-3 rounded-xl font-semibold transition flex items-center justify-center gap-2"
                >
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                    </svg>
                    Sign Out
                </button>
            </div>
        </main>
    );
}