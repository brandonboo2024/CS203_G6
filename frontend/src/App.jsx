import { Outlet, Link, useNavigate } from "react-router-dom";

export default function App() {
  const navigate = useNavigate();

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
