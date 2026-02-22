// components/auth/AuthLayout.tsx
import Link from "next/link";
import Image from "next/image";

interface AuthLayoutProps {
  children: React.ReactNode;
}

export default function AuthLayout({ children }: AuthLayoutProps) {
  return (
    <div className="auth-layout">
      {/* ── Left Panel ─────────────────────────────── */}
      <div className="auth-panel-left">
        {/* Background image — place /public/forest-bg.jpg in your project */}
        <div className="bg-image" />
        <div className="overlay-gradient" />

        {/* Logo */}
        <Link href="/" className="logo">
          <div className="logo-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path
                d="M12 2C8 2 4 5.5 4 10c0 2.5 1.2 4.7 3 6.2V20h10v-3.8C18.8 14.7 20 12.5 20 10c0-4.5-4-8-8-8z"
                fill="white"
                opacity="0.9"
              />
              <path d="M12 2v18" stroke="white" strokeWidth="1.5" opacity="0.4" />
              <path d="M8 8l4 4 4-4" stroke="white" strokeWidth="1.5" strokeLinecap="round" opacity="0.6" />
            </svg>
          </div>
          <span className="logo-text">Ministry of Environment</span>
        </Link>

        {/* Hero copy */}
        <div className="hero-content">
          <h2 className="hero-title">
            Preserving our<br />
            natural heritage for<br />
            a sustainable<br />
            tomorrow.
          </h2>
          <p className="hero-subtitle">
            Unified Access Portal for public users, technicians, and partner
            companies. Together for a greener planet.
          </p>
        </div>

        {/* Footer */}
        <footer className="panel-footer">
          <span style={{ fontSize: 12, color: "rgba(255,255,255,0.4)" }}>
            © 2024 Ministry of Environment
          </span>
          <Link href="/privacy">Privacy Policy</Link>
          <Link href="/accessibility">Accessibility</Link>
        </footer>
      </div>

      {/* ── Right Panel ─────────────────────────────── */}
      <div className="auth-panel-right">{children}</div>
    </div>
  );
}
