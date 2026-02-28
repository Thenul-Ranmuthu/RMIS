import "./globals.css";

export const metadata = {
  title: "Ministry of Environment Portal",
  description: "Unified Access Portal for public users, technicians, and partner companies.",
};

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
