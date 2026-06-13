import { useEffect, useState } from "react";
import api from "../services/api";

const styles = {
  page: {
    minHeight: "100vh",
    background: "#080c14",
    fontFamily: "Inter, sans-serif",
    boxSizing: "border-box",
    overflowX: "hidden",
  },
  topbar: {
    background: "#0f1623",
    borderBottom: "0.5px solid rgba(99,102,241,0.2)",
    padding: "0 24px",
    height: 56,
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
  },
  logoIcon: {
    width: 30, height: 30,
    background: "#6366f1",
    borderRadius: 7,
    display: "flex", alignItems: "center", justifyContent: "center",
    fontSize: 15, color: "#fff",
  },
  avatar: {
    width: 32, height: 32,
    borderRadius: "50%",
    background: "#6366f1",
    display: "flex", alignItems: "center", justifyContent: "center",
    fontSize: 13, fontWeight: 500, color: "#fff",
  },
  statCard: {
    background: "#0f1623",
    border: "0.5px solid rgba(255,255,255,0.07)",
    borderRadius: 12,
    padding: 16,
  },
  section: {
    background: "#0f1623",
    border: "0.5px solid rgba(255,255,255,0.07)",
    borderRadius: 14,
    padding: 20,
    marginBottom: 20,
  },
  input: {
    background: "rgba(255,255,255,0.04)",
    border: "0.5px solid rgba(255,255,255,0.1)",
    borderRadius: 8,
    padding: "9px 12px",
    fontSize: 13,
    color: "#e2e8f0",
    outline: "none",
    width: "100%",
    fontFamily: "inherit",
    boxSizing: "border-box",
  },
  inputLocked: {
    background: "rgba(255,255,255,0.02)",
    border: "0.5px solid rgba(255,255,255,0.05)",
    borderRadius: 8,
    padding: "9px 12px",
    fontSize: 13,
    color: "#334155",
    outline: "none",
    width: "100%",
    fontFamily: "inherit",
    boxSizing: "border-box",
    cursor: "not-allowed",
  },
};

function PremiumBadge() {
  return (
    <span style={{
      background: "linear-gradient(135deg, #f59e0b, #fbbf24)",
      color: "#1c1917",
      fontSize: 9,
      fontWeight: 700,
      letterSpacing: "0.06em",
      padding: "2px 6px",
      borderRadius: 4,
      textTransform: "uppercase",
      marginLeft: 6,
      verticalAlign: "middle",
    }}>
      Premium
    </span>
  );
}

function UpgradeBanner({ onUpgrade, upgrading }) {
  return (
    <div style={{
      background: "rgba(245,158,11,0.08)",
      border: "0.5px solid rgba(245,158,11,0.3)",
      borderRadius: 10,
      padding: "12px 16px",
      display: "flex",
      alignItems: "center",
      justifyContent: "space-between",
      gap: 12,
      marginBottom: 16,
    }}>
      <div>
        <div style={{ fontSize: 13, fontWeight: 500, color: "#fbbf24" }}>
          🔒 Free Plan
        </div>
        <div style={{ fontSize: 11, color: "#78716c", marginTop: 2 }}>
          Upgrade to Premium to unlock custom aliases and custom expiry dates.
        </div>
      </div>
      <button
        onClick={onUpgrade}
        disabled={upgrading}
        style={{
          background: "linear-gradient(135deg, #f59e0b, #fbbf24)",
          border: "none",
          borderRadius: 7,
          padding: "7px 14px",
          fontSize: 12,
          fontWeight: 600,
          color: "#1c1917",
          cursor: upgrading ? "not-allowed" : "pointer",
          whiteSpace: "nowrap",
          opacity: upgrading ? 0.7 : 1,
        }}
      >
        {upgrading ? "Upgrading…" : "Upgrade Now"}
      </button>
    </div>
  );
}

export default function Dashboard() {
  const [user, setUser] = useState(null);
  const [longUrl, setLongUrl] = useState("");
  const [shortUrl, setShortUrl] = useState("");
  const [urls, setUrls] = useState([]);
  const [customAlias, setCustomAlias] = useState("");
  const [expiryDays, setExpiryDays] = useState(1);
  const [copied, setCopied] = useState(false);
  const [upgrading, setUpgrading] = useState(false);

  const isPremium = user?.role === "PREMIUM";

  useEffect(() => {
    fetchUser();
    fetchUrls();
  }, []);

  async function fetchUser() {
    try {
      const res = await api.get("/me");
      setUser(res.data);
    } catch {
      window.location.href = "/";
    }
  }

  async function fetchUrls() {
    try {
      const res = await api.get("/v1/url/my-urls");
      setUrls(res.data);
    } catch (err) {
      console.log(err);
    }
  }

  async function createShortUrl() {
    try {
      const res = await api.post("/v1/url", {
        longUrl,
        customAlias: isPremium && customAlias ? customAlias : null,
        expiryDays: isPremium ? Number(expiryDays) : undefined,
      });
      setShortUrl(res.data);
      setLongUrl("");
      setCustomAlias("");
      setExpiryDays(30);
      fetchUrls();
    } catch (err) {
      alert(err.response?.data?.message || "Failed to create URL");
    }
  }

  async function deleteUrl(code) {
    await api.delete(`/v1/url/${code}`);
    fetchUrls();
  }

  async function handleUpgrade() {
    setUpgrading(true);
    try {
      // 1. Create Razorpay order
      const { data } = await api.post("/api/payment/create-order");

      // 2. Open Razorpay checkout
      const options = {
        key: data.keyId,
        amount: data.amount,
        currency: data.currency,
        name: "LinkMesh",
        description: "Premium Plan",
        order_id: data.orderId,
        handler: async function (response) {
          // 3. Verify on backend
          await api.post("/api/payment/verify", {
            razorpay_order_id:   response.razorpay_order_id,
            razorpay_payment_id: response.razorpay_payment_id,
            razorpay_signature:  response.razorpay_signature,
          });
          await fetchUser(); // refresh role to PREMIUM
        },
        prefill: { name: user.name, email: user.email },
        theme: { color: "#6366f1" },
      };

      const rzp = new window.Razorpay(options);
      rzp.open();
    } catch {
      alert("Payment failed. Please try again.");
    } finally {
      setUpgrading(false);
    }
  }

  function logout() {
    window.location.href = "http://localhost:8080/logout";
  }

  function copyLink() {
    navigator.clipboard?.writeText(shortUrl.shortUrl);
    setCopied(true);
    setTimeout(() => setCopied(false), 1500);
  }

  function formatDate(iso) {
    if (!iso) return "—";
    return new Date(iso).toLocaleDateString("en-IN", {
      day: "2-digit", month: "short", year: "numeric",
      hour: "2-digit", minute: "2-digit"
    });
  }

  const initials = user?.name
    ?.split(" ")
    .map((n) => n[0])
    .join("")
    .toUpperCase()
    .slice(0, 2);

  return (
    <div style={styles.page}>
      {/* Topbar */}
      <div style={styles.topbar}>
        <div style={{ display: "flex", alignItems: "center", gap: 9 }}>
          <div style={styles.logoIcon}>🔗</div>
          <span style={{ fontSize: 13, fontWeight: 500, color: "#f1f5f9" }}>LinkMesh</span>
        </div>
        {user && (
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            {isPremium && (
              <span style={{
                background: "linear-gradient(135deg, #f59e0b, #fbbf24)",
                color: "#1c1917",
                fontSize: 10,
                fontWeight: 700,
                letterSpacing: "0.06em",
                padding: "3px 8px",
                borderRadius: 5,
                textTransform: "uppercase",
              }}>
                ⭐ Premium
              </span>
            )}
            <div style={{ textAlign: "right" }}>
              <div style={{ fontSize: 13, fontWeight: 500, color: "#e2e8f0" }}>{user.name}</div>
            </div>
            {user.picture
              ? <img
                  src={user.picture}
                  alt="Profile"
                  style={{
                    width: "32px",
                    height: "32px",
                    borderRadius: "50%",
                    objectFit: "cover",
                    display: "block"
                  }}
                />
              : <div style={styles.avatar}>{initials}</div>
            }
            <button
              onClick={logout}
              style={{
                background: "rgba(255,255,255,0.05)",
                border: "0.5px solid rgba(255,255,255,0.1)",
                borderRadius: 7, padding: "5px 12px",
                fontSize: 12, color: "#94a3b8", cursor: "pointer",
              }}
            >
              Logout
            </button>
          </div>
        )}
      </div>

      <div style={{ padding: 24, maxWidth: 1200, margin: "0 auto" }}>
        {/* Stats */}
        <div style={{ display: "grid", gridTemplateColumns: "repeat(3,1fr)", gap: 12, marginBottom: 24 }}>
          {[
            { label: "Total links", value: urls.length },
            { label: "Total clicks", value: urls.reduce((sum, url) => sum + (url.clickCount || 0), 0) },
            { label: "Active links", value: urls.length },
          ].map(({ label, value }) => (
            <div key={label} style={styles.statCard}>
              <div style={{ fontSize: 11, color: "#475569", textTransform: "uppercase", letterSpacing: "0.06em", marginBottom: 6 }}>
                {label}
              </div>
              <div style={{ fontSize: 22, fontWeight: 500, color: "#f1f5f9" }}>{value}</div>
            </div>
          ))}
        </div>

        {/* Create section */}
        <div style={styles.section}>
          <div style={{ fontSize: 13, fontWeight: 500, color: "#94a3b8", textTransform: "uppercase", letterSpacing: "0.06em", marginBottom: 16 }}>
            Create short URL
          </div>

          {/* Upgrade banner for free users */}
          {user && !isPremium && (
            <UpgradeBanner onUpgrade={handleUpgrade} upgrading={upgrading} />
          )}

          <input
            style={styles.input}
            value={longUrl}
            onChange={(e) => setLongUrl(e.target.value)}
            placeholder="https://example.com/very/long/url"
          />

          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 10, marginTop: 10 }}>
            {/* Custom alias — premium only */}
            <div style={{ minWidth: 0 }}>
              <div style={{ fontSize: 11, color: isPremium ? "#475569" : "#334155", textTransform: "uppercase", letterSpacing: "0.04em", marginBottom: 5 }}>
                Custom alias
                {isPremium
                  ? <span style={{ color: "#1e293b" }}> (optional)</span>
                  : <PremiumBadge />
                }
              </div>
              <div style={{ position: "relative" }}>
                <input
                  style={isPremium ? styles.input : styles.inputLocked}
                  value={isPremium ? customAlias : ""}
                  onChange={(e) => isPremium && setCustomAlias(e.target.value)}
                  placeholder={isPremium ? "my-link" : "Upgrade to use custom aliases"}
                  disabled={!isPremium}
                />
                {!isPremium && (
                  <span style={{
                    position: "absolute", right: 10, top: "50%",
                    transform: "translateY(-50%)", fontSize: 13,
                  }}>🔒</span>
                )}
              </div>
            </div>

            {/* Expiry days — premium only */}
            <div style={{ minWidth: 0 }}>
              <div style={{ fontSize: 11, color: isPremium ? "#475569" : "#334155", textTransform: "uppercase", letterSpacing: "0.04em", marginBottom: 5 }}>
                Expiry days
                {!isPremium && <PremiumBadge />}
              </div>
              <div style={{ position: "relative" }}>
                <input
                  style={isPremium ? styles.input : styles.inputLocked}
                  type="number"
                  value={isPremium ? expiryDays : ""}
                  onChange={(e) => isPremium && setExpiryDays(e.target.value)}
                  placeholder={isPremium ? "30" : "Default expiry applies"}
                  disabled={!isPremium}
                />
                {!isPremium && (
                  <span style={{
                    position: "absolute", right: 10, top: "50%",
                    transform: "translateY(-50%)", fontSize: 13,
                  }}>🔒</span>
                )}
              </div>
            </div>
          </div>

          <button
            onClick={createShortUrl}
            style={{
              marginTop: 14,
              background: "#6366f1", border: "none", borderRadius: 8,
              padding: "9px 18px", fontSize: 13, fontWeight: 500, width: "100%",
              color: "#fff", cursor: "pointer",
            }}
          >
            Shorten
          </button>

          {shortUrl && (
            <div style={{
              marginTop: 14,
              background: "rgba(99,102,241,0.08)",
              border: "0.5px solid rgba(99,102,241,0.25)",
              borderRadius: 10, padding: "14px 16px",
              display: "flex", alignItems: "center", justifyContent: "space-between",
            }}>
              <div>
                <a href={shortUrl.shortUrl} target="_blank" rel="noreferrer"
                  style={{ fontSize: 14, color: "#818cf8", fontWeight: 500 }}>
                  {shortUrl.shortUrl}
                </a>
                <div style={{ fontSize: 11, color: "#475569", marginTop: 3 }}>
                  Expires: {formatDate(shortUrl.expiresAt)} — {shortUrl.message}
                </div>
              </div>
              <button
                onClick={copyLink}
                style={{
                  background: "rgba(99,102,241,0.15)",
                  border: "0.5px solid rgba(99,102,241,0.3)",
                  borderRadius: 6, padding: "5px 10px",
                  fontSize: 11, color: "#818cf8", cursor: "pointer",
                }}
              >
                {copied ? "✓ Copied" : "Copy"}
              </button>
            </div>
          )}
        </div>

        {/* URLs table */}
        <div style={styles.section}>
          <div style={{ fontSize: 13, fontWeight: 500, color: "#94a3b8", textTransform: "uppercase", letterSpacing: "0.06em", marginBottom: 16 }}>
            My URLs
          </div>
          {urls.length === 0 ? (
            <div style={{ textAlign: "center", padding: 32, color: "#334155", fontSize: 13 }}>
              No URLs yet — create your first one above
            </div>
          ) : (
            <table style={{ width: "100%", borderCollapse: "collapse", tableLayout: "fixed" }}>
              <thead>
                <tr>
                  <th style={{ fontSize: 11, color: "#334155", textTransform: "uppercase", letterSpacing: "0.06em", padding: "0 12px 10px", textAlign: "left", fontWeight: 500, width: "15%" }}>Short link</th>
                  <th style={{ fontSize: 11, color: "#334155", textTransform: "uppercase", letterSpacing: "0.06em", padding: "0 12px 10px", textAlign: "left", fontWeight: 500, width: "45%" }}>Original URL</th>
                  <th style={{ fontSize: 11, color: "#334155", textTransform: "uppercase", letterSpacing: "0.06em", padding: "0 12px 10px", textAlign: "left", fontWeight: 500, width: "15%" }}>Clicks</th>
                  <th style={{ fontSize: 11, color: "#334155", textTransform: "uppercase", letterSpacing: "0.06em", padding: "0 12px 10px", textAlign: "left", fontWeight: 500, width: "25%" }}>Expires</th>
                  <th style={{ width: "15%" }}></th>
                </tr>
              </thead>
              <tbody>
                {urls.map((url) => (
                  <tr key={url.shortUrl} style={{ borderTop: "0.5px solid rgba(255,255,255,0.05)" }}>
                    <td style={{ padding: "11px 12px", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                      <a
                        href={`http://localhost:8080/v1/url/${url.shortUrl}`}
                        target="_blank"
                        rel="noreferrer"
                        style={{ fontSize: 13, color: "#818cf8", fontWeight: 500, textDecoration: "none" }}
                        onMouseEnter={(e) => (e.target.style.textDecoration = "underline")}
                        onMouseLeave={(e) => (e.target.style.textDecoration = "none")}
                      >
                        {url.shortUrl}
                      </a>
                    </td>
                    <td style={{ padding: "11px 12px", fontSize: 12, color: "#334155", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                      {url.longUrl}
                    </td>
                    <td style={{ padding: "11px 12px", fontSize: 12, color: "#f1f5f9", fontWeight: 600 }}>
                      {url.clickCount ?? 0}
                    </td>
                    <td style={{ padding: "11px 12px" }}>
                      <span style={{
                        background: "rgba(255,255,255,0.04)",
                        border: "0.5px solid rgba(255,255,255,0.08)",
                        borderRadius: 6, padding: "3px 8px",
                        fontSize: 11, color: "#475569",
                      }}>
                        {formatDate(url.expiresAt)}
                      </span>
                    </td>
                    <td style={{ padding: "11px 12px" }}>
                      <button
                        onClick={() => deleteUrl(url.shortUrl)}
                        style={{
                          background: "rgba(239,68,68,0.08)",
                          border: "0.5px solid rgba(239,68,68,0.2)",
                          borderRadius: 6, padding: "5px 10px",
                          fontSize: 11, color: "#f87171", cursor: "pointer",
                        }}
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}
