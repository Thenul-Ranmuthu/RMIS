import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
    title: "Ministry of Environment Portal",
    description: "Unified Access Portal for public users, technicians, and partner companies.",
};

export default function RootLayout({
    children,
}: {
    children: React.ReactNode;
}) {
    return (
        <html lang="en">
            <body>{children}</body>
        </html>
    );
}
