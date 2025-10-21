import { Outlet, Link, useNavigate } from "react-router-dom";
import { jwtDecode } from "jwt-decode";

export default function App() {
  const navigate = useNavigate();

  const token = localStorage.getItem("token");
  let isAdmin = false;

  if (token) {
    try {
      const decoded = jwtDecode(token);
      // Assuming your JWT payload looks like { sub: "user", role: "ADMIN" }
      isAdmin = decoded.role === "ADMIN";
    } catch (err) {
      console.error("Invalid token:", err);
    }
  }

  const handleLogout = () => {
    localStorage.removeItem("token"); // clear JWT
    navigate("/login"); // redirect to login
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
      {/* Navigation */}
      <nav className="nav" style={{ display: "flex", gap: "1rem" }}>
        <Link to="/dashboard">Dashboard</Link>
        <Link to="/tariffs">Tariff Calc</Link>
        <Link to="/history">History</Link>
        <Link to="/simulation">Simulation</Link>
        <Link to="/profile">Profile</Link>

        {isAdmin && <Link to="/admin/tariffs">Admin</Link>}

        <div style={{ marginLeft: "auto" }}>
          {localStorage.getItem("token") ? (
            <button onClick={handleLogout} style={{ cursor: "pointer" }}>
              Logout
            </button>
          ) : (
            <Link to="/login">Login</Link>
          )}
        </div>
      </nav>

      {/* Main content */}
      <main style={{ flex: 1, padding: "2rem", overflowY: "auto" }}>
        <Outlet />
      </main>

      {/* Footer */}
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
