import { Outlet, Link } from "react-router-dom";

export default function App() {
  return (
    <div style={{
      width: "100vw",
      height: "100vh",
      display: "flex",
      flexDirection: "column"
    }}>
      {/* Navigation */}
      <nav className="nav">
        <Link to="/">Dashboard</Link>
        <Link to="/tariffs">Tariff Calc</Link>
        <Link to="/history">History</Link>
        <Link to="/simulation">Simulation</Link>
        <Link to="/profile">Profile</Link>
        <Link to="/login" className="nav-right">Login</Link>
      </nav>

      {/* Main content */}
      <main style={{ flex: 1, padding: "2rem", overflowY: "auto" }}>
        <Outlet />
      </main>

      {/* Footer */}
      <footer style={{
        padding: "1rem",
        borderTop: "1px solid #ddd",
        textAlign: "center"
      }}>
        TARIFF • CS203 Project
      </footer>
    </div>
  );
}
