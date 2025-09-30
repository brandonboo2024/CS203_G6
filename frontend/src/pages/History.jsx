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
import { getPastCalculations } from '../api/PastCalculationsApi';
import { API_BASE_URL, getHeaders } from '../api/config';

export default function History() {
  // Summary data
  const summary = {
    route: "CN → SG",
    avgCost: "$2,395.50",
    saved: "$1,245.00",
  };

  // State for past calculations
  const [pastCalculations, setPastCalculations] = useState([]);
  const [calculationsLoading, setCalculationsLoading] = useState(true);
  const [calculationsError, setCalculationsError] = useState(null);

  const [historicalData, setHistoricalData] = useState([]);
  const [loading, setLoading] = useState(true);

  // Default selected product: electronics
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

  const countryOptions = [
    'SG', 'US', 'MY', 'TH', 'VN', 'ID', 'PH', 'KR',
    'IN', 'AU', 'GB', 'DE', 'FR', 'IT', 'ES', 'CA'
  ];

  // Fetch past calculations
  useEffect(() => {
    const fetchPastCalculations = async () => {
      try {
        setCalculationsLoading(true);
        const data = await getPastCalculations();
        console.log('Past calculations data:', data);
        if (!data) {
          throw new Error('No data received from API');
        }
        setPastCalculations(data);
      } catch (err) {
        console.error('Failed to load past calculations:', err);
        setCalculationsError(
          err.message === 'No data received from API' 
            ? 'No calculation history found' 
            : 'Failed to load calculation history. Please try again later.'
        );
      } finally {
        setCalculationsLoading(false);
      }
    };

    fetchPastCalculations();
  }, []);

  // Fetch historical tariff data
  const fetchHistoricalData = async () => {
    try {
      setLoading(true);
      console.log('Fetching with filters:', graphFilters);
      const response = await fetch(`${API_BASE_URL}/api/calculations/tariff-history`, {
        method: "POST",
        headers: getHeaders(),
        body: JSON.stringify(graphFilters),
      });
      
      if (!response.ok) {
        const errorText = await response.text();
        console.error('Tariff history error:', errorText);
        throw new Error(`Failed to fetch data: ${response.status} ${response.statusText}`);
      }
      
      const result = await response.json();
      console.log('Received tariff data:', result);
      setHistoricalData(Array.isArray(result) ? result : result.data || []);
    } catch (err) {
      console.error('Tariff history error:', err);
      setHistoricalData([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHistoricalData();
  }, [graphFilters]);

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
        ) : chartData.length === 0 ? (
          <div>No tariff history data available</div>
        ) : (
          <div style={{ width: '100%', height: 300 }}>
            <ResponsiveContainer>
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
                {Object.keys(chartData[0])
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
          </div>
        )}

        {/* Export CSV */}
        <button onClick={exportToCSV} className="export-btn">Export CSV</button>
      </div>

      {/* Past Calculations */}
      <div className="card">
        <h2>Your Past Calculations</h2>
        {calculationsLoading ? (
          <div className="loading">Loading your calculations...</div>
        ) : calculationsError ? (
          <div className="error">{calculationsError}</div>
        ) : pastCalculations.length === 0 ? (
          <p>No calculations found. Try making some calculations first!</p>
        ) : (
          <table className="history-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Item Price</th>
                <th>Tariff Rate</th>
                <th>Total Fees</th>
                <th>Total Price</th>
              </tr>
            </thead>
            <tbody>
              {pastCalculations.map((calc) => (
                <tr key={calc.id}>
                  <td>{new Date(calc.calculationTime).toLocaleDateString()}</td>
                  <td>${calc.itemPrice.toFixed(2)}</td>
                  <td>{calc.tariffRate.toFixed(2)}%</td>
                  <td>
                    ${(calc.handlingFee + calc.inspectionFee + 
                       calc.processingFee + calc.otherFees).toFixed(2)}
                  </td>
                  <td>${calc.totalPrice.toFixed(2)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
