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
  const historyData = [
    { date: "13/09/25", route: "CN → SG", product: "Electronics", total: "$2,458.00" },
    { date: "02/09/25", route: "VN → SG", product: "Textiles", total: "$1,245.00" },
    { date: "30/08/25", route: "US → SG", product: "Machinery", total: "$8,567.00" },
    { date: "29/08/25", route: "CN → SG", product: "Electronics", total: "$100.00" },
  ];

  const [historicalData, setHistoricalData] = useState([]);
  const [loading, setLoading] = useState(true);

  // Default selected product: electronics
  const [graphFilters, setGraphFilters] = useState({
    productCode: "electronics",
    startDate: "2020-01-01",
    endDate: new Date().toISOString().split("T")[0],
  });

  const productOptions = [
    "automotive", "beauty", "books", "clothing", "electronics", "food",
    "furniture", "sports", "tools", "toys",
  ];

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

  // Format chart data and clip by start/end filter dates
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

      const key =
        entry.origin_country && entry.dest_country
          ? `${entry.product_code} (${entry.origin_country}→${entry.dest_country})`
          : entry.product_code;

      // Clip start and end to filter range
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

        {/* Graph Filters */}
        <div className="filter-bar">
          <span className="filter-label">Graph Filters:</span>
          <div className="filter-actions">
            {/* Product Dropdown (no "All Products") */}
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
            {historyData.map((row, idx) => (
              <tr key={idx}>
                <td>{row.date}</td>
                <td>{row.route}</td>
                <td>{row.product}</td>
                <td>{row.total}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
