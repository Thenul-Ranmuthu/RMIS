"use client";

import SignupCard from "../../components/SignupCard";

export default function SignupPage() {
    return (
        <main className="relative min-h-screen w-full overflow-hidden">

            {/* Background Image */}
            <div
                className="absolute inset-0 bg-cover bg-center"
                style={{ backgroundImage: "url('/background.png')" }}
            />

            {/* Dark overlay for readability */}
            <div className="absolute inset-0 bg-black/10" />

            {/* Content */}
            <div className="relative z-10 flex min-h-screen items-center justify-center px-10 py-16 max-w-7xl mx-auto">
                <SignupCard />
            </div>

        </main>
    );
}
