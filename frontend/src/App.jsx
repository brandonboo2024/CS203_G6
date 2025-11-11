import { Outlet, Link, useNavigate, useLocation } from "react-router-dom";
import { jwtDecode } from "jwt-decode";
import { useEffect, useState } from "react";
import Chatbot from "./components/Chatbot";

export default function App() {
  const navigate = useNavigate();
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);
  const [theme, setTheme] = useState(() => localStorage.getItem("tk-theme") || "daybreak");
  const isAuthPage = location.pathname === "/login" || location.pathname === "/register";

  const token = localStorage.getItem("token");
  let isAdmin = false;
  let isLoggedIn = false;

  if (token) {
    try {
      const decoded = jwtDecode(token);
      isAdmin = decoded.role === "ADMIN";
      isLoggedIn = true;
    } catch (err) {
      console.error("Invalid token:", err);
    }
  }

  const handleLogout = () => {
    localStorage.removeItem("token");
    setMenuOpen(false);
    navigate("/login");
  };

  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
    localStorage.setItem("tk-theme", theme);
  }, [theme]);

  const toggleTheme = () => {
    setTheme((prev) => (prev === "midnight" ? "daybreak" : "midnight"));
  };

  return (
    <div
      style={{
        width: "100%",
        minHeight: "100vh",
        display: "flex",
        flexDirection: "column",
      }}
    >
      {/* ---------- Navigation ---------- */}
      {!isAuthPage && (
        <nav className="nav">
          {/* ---------- Mobile Header ---------- */}
          <div className="nav-header">
            <button
              className={`menu-toggle ${!isLoggedIn ? "disabled" : ""}`}
              onClick={isLoggedIn ? () => setMenuOpen(!menuOpen) : undefined}
              title={!isLoggedIn ? "Login to access menu" : ""}
            >
              ☰
            </button>

            <div className="nav-actions">
              <button className="theme-toggle" onClick={toggleTheme}>
                {theme === "midnight" ? "🌙 Midnight" : "🌤 Daybreak"}
              </button>
              <div className="logout-btn">
                {isLoggedIn ? (
                  <button onClick={handleLogout}>Logout</button>
                ) : (
                  <Link to="/login">Login</Link>
                )}
              </div>
            </div>
          </div>

          {/* ---------- Nav Links ---------- */}
          <div className={`nav-links ${menuOpen && isLoggedIn ? "open" : ""}`}>
            <Link to="/dashboard" onClick={() => setMenuOpen(false)}>
              Dashboard
            </Link>
            <Link to="/tariffs" onClick={() => setMenuOpen(false)}>
              Tariff Calc
            </Link>
            <Link to="/history" onClick={() => setMenuOpen(false)}>
              History
            </Link>
            <Link to="/simulation" onClick={() => setMenuOpen(false)}>
              Simulation
            </Link>
            <Link to="/profile" onClick={() => setMenuOpen(false)}>
              Profile
            </Link>
            {isAdmin && (
              <Link to="/admin/tariffs" onClick={() => setMenuOpen(false)}>
                Admin
              </Link>
            )}
          </div>
        </nav>
      )}

      {/* ---------- Main content ---------- */}
      <main style={{ flex: 1, padding: isAuthPage ? "0" : "2rem", overflow: "visible" }}>
        <Outlet />
      </main>

      {/* ---------- Footer ---------- */}
      {!isAuthPage && <footer>TARIFF • CS203 Project</footer>}

      {/* ---------- Chatbot ---------- */}
      {!isAuthPage && isLoggedIn && <Chatbot />}
    </div>
  );
}
