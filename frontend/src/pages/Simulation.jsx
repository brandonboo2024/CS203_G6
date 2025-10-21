import { useState } from "react";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid,
} from "recharts";

export default function Compare() {
  const [filters, setFilters] = useState({
    productCode: "electronics",
    destCountry: "ALL", // default is all destinations
    date: new Date().toISOString().split("T")[0],
  });

  const [compareData, setCompareData] = useState([]);
  const [loading, setLoading] = useState(false);

  const productOptions = [
    "automotive", "beauty", "books", "clothing", "electronics", "food",
    "furniture", "sports", "tools", "toys",
  ];

  const countryOptions = [
    "AU","BR","CA","CN","DE","ES","FR","GB","IN",
    "IT","JP","KR","MX","MY","PH","RU","SG","TH",
    "US","VN","ZA",
  ];

  const handleCompare = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);

      const res = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/tariff/history`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          productCode: filters.productCode,
          originCountry: "", // all origins
          destCountry: filters.destCountry === "ALL" ? "" : filters.destCountry,
          startDate: filters.date,
          endDate: filters.date,
        }),
      });

      if (!res.ok) throw new Error("Failed to fetch comparison");
      const data = await res.json();

      // Filter valid rows
      const valid = data.filter(
        (d) =>
          d.origin_country &&
          d.rate_percent !== undefined &&
          (filters.destCountry === "ALL" || d.dest_country === filters.destCountry)
      );

      const sorted = valid.sort((a, b) => a.rate_percent - b.rate_percent);
      setCompareData(sorted);

    } catch (err) {
      console.error(err);
      setCompareData([]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <h2>Compare Tariffs</h2>
      <p>
        Find which origin country offers the lowest tariff rate for your selected product and destination.
      </p>

      <form onSubmit={handleCompare} className="calc-form">
        <div className="form-row">
          <label>Product:</label>
          <select
            value={filters.productCode}
            onChange={(e) =>
              setFilters({ ...filters, productCode: e.target.value })
            }
          >
            {productOptions.map((p) => (
              <option key={p} value={p}>
                {p.charAt(0).toUpperCase() + p.slice(1)}
              </option>
            ))}
          </select>
        </div>

        <div className="form-row">
          <label>Destination:</label>
          <select
            value={filters.destCountry}
            onChange={(e) =>
              setFilters({ ...filters, destCountry: e.target.value })
            }
          >
            <option value="ALL">All Destinations</option>
            {countryOptions.map((c) => (
              <option key={c} value={c}>{c}</option>
            ))}
          </select>
        </div>

        <div className="form-row">
          <label>Date:</label>
          <input
            type="date"
            value={filters.date}
            onChange={(e) =>
              setFilters({ ...filters, date: e.target.value })
            }
          />
        </div>

        <button type="submit">Compare Tariffs</button>
      </form>

      {loading ? (
        <div className="result-box">Loading comparison...</div>
      ) : compareData.length > 0 ? (
        <div className="result-box">
          <h2>Comparison Results</h2>
          <ResponsiveContainer width="100%" height={350}>
            <BarChart
              data={compareData}
              margin={{ top: 20, right: 30, left: 20, bottom: 50 }}
            >
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis
                dataKey="origin_country"
                label={{ value: "Origin", position: "bottom", dy: 10 }}
              />
              <YAxis label={{ value: "Rate %", angle: -90, position: "insideLeft" }} />
              <Tooltip
                formatter={(v) => `${v}%`}
                labelFormatter={(label, payload) => {
                  const dest = payload[0]?.payload?.dest_country || "Unknown";
                  return `Origin: ${label}, Destination: ${dest}`;
                }}
              />
              <Bar dataKey="rate_percent" fill="#4a90e2" radius={[5, 5, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>

          <table className="history-table" style={{ marginTop: "1rem" }}>
            <thead>
              <tr>
                <th>Rank</th>
                <th>Origin</th>
                <th>Destination</th>
                <th>Rate (%)</th>
              </tr>
            </thead>
            <tbody>
              {compareData.map((row, i) => (
                <tr key={i}>
                  <td>{i + 1}</td>
                  <td>{row.origin_country}</td>
                  <td>{row.dest_country || "N/A"}</td>
                  <td>{row.rate_percent}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <div className="result-box">No data found for the selected filters.</div>
      )}
    </div>
  );
}
