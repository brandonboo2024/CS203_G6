import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useCalcHistory } from "../hooks/useCalcHistory.jsx";

export default function Dashboard() {
  const userName = localStorage.getItem("username") || "Guest";
  const [news, setNews] = useState([]);
  const { history } = useCalcHistory();
  const navigate = useNavigate();
  const recentCalculations = history.slice(0, 3);

  const fmtShortDate = (iso) => {
    if (!iso) return "-";
    try {
      return new Date(iso).toLocaleDateString("en-GB", {
        day: "2-digit",
        month: "2-digit",
        year: "2-digit",
      });
    } catch {
      return iso;
    }
  };

  const totals = useMemo(() => {
    const totalValue = history.reduce(
      (sum, calc) => sum + Number(calc.total || 0),
      0
    );
    const uniqueRoutes = new Set(history.map((calc) => calc.route)).size;
    return {
      totalRuns: history.length,
      avgValue: history.length ? totalValue / history.length : 0,
      routes: uniqueRoutes,
    };
  }, [history]);

  const headlineRoute = recentCalculations[0]?.route || "Awaiting route";
  const highlightNews = news.slice(0, 4);

  useEffect(() => {
    // replace with localhost:8080 if testing locally
    fetch("https://cs203g6-production-170f.up.railway.app/api/news")
      .then((res) => res.json())
      .then((data) => setNews(data))
      .catch((err) => console.error("Error fetching news:", err));
  }, []);
  return (
    <div className="dashboard-wrapper">
      <section className="dashboard-hero">
        <div className="hero-text">
          <p className="hero-eyebrow">Operations overview</p>
          <h1 className="hero-title">
            Welcome back <span className="highlight">{userName}</span>
          </h1>
          <p>
            Track active routes, monitor landed costs, and jump back into the
            journeys you touched last.
          </p>
          <div className="hero-meta">
            <span className="stat-pill">
              <span className="pill-dot" />
              {totals.totalRuns ? `${totals.totalRuns} quotes saved` : "Start your first quote"}
            </span>
            <span className="chip">Latest lane: {headlineRoute}</span>
          </div>
          <div className="quick-actions">
            <button className="pill-button" onClick={() => navigate("/tariffs")}>
              New calculation
            </button>
            <button className="pill-button" onClick={() => navigate("/simulation")}>
              Open simulation
            </button>
          </div>
        </div>
        <div className="hero-metrics">
          <div className="metric-card">
            <span>Avg landed cost</span>
            <strong>${totals.avgValue.toFixed(2)}</strong>
            <small className="chip">Last run: {fmtShortDate(recentCalculations[0]?.createdAt)}</small>
          </div>
          <div className="metric-card">
            <span>Routes tracked</span>
            <strong>{totals.routes}</strong>
            <small className="chip">Session view</small>
          </div>
        </div>
      </section>

      <section className="card">
        <h2>Recent calculations</h2>
        <div className="table-scroll">
          <table className="calc-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Route</th>
                <th>Product</th>
                <th>Total</th>
              </tr>
            </thead>
            <tbody>
              {recentCalculations.length === 0 ? (
                <tr>
                  <td colSpan="4" style={{ textAlign: "center" }}>
                    No calculations yet this session.
                  </td>
                </tr>
              ) : (
                recentCalculations.map((row, idx) => (
                  <tr key={`${row.createdAt}-${idx}`}>
                    <td>{fmtShortDate(row.createdAt)}</td>
                    <td>{row.route}</td>
                    <td>{row.product}</td>
                    <td>${Number(row.total || 0).toFixed(2)}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>

      <section className="card">
        <h2>Latest trade & tariff news</h2>
        <p className="news-subtext">Curated from global trade and tariff headlines</p>
        <p className="last-updated">
          Last refreshed {new Date().toLocaleString("en-US", {
            month: "short",
            day: "numeric",
            year: "numeric",
            hour: "numeric",
            minute: "2-digit",
          })}
        </p>
        <div className="news-list">
          {highlightNews.length > 0 ? (
            highlightNews.map((item, idx) => (
              <div className="news-item" key={idx}>
                <span className="news-title">{item.title}</span>
                <div className="news-meta">
                  <a
                    href={item.url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="news-link"
                  >
                    Read more
                  </a>
                  <small className="news-date">
                    {item.date
                      ? new Date(item.date).toLocaleString("en-US", {
                          month: "short",
                          day: "numeric",
                          year: "numeric",
                          hour: "numeric",
                          minute: "2-digit",
                        })
                      : ""}
                  </small>
                </div>
              </div>
            ))
          ) : (
            <p>Loading latest news...</p>
          )}
        </div>
      </section>
    </div>
  );
}
