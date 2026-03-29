"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";

interface Certification {
  id: number;
  certificationName: string;
  issuingAuthority: string;
  fileType: string;
  fileUrl: string;
  originalFileName: string;
}

interface Technician {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  address: string;
  specialization: string;
  yearsOfExperience: number;
  status: string;
  registrationDate: string;
  approvalDate: string;
  certifications: Certification[];
}

const sriLankanDistricts = [
  "Ampara","Anuradhapura","Badulla","Batticaloa","Colombo",
  "Galle","Gampaha","Hambantota","Jaffna","Kalutara",
  "Kandy","Kegalle","Kilinochchi","Kurunegala","Mannar",
  "Matale","Matara","Monaragala","Mullaitivu","Nuwara Eliya",
  "Polonnaruwa","Puttalam","Ratnapura","Trincomalee","Vavuniya",
];

// ── Inline SVG nature/environment background ──────────────────────────────
function NatureBg() {
  return (
    <svg style={{ position:"absolute", inset:0, width:"100%", height:"100%", opacity:0.2 }}
      xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1440 900" preserveAspectRatio="xMidYMid slice">
      {/* Mountains */}
      <polygon points="0,900 280,380 560,900"          fill="#34d399" />
      <polygon points="180,900 520,270 860,900"         fill="#10b981" />
      <polygon points="460,900 780,400 1100,900"        fill="#34d399" />
      <polygon points="720,900 1020,330 1320,900"       fill="#6ee7b7" />
      <polygon points="1050,900 1260,440 1440,580 1440,900" fill="#10b981" />
      {/* Snow caps */}
      <polygon points="280,380 252,448 308,448"  fill="#ffffff" opacity="0.55" />
      <polygon points="520,270 490,345 550,345"  fill="#ffffff" opacity="0.55" />
      <polygon points="780,400 752,468 808,468"  fill="#ffffff" opacity="0.55" />
      <polygon points="1020,330 990,405 1050,405" fill="#ffffff" opacity="0.55" />
      {/* Left trees */}
      <rect x="28"  y="748" width="10" height="62" rx="3" fill="#065f46" />
      <polygon points="33,678 8,756  58,756"   fill="#059669" />
      <polygon points="33,716 12,782 54,782"   fill="#047857" />
      <rect x="88"  y="758" width="8"  height="52" rx="3" fill="#065f46" />
      <polygon points="92,698 70,762 114,762"  fill="#059669" />
      <rect x="140" y="752" width="9"  height="58" rx="3" fill="#065f46" />
      <polygon points="144,688 119,756 169,756" fill="#10b981" />
      {/* Right trees */}
      <rect x="1358" y="748" width="10" height="62" rx="3" fill="#065f46" />
      <polygon points="1363,678 1338,756 1388,756" fill="#059669" />
      <rect x="1308" y="758" width="8"  height="52" rx="3" fill="#065f46" />
      <polygon points="1312,698 1290,762 1334,762" fill="#047857" />
      <rect x="1258" y="752" width="9"  height="58" rx="3" fill="#065f46" />
      <polygon points="1262,688 1237,756 1287,756" fill="#10b981" />
      {/* Sun */}
      <circle cx="720" cy="110" r="52"  fill="#fde68a" opacity="0.38" />
      <circle cx="720" cy="110" r="85"  fill="#fde68a" opacity="0.12" />
      <circle cx="720" cy="110" r="115" fill="#fde68a" opacity="0.06" />
      {/* Birds */}
      <path d="M195 175 Q206 164 217 175" stroke="#fff" strokeWidth="1.8" fill="none" opacity="0.5"/>
      <path d="M228 160 Q239 149 250 160" stroke="#fff" strokeWidth="1.8" fill="none" opacity="0.5"/>
      <path d="M1195 195 Q1206 184 1217 195" stroke="#fff" strokeWidth="1.8" fill="none" opacity="0.5"/>
      <path d="M1235 178 Q1246 167 1257 178" stroke="#fff" strokeWidth="1.8" fill="none" opacity="0.5"/>
      {/* River / water */}
      <path d="M0,820 Q200,800 400,828 Q600,856 800,838 Q1000,820 1200,844 Q1320,856 1440,832 L1440,900 L0,900Z"
        fill="#0d9488" opacity="0.32"/>
      <path d="M0,852 Q180,836 360,854 Q540,872 720,858 Q900,844 1100,863 Q1280,878 1440,858 L1440,900 L0,900Z"
        fill="#0f766e" opacity="0.22"/>
    </svg>
  );
}

// ─────────────────────────────────────────────────────────────────────────────
export default function PublicDirectory() {
  const router = useRouter();

  const [technicians, setTechnicians]   = useState<Technician[]>([]);
  const [isLoading, setIsLoading]       = useState(true);
  const [error, setError]               = useState("");
  const [searchTerm, setSearchTerm]     = useState("");
  const [selectedSpec, setSelectedSpec] = useState("");
  const [selectedExp,  setSelectedExp]  = useState("");
  const [selectedDist, setSelectedDist] = useState("");
  const [currentPage,  setCurrentPage]  = useState(1);
  const PER_PAGE = 6;

  const extractDistrict = (address: string) => {
    if (!address) return "Unknown";
    const parts   = address.split(",").map((p) => p.trim());
    const last    = parts[parts.length - 1] || "";
    const matched = sriLankanDistricts.find((d) => last.toLowerCase().includes(d.toLowerCase()));
    return matched || last || "Unknown";
  };

  useEffect(() => {
    (async () => {
      setIsLoading(true);
      try {
        const res  = await fetch("http://localhost:5050/public/technicians/active");
        if (!res.ok) throw new Error();
        const data = await res.json();
        setTechnicians(Array.isArray(data) ? data : []);
      } catch { setError("Failed to load technicians. Please try again later."); }
      finally  { setIsLoading(false); }
    })();
  }, []);

  const specs    = useMemo(() => Array.from(new Set(technicians.map((t) => (t.specialization || "").trim()).filter(Boolean))).sort(), [technicians]);
  const dists    = useMemo(() => Array.from(new Set(technicians.map((t) => extractDistrict(t.address)).filter((d) => d !== "Unknown"))).sort(), [technicians]);

  const filtered = useMemo(() => {
    let f = [...technicians];
    if (searchTerm.trim()) {
      const t = searchTerm.toLowerCase();
      f = f.filter((x) => (x.firstName+x.lastName+x.specialization).toLowerCase().includes(t));
    }
    if (selectedSpec) f = f.filter((x) => x.specialization === selectedSpec);
    if (selectedDist) f = f.filter((x) => extractDistrict(x.address) === selectedDist);
    if (selectedExp)  {
      const [mn, mx] = selectedExp.split("-").map(Number);
      f = f.filter((x) => { const e = Number(x.yearsOfExperience)||0; return mx ? e>=mn&&e<=mx : e>=mn; });
    }
    return f;
  }, [technicians, searchTerm, selectedSpec, selectedExp, selectedDist]);

  useEffect(() => setCurrentPage(1), [searchTerm, selectedSpec, selectedExp, selectedDist]);

  const totalPages    = Math.ceil(filtered.length / PER_PAGE);
  const currentItems  = filtered.slice((currentPage-1)*PER_PAGE, currentPage*PER_PAGE);
  const clearFilters  = () => { setSearchTerm(""); setSelectedSpec(""); setSelectedExp(""); setSelectedDist(""); };
  const initials      = (f:string, l:string) => `${f?.[0]||""}${l?.[0]||""}`.toUpperCase() || "T";

  const BG: React.CSSProperties = {
    position: "fixed", inset: 0,
    background: "linear-gradient(145deg, #064e3b 0%, #065f46 30%, #047857 60%, #059669 100%)",
    zIndex: 0,
  };

  // ── Loading ──
  if (isLoading) return (
    <div style={{ minHeight:"100vh", position:"relative" }}>
      <div style={BG}><NatureBg /><div style={{ position:"absolute", inset:0, background:"rgba(0,0,0,0.46)" }}/></div>
      <div style={{ position:"relative", zIndex:1, minHeight:"100vh", display:"flex", alignItems:"center", justifyContent:"center" }}>
        <div style={{ textAlign:"center" }}>
          <div style={{ width:52, height:52, borderRadius:"50%", border:"4px solid rgba(52,211,153,0.3)", borderTopColor:"#34d399", animation:"spin 0.85s linear infinite", margin:"0 auto 14px" }}/>
          <p style={{ color:"rgba(255,255,255,0.7)", fontSize:13, fontWeight:500 }}>Loading technicians…</p>
        </div>
      </div>
      <style>{`@keyframes spin{to{transform:rotate(360deg)}}`}</style>
    </div>
  );

  // ── Error ──
  if (error) return (
    <div style={{ minHeight:"100vh", position:"relative" }}>
      <div style={BG}><NatureBg /><div style={{ position:"absolute", inset:0, background:"rgba(0,0,0,0.46)" }}/></div>
      <div style={{ position:"relative", zIndex:1, minHeight:"100vh", display:"flex", alignItems:"center", justifyContent:"center", padding:16 }}>
        <div className="bg-white rounded-3xl shadow-2xl p-10 text-center" style={{ maxWidth:380, width:"100%" }}>
          <div style={{ background:"#fef2f2", borderRadius:"50%", width:64, height:64, display:"flex", alignItems:"center", justifyContent:"center", margin:"0 auto 14px" }}>
            <svg style={{ width:30, height:30 }} fill="none" viewBox="0 0 24 24" stroke="#dc2626"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
          </div>
          <h2 className="text-2xl font-black text-gray-900 mb-2">Something went wrong</h2>
          <p className="text-sm text-gray-500 mb-6">{error}</p>
          <button onClick={() => window.location.reload()} className="w-full bg-emerald-600 hover:bg-emerald-700 text-white py-3.5 rounded-xl text-sm font-bold shadow-lg transition-all duration-200 active:scale-[0.98]">Try Again</button>
        </div>
      </div>
    </div>
  );

  // ── Main ──
  return (
    <div style={{ minHeight:"100vh", position:"relative" }}>
      {/* Fixed background */}
      <div style={BG}><NatureBg /><div style={{ position:"absolute", inset:0, background:"rgba(0,0,0,0.46)" }}/></div>

      <style>{`
        @keyframes spin      { to  { transform:rotate(360deg); } }
        @keyframes fadeInUp  { from{ opacity:0; transform:translateY(18px); } to{ opacity:1; transform:translateY(0); } }
        @keyframes scaleIn   { from{ opacity:0; transform:scale(0.95); }      to{ opacity:1; transform:scale(1);    } }
        @keyframes pulseDot  { 0%,100%{ opacity:1; } 50%{ opacity:0.35; } }
        .d-fadeup  { animation: fadeInUp 0.45s ease both; }
        .d-scalein { animation: scaleIn  0.38s ease both; }
        .d-card    { transition: transform 0.22s ease, box-shadow 0.22s ease; }
        .d-card:hover { transform: translateY(-5px); box-shadow: 0 26px 60px rgba(0,0,0,0.22); }
        .d-btn     { transition: background 0.18s ease, transform 0.14s ease, box-shadow 0.18s ease; cursor:pointer; }
        .d-btn:hover  { background:#047857 !important; box-shadow:0 8px 22px rgba(5,150,105,0.45) !important; }
        .d-btn:active { transform:scale(0.97); }
        .d-inp     { transition: border-color 0.18s ease, box-shadow 0.18s ease; }
        .d-inp:focus { border-color:#10b981 !important; box-shadow:0 0 0 3px rgba(16,185,129,0.18); outline:none; }
        .d-stat    { transition: transform 0.18s ease, background 0.18s ease; }
        .d-stat:hover { transform:translateY(-2px); background:rgba(255,255,255,0.17) !important; }
      `}</style>

      {/* Scrollable content */}
      <div style={{ position:"relative", zIndex:1 }}>
        <div style={{ maxWidth:1280, margin:"0 auto", padding:"40px 20px 64px" }}>

          {/* ── Hero ── */}
          <section className="d-fadeup" style={{ marginBottom:32, animationDelay:"0.04s" }}>
            <div style={{ display:"flex", flexWrap:"wrap", gap:20, alignItems:"flex-end", justifyContent:"space-between" }}>
              <div style={{ color:"#fff", flex:"1 1 280px" }}>
                <div style={{ display:"inline-flex", alignItems:"center", gap:7, background:"rgba(255,255,255,0.1)", border:"1px solid rgba(255,255,255,0.18)", borderRadius:999, padding:"5px 14px", marginBottom:14, backdropFilter:"blur(8px)" }}>
                  <span style={{ width:6, height:6, borderRadius:"50%", background:"#34d399", animation:"pulseDot 2s infinite" }}/>
                  <span style={{ fontSize:10, fontWeight:700, letterSpacing:"0.18em", textTransform:"uppercase", color:"#6ee7b7" }}>Public Directory</span>
                </div>
                <h1 style={{ fontSize:"clamp(30px,4.5vw,52px)", fontWeight:900, lineHeight:1.1, margin:0 }}>
                  Find Certified<br/><span style={{ color:"#34d399" }}>Technicians</span>
                </h1>
                <p style={{ marginTop:10, fontSize:14, color:"rgba(255,255,255,0.7)", maxWidth:460, lineHeight:1.65 }}>
                  Browse verified environmental compliance technicians across Sri Lanka. Filter by expertise, experience, and district.
                </p>
              </div>

              {/* Stats */}
              <div style={{ display:"grid", gridTemplateColumns:"repeat(2,1fr)", gap:10, flex:"0 0 auto" }}>
                {[
                  { v:technicians.length,  l:"Total",           e:"👥" },
                  { v:specs.length,        l:"Specializations", e:"🎓" },
                  { v:dists.length,        l:"Districts",       e:"📍" },
                  { v:filtered.length,     l:"Matched",         e:"✅" },
                ].map((s,i) => (
                  <div key={s.l} className="d-stat d-fadeup"
                    style={{ background:"rgba(255,255,255,0.1)", border:"1px solid rgba(255,255,255,0.14)", borderRadius:16, padding:"12px 16px", color:"#fff", backdropFilter:"blur(12px)", animationDelay:`${0.09+i*0.06}s`, minWidth:100 }}>
                    <div style={{ fontSize:17, marginBottom:3 }}>{s.e}</div>
                    <div style={{ fontSize:24, fontWeight:900, lineHeight:1 }}>{s.v}</div>
                    <div style={{ fontSize:10, color:"rgba(255,255,255,0.55)", marginTop:3, fontWeight:600 }}>{s.l}</div>
                  </div>
                ))}
              </div>
            </div>
          </section>

          {/* ── Filters ── */}
          <section className="d-fadeup"
            style={{ background:"rgba(255,255,255,0.08)", border:"1px solid rgba(255,255,255,0.12)", borderRadius:22, padding:"18px 22px", marginBottom:26, backdropFilter:"blur(16px)", animationDelay:"0.17s" }}>
            <div style={{ display:"grid", gridTemplateColumns:"repeat(auto-fill,minmax(170px,1fr))", gap:12 }}>
              {/* Search spans 2 cols when possible */}
              <div style={{ gridColumn:"span 2" }}>
                <label style={LBL}>Search</label>
                <div style={{ position:"relative" }}>
                  <input type="text" value={searchTerm} onChange={e=>setSearchTerm(e.target.value)}
                    placeholder="Name or specialization…" className="d-inp"
                    style={{ ...INP, paddingLeft:38 }}/>
                  <svg style={{ position:"absolute", left:11, top:"50%", transform:"translateY(-50%)", width:15, height:15 }} fill="none" stroke="#9ca3af" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
                  </svg>
                </div>
              </div>
              <div>
                <label style={LBL}>Specialization</label>
                <select value={selectedSpec} onChange={e=>setSelectedSpec(e.target.value)} className="d-inp" style={INP}>
                  <option value="">All</option>
                  {specs.map(s=><option key={s} value={s}>{s}</option>)}
                </select>
              </div>
              <div>
                <label style={LBL}>District</label>
                <select value={selectedDist} onChange={e=>setSelectedDist(e.target.value)} className="d-inp" style={INP}>
                  <option value="">All</option>
                  {dists.map(d=><option key={d} value={d}>{d}</option>)}
                </select>
              </div>
              <div>
                <label style={LBL}>Experience</label>
                <select value={selectedExp} onChange={e=>setSelectedExp(e.target.value)} className="d-inp" style={INP}>
                  <option value="">Any</option>
                  <option value="0-2">0–2 yrs</option>
                  <option value="3-5">3–5 yrs</option>
                  <option value="6-10">6–10 yrs</option>
                  <option value="10-100">10+ yrs</option>
                </select>
              </div>
            </div>
            <div style={{ marginTop:12, display:"flex", alignItems:"center", justifyContent:"space-between", flexWrap:"wrap", gap:8 }}>
              <p style={{ fontSize:12, color:"rgba(255,255,255,0.7)", fontWeight:500, margin:0 }}>
                Showing <strong style={{ color:"#fff" }}>{filtered.length}</strong> technician{filtered.length!==1?"s":""}
              </p>
              {(searchTerm||selectedSpec||selectedDist||selectedExp) && (
                <button onClick={clearFilters}
                  style={{ fontSize:11, fontWeight:700, color:"#fff", background:"rgba(255,255,255,0.12)", border:"1px solid rgba(255,255,255,0.2)", borderRadius:8, padding:"5px 12px", cursor:"pointer", transition:"background 0.18s" }}>
                  ✕ Clear Filters
                </button>
              )}
            </div>
          </section>

          {/* ── Grid ── */}
          {currentItems.length > 0 ? (
            <>
              <section style={{ display:"grid", gridTemplateColumns:"repeat(auto-fill,minmax(290px,1fr))", gap:18 }}>
                {currentItems.map((tech, i) => (
                  <article key={tech.id} className="d-card d-scalein bg-white rounded-3xl shadow-2xl overflow-hidden"
                    style={{ animationDelay:`${i*0.06}s` }}>

                    {/* Header */}
                    <div style={{ background:"linear-gradient(135deg,#059669,#10b981)", padding:"18px 18px 16px", color:"#fff" }}>
                      <div style={{ display:"flex", alignItems:"center", gap:12 }}>
                        <div style={{ width:56, height:56, borderRadius:"50%", background:"#fff", display:"flex", alignItems:"center", justifyContent:"center", flexShrink:0, boxShadow:"0 4px 14px rgba(0,0,0,0.15)" }}>
                          <span style={{ fontSize:18, fontWeight:900, color:"#059669" }}>{initials(tech.firstName, tech.lastName)}</span>
                        </div>
                        <div style={{ minWidth:0 }}>
                          <h3 style={{ fontSize:15, fontWeight:900, margin:0, overflow:"hidden", textOverflow:"ellipsis", whiteSpace:"nowrap" }}>
                            {tech.firstName} {tech.lastName}
                          </h3>
                          <p style={{ fontSize:11, color:"rgba(255,255,255,0.8)", margin:"2px 0 0", overflow:"hidden", textOverflow:"ellipsis", whiteSpace:"nowrap" }}>
                            {tech.specialization || "General Technician"}
                          </p>
                          <div style={{ marginTop:7, display:"inline-flex", alignItems:"center", gap:5, background:"rgba(255,255,255,0.18)", borderRadius:999, padding:"3px 9px" }}>
                            <span style={{ width:5, height:5, borderRadius:"50%", background:"#86efac" }}/>
                            <span style={{ fontSize:9, fontWeight:700, letterSpacing:"0.06em" }}>ACTIVE</span>
                          </div>
                        </div>
                      </div>
                    </div>

                    {/* Body */}
                    <div style={{ padding:"16px 18px 18px" }}>
                      <div style={{ display:"flex", flexDirection:"column", gap:9 }}>
                        {/* Experience */}
                        <Row icon={<ClockIcon/>} label="Experience" value={`${tech.yearsOfExperience||0} years`}/>
                        {/* District */}
                        {tech.address && <Row icon={<PinIcon/>} label="District" value={extractDistrict(tech.address)}/>}
                        {/* Phone */}
                        <Row icon={<PhoneIcon/>} label="Contact" value={tech.phoneNumber||"Not available"}/>
                        {/* Certs */}
                        {tech.certifications?.length > 0 && (
                          <div style={{ paddingTop:2 }}>
                            <p style={{ ...SUBLBL, marginBottom:5 }}>Certifications</p>
                            <div style={{ display:"flex", flexWrap:"wrap", gap:4 }}>
                              {tech.certifications.slice(0,2).map((c,k)=>(
                                <span key={k} style={{ fontSize:10, fontWeight:600, background:"#f3f4f6", color:"#374151", borderRadius:6, padding:"3px 8px" }}>{c.certificationName}</span>
                              ))}
                              {tech.certifications.length>2 && (
                                <span style={{ fontSize:10, fontWeight:700, background:"#ecfdf5", color:"#059669", borderRadius:6, padding:"3px 8px" }}>+{tech.certifications.length-2} more</span>
                              )}
                            </div>
                          </div>
                        )}
                      </div>
                      <button onClick={()=>router.push(`/public/technician/${tech.id}`)} className="d-btn"
                        style={{ marginTop:14, width:"100%", background:"#059669", color:"#fff", border:"none", borderRadius:11, padding:"11px 0", fontSize:12, fontWeight:700, boxShadow:"0 4px 14px rgba(5,150,105,0.3)" }}>
                        View Full Profile →
                      </button>
                    </div>
                  </article>
                ))}
              </section>

              {/* Pagination */}
              {totalPages > 1 && (
                <div style={{ marginTop:32, display:"flex", alignItems:"center", justifyContent:"center", gap:7, flexWrap:"wrap" }}>
                  <PageBtn onClick={()=>setCurrentPage(p=>Math.max(p-1,1))} disabled={currentPage===1}>← Prev</PageBtn>
                  {Array.from({length:totalPages},(_,i)=>i+1).map(page=>(
                    <PageBtn key={page} onClick={()=>setCurrentPage(page)} active={currentPage===page}>{page}</PageBtn>
                  ))}
                  <PageBtn onClick={()=>setCurrentPage(p=>Math.min(p+1,totalPages))} disabled={currentPage===totalPages}>Next →</PageBtn>
                </div>
              )}
            </>
          ) : (
            <div className="d-scalein" style={{ maxWidth:400, margin:"0 auto" }}>
              <div className="bg-white rounded-3xl shadow-2xl p-10 text-center">
                <div style={{ background:"#f0fdf4", borderRadius:"50%", width:68, height:68, display:"flex", alignItems:"center", justifyContent:"center", margin:"0 auto 14px" }}>
                  <svg style={{ width:32, height:32 }} fill="none" stroke="#059669" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                  </svg>
                </div>
                <h3 className="text-2xl font-black text-gray-900 mb-2">No technicians found</h3>
                <p className="text-sm text-gray-500 mb-6">Try adjusting your search filters.</p>
                <button onClick={clearFilters} className="d-btn w-full bg-emerald-600 text-white py-3.5 rounded-xl text-sm font-bold"
                  style={{ border:"none", boxShadow:"0 4px 18px rgba(5,150,105,0.3)" }}>Clear Filters</button>
              </div>
            </div>
          )}

          <footer style={{ marginTop:44, textAlign:"center", fontSize:11, color:"rgba(255,255,255,0.38)", fontWeight:500 }}>
            © {new Date().getFullYear()} RMIS · Ministry of Environment
          </footer>
        </div>
      </div>
    </div>
  );
}

// ── Small reusable pieces ────────────────────────────────────────────────────

const LBL: React.CSSProperties = { display:"block", fontSize:11, fontWeight:700, color:"rgba(255,255,255,0.82)", marginBottom:5, textTransform:"uppercase", letterSpacing:"0.07em" };
const INP: React.CSSProperties = { width:"100%", borderRadius:11, border:"1px solid #d1fae5", background:"#f9fafb", padding:"10px 12px", fontSize:12, color:"#111", boxSizing:"border-box", appearance:"none" };
const SUBLBL: React.CSSProperties = { fontSize:9, fontWeight:700, color:"#9ca3af", textTransform:"uppercase", letterSpacing:"0.07em", margin:0 };

function Row({ icon, label, value }: { icon: React.ReactNode; label: string; value: string }) {
  return (
    <div style={{ display:"flex", alignItems:"center", gap:9 }}>
      <div style={{ width:32, height:32, borderRadius:9, background:"#f0fdf4", display:"flex", alignItems:"center", justifyContent:"center", flexShrink:0 }}>
        {icon}
      </div>
      <div style={{ minWidth:0 }}>
        <p style={{ ...SUBLBL, marginBottom:1 }}>{label}</p>
        <p style={{ fontSize:12, fontWeight:700, color:"#111827", margin:0, overflow:"hidden", textOverflow:"ellipsis", whiteSpace:"nowrap" }}>{value}</p>
      </div>
    </div>
  );
}

function PageBtn({ children, onClick, disabled, active }: { children: React.ReactNode; onClick: ()=>void; disabled?: boolean; active?: boolean }) {
  return (
    <button onClick={onClick} disabled={disabled}
      style={{ padding:"8px 15px", borderRadius:9, border: active ? "none" : "1px solid rgba(255,255,255,0.2)", background: active ? "#059669" : "rgba(255,255,255,0.9)", color: active ? "#fff" : "#374151", fontSize:12, fontWeight:700, cursor:"pointer", opacity: disabled ? 0.4 : 1, boxShadow: active ? "0 4px 14px rgba(5,150,105,0.4)" : "none", transition:"all 0.18s" }}>
      {children}
    </button>
  );
}

function ClockIcon() {
  return <svg style={{ width:14, height:14 }} fill="none" stroke="#059669" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>;
}
function PinIcon() {
  return <svg style={{ width:14, height:14 }} fill="none" stroke="#059669" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/></svg>;
}
function PhoneIcon() {
  return <svg style={{ width:14, height:14 }} fill="none" stroke="#059669" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"/></svg>;
}