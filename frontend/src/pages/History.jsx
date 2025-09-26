import React, { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

export default function History() {
  const summary = {
    route: "CN → SG",
    avgCost: "$2,395.50",
    saved: "$1,245.00",
  };

  const historyData = [
    { date: "13/09/25", route: "CN → SG", product: "Electronics", total: "$2,458.00" },
    { date: "02/09/25", route: "VN → SG", product: "Textiles", total: "$1,245.00" },
    { date: "30/08/25", route: "US → SG", product: "Machinery", total: "$8,567.00" },
    { date: "29/08/25", route: "CN → SG", product: "Electronics", total: "$100.00" },
  ];

  // Graph state
  const [historicalData, setHistoricalData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [graphFilters, setGraphFilters] = useState({
    productCode: '',
    startDate: '2020-01-01',
    endDate: new Date().toISOString().split('T')[0]
  });

  // Fetch historical tariff data
  const fetchHistoricalData = async () => {
    try {
      setLoading(true);
      const response = await fetch('http://localhost:8080/api/tariff/history', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(graphFilters)
      });
      
      if (!response.ok) throw new Error('Failed to fetch data');
      
      const data = await response.json();
      setHistoricalData(data);
    } catch (error) {
      console.error('Error fetching tariff history:', error);
      // Fallback to mock data for demo
      setHistoricalData([
        { product_code: 'electronics', origin_country: 'CN', dest_country: 'SG', rate_percent: 5.0, valid_from: '2024-01-01' },
        { product_code: 'electronics', origin_country: 'CN', dest_country: 'SG', rate_percent: 5.5, valid_from: '2024-03-01' },
        { product_code: 'electronics', origin_country: 'CN', dest_country: 'SG', rate_percent: 4.8, valid_from: '2024-06-01' },
        { product_code: 'textiles', origin_country: 'VN', dest_country: 'SG', rate_percent: 3.2, valid_from: '2024-01-01' },
        { product_code: 'textiles', origin_country: 'VN', dest_country: 'SG', rate_percent: 3.5, valid_from: '2024-05-01' },
      ]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHistoricalData();
  }, []);

  // Format data for the chart
  const formatChartData = () => {
    const formattedData = {};
    
    historicalData.forEach(entry => {
      const date = new Date(entry.valid_from).toLocaleDateString('en-GB'); // DD/MM/YY format
      
      if (!formattedData[date]) {
        formattedData[date] = { date };
      }
      
      const key = entry.origin_country && entry.dest_country 
        ? `${entry.product_code} (${entry.origin_country}→${entry.dest_country})`
        : `${entry.product_code} (Default)`;
      
      formattedData[date][key] = entry.rate_percent;
    });
    
    return Object.values(formattedData);
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
            <input
              type="text"
              placeholder="Product (electronics, textiles...)"
              value={graphFilters.productCode}
              onChange={(e) => setGraphFilters({...graphFilters, productCode: e.target.value})}
            />
            <input
              type="date"
              value={graphFilters.startDate}
              onChange={(e) => setGraphFilters({...graphFilters, startDate: e.target.value})}
            />
            <input
              type="date"
              value={graphFilters.endDate}
              onChange={(e) => setGraphFilters({...graphFilters, endDate: e.target.value})}
            />
            <button type="button" onClick={fetchHistoricalData}>Apply</button>
          </div>
        </div>

        {/* Graph */}
        {loading ? (
          <div className="loading">Loading tariff history...</div>
        ) : (
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={chartData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="date" />
              <YAxis label={{ value: 'Rate %', angle: -90, position: 'insideLeft' }} />
              <Tooltip 
                formatter={(value) => [`${value}%`, 'Tariff Rate']}
                labelFormatter={(label) => `Date: ${label}`}
              />
              <Legend />
              
              {chartData.length > 0 && Object.keys(chartData[0])
                .filter(key => key !== 'date')
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

      {/* Filters for History Table */}
      <div className="card filter-bar">
        <span className="filter-label">Filter Calculations:</span>
        <div className="filter-actions">
          <button type="button">Date</button>
          <button type="button">Country</button>
          <button type="button">Product</button>
        </div>
        <input type="text" placeholder="Search..." />
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