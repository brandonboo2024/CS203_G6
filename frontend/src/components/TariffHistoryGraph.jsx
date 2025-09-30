import React, { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const TariffHistoryGraph = () => {
  const [historicalData, setHistoricalData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({
    productCode: '',
    startDate: '2020-01-01',
    endDate: new Date().toISOString().split('T')[0]
  });

  // Fetch historical tariff data
  const fetchHistoricalData = async (currentFilters) => {
    try {
      setLoading(true);
      const response = await fetch('http://localhost:8080/api/tariff/history', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(currentFilters)  // use latest filters
      });
      
      if (!response.ok) throw new Error('Failed to fetch data');
      
      const data = await response.json();
      setHistoricalData(data);
    } catch (error) {
      console.error('Error fetching tariff history:', error);
    } finally {
      setLoading(false);
    }
  };

  // Load initial data
  useEffect(() => {
    fetchHistoricalData(filters);
  }, []);

  const formatChartData = () => {
    const formattedData = {};
    
    historicalData.forEach(entry => {
      const date = new Date(entry.valid_from).toLocaleDateString();
      
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

  if (loading) return <div className="loading">Loading tariff history...</div>;

  return (
    <div className="tariff-history-graph">
      <h2>Historical Tariff Rates</h2>
      
      {/* Filters */}
      <div className="filters">
        <input
          type="text"
          placeholder="Product Code (optional)"
          value={filters.productCode}
          onChange={(e) => setFilters({...filters, productCode: e.target.value})}
        />
        <input
          type="date"
          value={filters.startDate}
          onChange={(e) => setFilters({...filters, startDate: e.target.value})}
        />
        <input
          type="date"
          value={filters.endDate}
          onChange={(e) => setFilters({...filters, endDate: e.target.value})}
        />
        <button onClick={() => fetchHistoricalData(filters)}>Apply Filters</button>
      </div>

      {/* Graph */}
      <ResponsiveContainer width="100%" height={400}>
        <LineChart data={chartData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="date" />
          <YAxis label={{ value: 'Rate (%)', angle: -90, position: 'insideLeft' }} />
          <Tooltip />
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
    </div>
  );
};

export default TariffHistoryGraph;
