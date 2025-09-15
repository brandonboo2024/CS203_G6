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
      <nav style={{ padding: "1rem", borderBottom: "1px solid #ddd" }}>
        <Link to="/" style={{ marginRight: "1rem" }}>Dashboard</Link>
        <Link to="/tariffs" style={{ marginRight: "1rem" }}>Tariff Calc</Link>
        <Link to="/profile" style={{ marginRight: "1rem" }}>Profile</Link>
        <Link to="/login" style={{ float: "right" }}>Login</Link>
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
