import { Outlet, Link, useNavigate } from "react-router-dom";
import { jwtDecode } from "jwt-decode";
import { useState } from "react";

export default function App() {
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);

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

  return (
    <div
      style={{
        width: "100vw",
        height: "100vh",
        display: "flex",
        flexDirection: "column",
      }}
    >
      {/* ---------- Navigation ---------- */}
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

          <div className="logout-btn">
            {isLoggedIn ? (
              <button onClick={handleLogout}>Logout</button>
            ) : (
              <Link to="/login">Login</Link>
            )}
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

      {/* ---------- Main content ---------- */}
      <main style={{ flex: 1, padding: "2rem", overflowY: "auto" }}>
        <Outlet />
      </main>

      {/* ---------- Footer ---------- */}
      <footer
        style={{
          padding: "1rem",
          borderTop: "1px solid #ddd",
          textAlign: "center",
        }}
      >
        TARIFF • CS203 Project
      </footer>
    </div>
  );
}
