export default function Login() {
    const handleLogin = () => {
        window.location.href = "http://localhost:8080/oauth2/authorization/google";
    };

    return (
        <div style={{
            minHeight: "100vh",
            background: "#080c14",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            padding: "2rem",
            fontFamily: "Inter, sans-serif",
            position: "relative",
            overflow: "hidden"
        }}>
            {/* Background glow */}
            <div style={{
                position: "absolute",
                width: "500px",
                height: "500px",
                borderRadius: "50%",
                background: "radial-gradient(circle, rgba(99,102,241,0.18) 0%, transparent 70%)",
                top: "-100px",
                left: "50%",
                transform: "translateX(-50%)",
                pointerEvents: "none"
            }} />

            {/* Card */}
            <div style={{
                position: "relative",
                background: "#0f1623",
                border: "0.5px solid rgba(99,102,241,0.25)",
                borderRadius: "20px",
                padding: "2.5rem 2rem 2rem",
                width: "100%",
                maxWidth: "360px",
                textAlign: "center"
            }}>
                {/* Logo */}
                <div style={{
                    width: 56, height: 56,
                    background: "linear-gradient(135deg, #6366f1, #818cf8)",
                    borderRadius: "14px",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    margin: "0 auto 1.25rem"
                }}>
                    <svg width="26" height="26" viewBox="0 0 24 24" fill="none"
                        stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71" />
                        <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71" />
                    </svg>
                </div>

                {/* Brand name */}
                <h1 style={{
                    fontSize: 24,
                    fontWeight: 600,
                    color: "#f1f5f9",
                    margin: "0 0 6px",
                    letterSpacing: "-0.3px"
                }}>
                    LinkMesh
                </h1>

                {/* Badge */}
                <div style={{
                    display: "inline-flex",
                    alignItems: "center",
                    gap: 5,
                    background: "rgba(99,102,241,0.12)",
                    border: "0.5px solid rgba(99,102,241,0.3)",
                    borderRadius: "20px",
                    padding: "3px 10px",
                    fontSize: 11,
                    color: "#818cf8",
                    letterSpacing: "0.06em",
                    textTransform: "uppercase",
                    marginBottom: "1.75rem"
                }}>
                    Smart URL Shortener
                </div>

                {/* Stats */}
                <div style={{
                    display: "grid",
                    gridTemplateColumns: "1fr 1fr 1fr",
                    gap: 8,
                    marginBottom: "1.75rem"
                }}>
                    {[
                        { num: "Custom", label: "Alias" },
                        { num: "99.9%", label: "Uptime" },
                        { num: "Live", label: "Analytics" }
                    ].map(({ num, label }) => (
                        <div key={label} style={{
                            background: "rgba(255,255,255,0.03)",
                            border: "0.5px solid rgba(255,255,255,0.07)",
                            borderRadius: 10,
                            padding: "10px 6px"
                        }}>
                            <span style={{ fontSize: 15, fontWeight: 600, color: "#e2e8f0", display: "block" }}>
                                {num}
                            </span>
                            <span style={{
                                fontSize: 10, color: "#475569",
                                textTransform: "uppercase", letterSpacing: "0.05em",
                                marginTop: 2, display: "block"
                            }}>
                                {label}
                            </span>
                        </div>
                    ))}
                </div>

                {/* Divider */}
                <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: "1.25rem" }}>
                    <div style={{ flex: 1, height: "0.5px", background: "rgba(255,255,255,0.07)" }} />
                    <span style={{ fontSize: 11, color: "#334155", whiteSpace: "nowrap" }}>
                        Sign in to continue
                    </span>
                    <div style={{ flex: 1, height: "0.5px", background: "rgba(255,255,255,0.07)" }} />
                </div>

                {/* Google Button */}
                <button
                    onClick={handleLogin}
                    onMouseEnter={e => {
                        e.currentTarget.style.background = "rgba(255,255,255,0.09)";
                        e.currentTarget.style.borderColor = "rgba(255,255,255,0.2)";
                        e.currentTarget.style.transform = "translateY(-1px)";
                    }}
                    onMouseLeave={e => {
                        e.currentTarget.style.background = "rgba(255,255,255,0.05)";
                        e.currentTarget.style.borderColor = "rgba(255,255,255,0.12)";
                        e.currentTarget.style.transform = "translateY(0)";
                    }}
                    style={{
                        width: "100%",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        gap: 10,
                        background: "rgba(255,255,255,0.05)",
                        color: "#e2e8f0",
                        border: "0.5px solid rgba(255,255,255,0.12)",
                        borderRadius: 10,
                        padding: "12px 20px",
                        fontSize: 14,
                        fontWeight: 500,
                        cursor: "pointer",
                        transition: "all 0.15s",
                        fontFamily: "inherit",
                        marginBottom: "1.25rem"
                    }}
                >
                    <svg width="17" height="17" viewBox="0 0 18 18" xmlns="http://www.w3.org/2000/svg">
                        <path d="M17.64 9.2c0-.637-.057-1.251-.164-1.84H9v3.481h4.844a4.14 4.14 0 0 1-1.796 2.716v2.259h2.908C16.658 14.017 17.64 11.71 17.64 9.2z" fill="#4285F4"/>
                        <path d="M9 18c2.43 0 4.467-.806 5.956-2.184l-2.908-2.259c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332A8.997 8.997 0 0 0 9 18z" fill="#34A853"/>
                        <path d="M3.964 10.706A5.41 5.41 0 0 1 3.682 9c0-.593.102-1.17.282-1.706V4.962H.957A8.996 8.996 0 0 0 0 9c0 1.452.348 2.827.957 4.038l3.007-2.332z" fill="#FBBC05"/>
                        <path d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0A8.997 8.997 0 0 0 .957 4.962L3.964 7.294C4.672 5.163 6.656 3.58 9 3.58z" fill="#EA4335"/>
                    </svg>
                    Continue with Google
                </button>

                {/* Terms */}
                <p style={{ fontSize: 11, color: "#334155", lineHeight: 1.6 }}>
                    By continuing, you agree to our{" "}
                    <a href="#" style={{ color: "#4f6080", textDecoration: "none" }}>Terms of Service</a>
                    {" "}and{" "}
                    <a href="#" style={{ color: "#4f6080", textDecoration: "none" }}>Privacy Policy</a>
                </p>
            </div>
        </div>
    );
}