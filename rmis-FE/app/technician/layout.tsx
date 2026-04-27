import TechnicianNavbar from "@/components/TechnicianNavbar";

export default function TechnicianLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <>
      <TechnicianNavbar />
      {children}
    </>
  );
}