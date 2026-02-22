// components/auth/RoleTabs.tsx
"use client";
import { useRouter, usePathname } from "next/navigation";

const ROLES = [
  { label: "Public User", loginPath: "/auth/login?role=public", registerPath: "/auth/register?role=public" },
  { label: "Technician",  loginPath: "/auth/login?role=tech",   registerPath: "/auth/register?role=tech" },
  { label: "Company",     loginPath: "/auth/login?role=company", registerPath: "/auth/register?role=company" },
];

interface RoleTabsProps {
  activeRole: "public" | "tech" | "company";
  mode: "login" | "register";
}

export default function RoleTabs({ activeRole, mode }: RoleTabsProps) {
  const router = useRouter();

  const roleKey = (label: string) =>
    label === "Public User" ? "public" : label === "Technician" ? "tech" : "company";

  return (
    <div className="role-tabs">
      {ROLES.map((role) => {
        const key = roleKey(role.label);
        const isActive = key === activeRole;
        const path = mode === "login" ? role.loginPath : role.registerPath;
        return (
          <button
            key={key}
            className={`role-tab ${isActive ? "active" : ""}`}
            onClick={() => router.push(path)}
          >
            {role.label}
          </button>
        );
      })}
    </div>
  );
}
