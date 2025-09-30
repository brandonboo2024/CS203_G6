// src/pages/History.jsx
import React, { useState, useEffect } from "react";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";

export default function History() {
  // Summary data
  const summary = {
    route: "CN → SG",
    avgCost: "$2,395.50",
    saved: "$1,245.00",
  };

  // Sample past calculations
  // const historyData = [
  //   { date: "13/09/25", route: "CN → SG", product: "Electronics", total: "$2,458.00" },
  //   { date: "02/09/25", route: "VN → SG", product: "Textiles", total: "$1,245.00" },
  //   { date: "30/08/25", route: "US → SG", product: "Machinery", total: "$8,567.00" },
  //   { date: "29/08/25", route: "CN → SG", product: "Electronics", total: "$100.00" },
  // ];

  const [historicalData, setHistoricalData] = useState([]);
  const [loading, setLoading] = useState(true);

  // Local history (from localStorage)
  const [historyData, setHistoryData] = useState([]);

  const productLabel = (code) => {
    const map = {
      electronics: "Electronics",
      clothing: "Clothing",
      furniture: "Furniture",
      food: "Food",
      books: "Books",
      toys: "Toys",
      tools: "Tools",
      beauty: "Beauty Products",
      sports: "Sports Equipment",
      automotive: "Automotive Parts",
    };
    return map[code] || code;
  };

  const fmtShortDate = (iso) => {
    try {
      // dd/MM/yy (en-GB) to match your UI
      return new Date(iso).toLocaleDateString("en-GB", { day: "2-digit", month: "2-digit", year: "2-digit" });
    } catch {
      return iso ?? "-";
    }
  };

  // Filters for country
  const [graphFilters, setGraphFilters] = useState({
    productCode: "electronics",
    originCountry: "",
    destCountry: "",
    startDate: "2020-01-01",
    endDate: new Date().toISOString().split("T")[0],
  });

  const productOptions = [
    "automotive", "beauty", "books", "clothing", "electronics", "food",
    "furniture", "sports", "tools", "toys",
  ];

  const countryOptions = ["CN", "US", "VN", "SG", "JP", "KR", "DE"];

  // Fetch historical tariff data
  const fetchHistoricalData = async () => {
    try {
      setLoading(true);
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/tariff/history`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(graphFilters),
      });
      if (!response.ok) throw new Error("Failed to fetch data");
      const result = await response.json();
      setHistoricalData(Array.isArray(result) ? result : result.data || []);
    } catch (err) {
      console.error(err);
      setHistoricalData([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHistoricalData();
  }, [graphFilters]);

  useEffect(() => {
    fetchHistoricalData();
  }, [graphFilters]);

  // add this as a separate effect (anywhere at top level in the component)
  useEffect(() => {
    const load = () => {
      const raw = localStorage.getItem("calcHistory");
      const rows = JSON.parse(raw || "[]");
      setHistoryData(rows);
    };
    load();

    // updates if another tab writes to localStorage
    const onStorage = (e) => {
      if (e.key === "calcHistory") load();
    };
    window.addEventListener("storage", onStorage);

    // optional: also refresh when the tab regains focus (same-tab updates)
    const onFocus = () => load();
    window.addEventListener("focus", onFocus);

    return () => {
      window.removeEventListener("storage", onStorage);
      window.removeEventListener("focus", onFocus);
    };
  }, []); // <- empty deps


  // Format chart data
  const formatChartData = () => {
    if (!Array.isArray(historicalData) || historicalData.length === 0) return [];
  
    const formatted = {};
    const globalStart = new Date(graphFilters.startDate);
    const globalEnd = new Date(graphFilters.endDate);
  
    historicalData.forEach((entry) => {
      const from = new Date(entry.valid_from);
      const to = entry.valid_to ? new Date(entry.valid_to) : globalEnd;
  
      // Skip entries completely outside the filter range
      if (to < globalStart || from > globalEnd) return;
  
      // Filter by product, origin, and destination
      if (graphFilters.productCode && entry.product_code !== graphFilters.productCode) return;
      if (graphFilters.originCountry && entry.origin_country !== graphFilters.originCountry) return;
      if (graphFilters.destCountry && entry.dest_country !== graphFilters.destCountry) return;
  
      const key =
        entry.origin_country && entry.dest_country
          ? `${entry.product_code} (${entry.origin_country}→${entry.dest_country})`
          : entry.product_code;
  
      const start = from < globalStart ? globalStart : from;
      const end = to > globalEnd ? globalEnd : to;
  
      const startLabel = start.toLocaleDateString("en-GB");
      if (!formatted[startLabel]) formatted[startLabel] = { date: startLabel };
      formatted[startLabel][key] = entry.rate_percent;
  
      const endLabel = end.toLocaleDateString("en-GB");
      if (!formatted[endLabel]) formatted[endLabel] = { date: endLabel };
      formatted[endLabel][key] = entry.rate_percent;
    });
  
    return Object.values(formatted).sort((a, b) => {
      const aDate = new Date(a.date.split("/").reverse().join("-"));
      const bDate = new Date(b.date.split("/").reverse().join("-"));
      return aDate - bDate;
    });
  };
  

  const chartData = formatChartData();

  // CSV export
  const exportToCSV = () => {
    if (chartData.length === 0) {
      alert("No data available to export.");
      return;
    }
    const headers = ["Date", ...Object.keys(chartData[0]).filter((k) => k !== "date")];
    const rows = chartData.map((row) =>
      [row.date, ...headers.slice(1).map((h) => row[h] ?? "")].join(",")
    );
    const csvContent = [headers.join(","), ...rows].join("\n");
    const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.setAttribute("href", url);
    link.setAttribute("download", "tariff_history.csv");
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div className="history-wrapper">
      {/* Summary */}
      <div className="card summary-card">
        <h2>Summary</h2>
        <ul>
          <li><span>Most Used Route:</span> {summary.route}</li>
          <li><span>Average Cost:</span> {summary.avgCost}</li>
          <li><span>Total Saved:</span> {summary.saved}</li>
        </ul>
      </div>

      {/* Tariff History Graph */}
      <div className="card">
        <h2>Tariff Rate History</h2>
        <p>Visualize how tariff rates have changed over time</p>

        {/* Filters */}
        <div className="filter-bar">
          <span className="filter-label">Graph Filters:</span>
          <div className="filter-actions">
            {/* Product */}
            <select
              value={graphFilters.productCode}
              onChange={(e) =>
                setGraphFilters({ ...graphFilters, productCode: e.target.value })
              }
            >
              {productOptions.map((product) => (
                <option key={product} value={product}>
                  {product.charAt(0).toUpperCase() + product.slice(1)}
                </option>
              ))}
            </select>

            {/* Origin Country */}
            <select
              value={graphFilters.originCountry}
              onChange={(e) =>
                setGraphFilters({ ...graphFilters, originCountry: e.target.value })
              }
            >
              <option value="">All Origins</option>
              {countryOptions.map((c) => (
                <option key={c} value={c}>{c}</option>
              ))}
            </select>

            {/* Destination Country */}
            <select
              value={graphFilters.destCountry}
              onChange={(e) =>
                setGraphFilters({ ...graphFilters, destCountry: e.target.value })
              }
            >
              <option value="">All Destinations</option>
              {countryOptions.map((c) => (
                <option key={c} value={c}>{c}</option>
              ))}
            </select>

            {/* Date filters */}
            <input
              type="date"
              value={graphFilters.startDate}
              onChange={(e) =>
                setGraphFilters({ ...graphFilters, startDate: e.target.value })
              }
            />
            <input
              type="date"
              value={graphFilters.endDate}
              onChange={(e) =>
                setGraphFilters({ ...graphFilters, endDate: e.target.value })
              }
            />
          </div>
        </div>

        {/* Graph */}
        {loading ? (
          <div className="loading">Loading tariff history...</div>
        ) : (
          <ResponsiveContainer width="100%" height={300}>
            <LineChart
              data={chartData}
              margin={{ top: 20, right: 30, left: 20, bottom: 5 }}
            >
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="date" />
              <YAxis label={{ value: "Rate %", angle: -90, position: "insideLeft" }} />
              <Tooltip
                formatter={(value) => [`${value}%`, "Tariff Rate"]}
                labelFormatter={(label) => `Date: ${label}`}
              />
              <Legend />
              {chartData.length > 0 &&
                Object.keys(chartData[0])
                  .filter((key) => key !== "date")
                  .map((key, index) => (
                    <Line
                      key={key}
                      type="monotone"
                      dataKey={key}
                      stroke={`hsl(${index * 60}, 70%, 50%)`}
                      strokeWidth={2}
                      dot={{ r: 4 }}
                      activeDot={{ r: 6 }}
                    />
                  ))}
            </LineChart>
          </ResponsiveContainer>
        )}

        {/* Export CSV */}
        <button onClick={exportToCSV} className="export-btn">Export CSV</button>
      </div>

      {/* History Table */}
      <div className="card">
        <h2>Past Calculations</h2>
        <table className="history-table">
          <thead>
            <tr>
              <th>Date</th>
              <th>Route</th>
              <th>Product</th>
              <th>Total</th>
            </tr>
          </thead>
          <tbody>
            {historyData.length === 0 ? (
              <tr><td colSpan="4" style={{ textAlign: "center" }}>No calculations yet.</td></tr>
            ) : (
              historyData.map((row, idx) => (
                <tr key={idx}>
                  <td>
                    {fmtShortDate(row.createdAt)}
                    <div className="muted" style={{ fontSize: "0.8em" }}>
                      {fmtShortDate(row.tariffFrom)} → {fmtShortDate(row.tariffTo)}
                    </div>
                  </td>
                  <td>{row.route}</td>
                  <td>{productLabel(row.product)}</td>
                  <td>${Number(row.total || 0).toFixed(2)}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
        <button
          className="secondary-btn"
          onClick={() => { localStorage.removeItem("calcHistory"); setHistoryData([]); }}
        >
          Clear History
        </button>
      </div>
    </div>
  );
}
