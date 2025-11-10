import { useEffect, useState } from "react";
import { useCalcHistory } from "../hooks/useCalcHistory.jsx";

export default function Dashboard() {
  const userName = localStorage.getItem("username") || "Guest";
  const [news, setNews] = useState([]);
  const { history } = useCalcHistory();
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

  useEffect(() => {
    // replace with localhost:8080 if testing locally
    fetch("https://cs203g6-production-170f.up.railway.app/api/news")
      .then((res) => res.json())
      .then((data) => setNews(data))
      .catch((err) => console.error("Error fetching news:", err));
  }, []);

  return (
    <div className="dashboard-wrapper">
      <h1>
        Welcome Back <span className="highlight">{userName}</span>!
      </h1>

      {/* =========================
          Recent Calculations
      ========================= */}
      <div className="card">
        <h2>Recent Calculations:</h2>
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

      {/* =========================
          Latest News
      ========================= */}
      <div className="card">
        <h2>Latest Trade & Tariff News</h2>
        <p className="news-subtext">Curated from global trade and tariff headlines</p>

        {/* 🕒 Last refreshed text */}
        <p className="last-updated">
          Last updated:{" "}
          {new Date().toLocaleString("en-US", {
            month: "short",
            day: "numeric",
            year: "numeric",
            hour: "numeric",
            minute: "2-digit",
          })}
        </p>

        <div className="news-list">
          {news.length > 0 ? (
            news.map((item, idx) => (
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
      </div>
    </div>
  );
}
