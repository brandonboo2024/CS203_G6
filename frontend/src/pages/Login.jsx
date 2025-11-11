import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { validateForm, sanitizeInput } from "../utils/inputValidation";

const TradeMarkIcon = () => (
  <svg
    viewBox="0 0 64 64"
    role="img"
    aria-label="TariffKey globe icon"
    xmlns="http://www.w3.org/2000/svg"
  >
    <defs>
      <linearGradient id="login-logo-gradient" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stopColor="#2563eb" />
        <stop offset="100%" stopColor="#f97316" />
      </linearGradient>
    </defs>
    <circle cx="32" cy="32" r="24" fill="url(#login-logo-gradient)" opacity="0.15" />
    <circle cx="32" cy="32" r="20" stroke="#2563eb" strokeWidth="2.2" fill="none" />
    <path
      d="M32 12c6 4.8 9.5 11.9 9.5 20S38 47.2 32 52c-6-4.8-9.5-11.9-9.5-20S26 16.8 32 12z"
      stroke="#2563eb"
      strokeWidth="2.2"
      fill="none"
    />
    <path d="M14 32h36" stroke="#2563eb" strokeWidth="2" strokeLinecap="round" />
    <path d="M18 24h28M18 40h28" stroke="#f97316" strokeWidth="1.6" strokeLinecap="round" />
  </svg>
);

export default function Login() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [validationErrors, setValidationErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setValidationErrors({});

    // Validate form data
    const formData = { username, password };
    const validation = validateForm(formData, "login");
    if (!validation.isValid) {
      setValidationErrors(validation.errors);
      setLoading(false);
      return;
    }

    try {
      const res = await fetch(`${import.meta.env.VITE_API_BASE_URL}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          username: sanitizeInput(username),
          password: sanitizeInput(password),
        }),
      });

      if (!res.ok) {
        try {
          const errorData = await res.json();
          throw new Error(errorData.error || errorData.message || "Login failed");
        } catch (parseError) {
          throw new Error("Login failed");
        }
      }

      const data = await res.json();

      localStorage.setItem("token", data.token);
      localStorage.setItem("username", data.username);
      localStorage.setItem("email", data.email);
      if (data.role) localStorage.setItem("role", data.role);

      if (data.role === "ADMIN") {
        navigate("/admin/tariffs");
      } else {
        navigate("/dashboard");
      }
    } catch (err) {
      alert("Invalid username or password");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-wrapper">
      <div className="login-card">
        <section className="login-hero">
          <div className="login-brand">
            <span className="login-logo">
              <TradeMarkIcon />
            </span>
            <div>
              <p className="login-eyebrow">Tariff simulator</p>
              <h2>TariffKey</h2>
            </div>
          </div>
          <p className="login-tagline">
            Explore tariff stories with a friendly dashboard powered by WITS/UNCTAD data.
          </p>
          <ul className="login-perks">
            <li>Quick rate previews</li>
            <li>Country + HS search helpers</li>
            <li>Visual insights at a glance</li>
          </ul>
          <div className="login-badge">
            <strong>180+</strong>
            <span>economies covered</span>
          </div>
        </section>

        <section className="login-content">
          <h1>Welcome back</h1>
          <p className="login-subtitle">Sign in to keep comparing tariffs in seconds.</p>

          <form onSubmit={handleSubmit} className="login-form">
            <div className="form-row">
              <label htmlFor="username">Username</label>
              <div>
                <input
                  type="text"
                  id="username"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  required
                  placeholder="e.g. trade.analyst"
                />
              </div>
            </div>

            <div className="form-row">
              <label htmlFor="password">Password</label>
              <div>
                <input
                  type="password"
                  id="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  placeholder="••••••••"
                />
              </div>
            </div>

            <div className="form-actions">
              <button type="submit" disabled={loading}>
                {loading ? "Logging in..." : "Login"}
              </button>
              <p className="register-link">
                Don&apos;t have an account? <a href="/register">Create one</a>
              </p>
            </div>
          </form>

          {Object.keys(validationErrors).length > 0 && (
            <div className="validation-errors" role="alert">
              <h4>Please fix the following:</h4>
              {Object.entries(validationErrors).map(([field, errors]) => (
                <div key={field} className="validation-errors__group">
                  <strong>{field}</strong>
                  <ul>
                    {errors.map((error, index) => (
                      <li key={index}>{error}</li>
                    ))}
                  </ul>
                </div>
              ))}
            </div>
          )}
        </section>
      </div>
    </div>
  );
}
