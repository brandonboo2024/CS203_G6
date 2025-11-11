import { useState, useEffect } from "react";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid,
} from "recharts";

// Import the same shared components used in TariffCalc
import CountryDropdown from "../components/CountryDropdown.jsx";
import ProductDropdown from "../components/ProductDropdown.jsx";

export default function Simulation() {
  // --- Profitability simulation state ---
  const [inputs, setInputs] = useState({
    productionCost: '',
    sellingPrice: '',
    tariffRate: '',
    newTariff: '',
    quantity: '',
  });
  const [result, setResult] = useState(null);

  // --- Comparison state ---
  const [filters, setFilters] = useState({
    productCode: "",
    toCountry: "", // Only need destination country
  });
  const [compareData, setCompareData] = useState([]);
  const [loading, setLoading] = useState(false);
  
  // NEW: Store the context of the last comparison to fix the display issue
  const [lastComparisonContext, setLastComparisonContext] = useState({
    productCode: "",
    toCountry: "",
    productLabel: "",
    countryLabel: ""
  });

  // Use same lookup data structure as TariffCalc
  const [lookups, setLookups] = useState({
    reporters: [], // All origin countries
    validOrigins: [], // Origins that have the selected destination as partner
    products: [], // Products available between valid origins and destination
  });
  const [reportersLoading, setReportersLoading] = useState(true);
  const [originsLoading, setOriginsLoading] = useState(false);
  const [productsLoading, setProductsLoading] = useState(false);
  const [lookupError, setLookupError] = useState(null);

  const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || "").replace(/\/$/, "");

  // Same data fetching logic as TariffCalc
  const fetchLookupJson = async (path) => {
    try {
      const response = await fetch(`${apiBaseUrl}${path}`);
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      
      const payload = await response.json();
      return payload;
    } catch (error) {
      console.error(`Failed to fetch ${path}:`, error);
      throw error;
    }
  };

  // Load reporters (ALL origin countries) on mount
  useEffect(() => {
    const loadReporters = async () => {
      setReportersLoading(true);
      try {
        const data = await fetchLookupJson("/api/lookups");
        setLookups((prev) => ({
          ...prev,
          reporters: data?.reporters ?? [],
        }));
        setLookupError(null);
      } catch (err) {
        console.error(err);
        setLookupError(
          "Unable to load country options. Please try again later."
        );
        setLookups((prev) => ({ ...prev, reporters: [] }));
      } finally {
        setReportersLoading(false);
      }
    };
    loadReporters();
  }, [apiBaseUrl]);

  // CORRECTED: Find all origin countries that have the selected destination as a partner
  useEffect(() => {
    if (!filters.toCountry) {
      setLookups((prev) => ({ ...prev, validOrigins: [], products: [] }));
      return;
    }

    const findValidOrigins = async () => {
      setOriginsLoading(true);
      setLookups((prev) => ({ ...prev, validOrigins: [], products: [] }));
      
      try {
        const validOrigins = [];
        
        // Check each reporter to see if they have the destination as a partner
        for (const reporter of lookups.reporters) {
          if (!reporter.code) continue;
          
          try {
            const partners = await fetchLookupJson(
              `/api/lookups/reporters/${reporter.code}/partners`
            );
            
            // Check if the selected destination is in this reporter's partners
            const hasDestination = Array.isArray(partners) && 
              partners.some(partner => partner.code === filters.toCountry);
            
            if (hasDestination) {
              validOrigins.push(reporter);
            }
          } catch (err) {
            // If we can't fetch partners for this reporter, skip it
            console.warn(`Could not fetch partners for ${reporter.code}:`, err);
          }
        }
        
        setLookups((prev) => ({ ...prev, validOrigins }));
        setLookupError(null);
        
      } catch (err) {
        console.error(err);
        setLookupError("Failed to find valid origin countries.");
        setLookups((prev) => ({ ...prev, validOrigins: [] }));
      } finally {
        setOriginsLoading(false);
      }
    };

    findValidOrigins();
  }, [filters.toCountry, lookups.reporters, apiBaseUrl]);

  // CORRECTED: Load products using the first valid origin (they should all have similar products)
  useEffect(() => {
    if (!filters.toCountry || lookups.validOrigins.length === 0) {
      setLookups((prev) => ({ ...prev, products: [] }));
      return;
    }

    const loadProducts = async () => {
      setProductsLoading(true);
      
      try {
        // Use the first valid origin to load products for this destination
        const firstValidOrigin = lookups.validOrigins[0];
        const products = await fetchLookupJson(
          `/api/lookups/reporters/${firstValidOrigin.code}/partners/${filters.toCountry}/products`
        );
        
        setLookups((prev) => ({ ...prev, products: products || [] }));
        setLookupError(null);
        
      } catch (err) {
        console.error(err);
        setLookupError("Could not load products for the selected countries.");
        setLookups((prev) => ({ ...prev, products: [] }));
      } finally {
        setProductsLoading(false);
      }
    };

    loadProducts();
  }, [filters.toCountry, lookups.validOrigins, apiBaseUrl]);

  // --- Profitability handlers ---
  const handleChange = (field, value) => {
    // Handle empty string and remove leading zeros
    if (value === '') {
      setInputs({ ...inputs, [field]: '' });
    } else {
      // Remove leading zeros and convert to number
      const numericValue = Number(value.replace(/^0+/, ''));
      setInputs({ ...inputs, [field]: numericValue });
    }
  };

  const handleSimulate = (e) => {
    e.preventDefault();
    
    // Check if all required fields are filled (allow 0 for tariffs)
    const { productionCost, sellingPrice, tariffRate, newTariff, quantity } = inputs;
    
    if (productionCost === '' || sellingPrice === '' || tariffRate === '' || newTariff === '' || quantity === '') {
      alert("Please fill in all fields for the simulation.");
      return;
    }

    // Convert to numbers and allow 0 values
    const prodCost = Number(productionCost);
    const sellPrice = Number(sellingPrice);
    const currentTariff = Number(tariffRate);
    const simulatedTariff = Number(newTariff);
    const qty = Number(quantity);

    if (prodCost <= 0 || sellPrice <= 0 || qty <= 0) {
      alert("Production cost, selling price, and quantity must be greater than 0.");
      return;
    }

    // Tariffs can be 0 or positive
    if (currentTariff < 0 || simulatedTariff < 0) {
      alert("Tariff rates cannot be negative.");
      return;
    }

    const revenue = sellPrice * qty;
    const costBefore = prodCost * qty * (1 + currentTariff / 100);
    const costAfter = prodCost * qty * (1 + simulatedTariff / 100);

    const profitBefore = revenue - costBefore;
    const profitAfter = revenue - costAfter;
    const marginBefore = (profitBefore / revenue) * 100;
    const marginAfter = (profitAfter / revenue) * 100;
    const diff = profitAfter - profitBefore;

    const breakevenPrice = prodCost * (1 + simulatedTariff / 100);

    setResult({
      revenue,
      costBefore,
      costAfter,
      profitBefore,
      profitAfter,
      marginBefore,
      marginAfter,
      diff,
      breakevenPrice,
      chartData: [
        { name: "Before Tariff", profit: profitBefore },
        { name: "After Tariff", profit: profitAfter },
      ],
    });
  };

  // CORRECTED: Compare only the valid origins that have this destination as partner
// CORRECTED: Compare only origins that have actual tariff data for this specific product
const handleCompare = async (e) => {
  e.preventDefault();
  
  if (!filters.toCountry || !filters.productCode) {
    alert("Please select destination country and product to compare tariffs.");
    return;
  }

  setLoading(true);
  setCompareData([]);
  setLookupError(null);
  
  try {
    const token = localStorage.getItem("token");
    const headers = { "Content-Type": "application/json" };
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }

    const comparisonResults = [];
    
    // Only compare origins that are valid (have this destination as partner)
    for (const origin of lookups.validOrigins) {
      if (!origin.code) continue;
      
      try {
        const calculationPayload = {
          fromCountry: origin.code,
          toCountry: filters.toCountry,
          product: filters.productCode,
          quantity: 1,
          calculationFrom: new Date().toISOString(),
          calculationTo: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
          handling: false,
          inspection: false,
          processing: false,
          others: false
        };

        const res = await fetch(`${apiBaseUrl}/api/tariff/calculate`, {
          method: "POST",
          headers,
          body: JSON.stringify(calculationPayload),
        });

        let tariffRate = 0;
        let label = `Rate from ${origin.label || origin.code}`;

        if (res.ok) {
          const data = await res.json();
          tariffRate = data.tariff_rate ?? data.tariffRate ?? 0;
          label = data.label || label;
        } else {
          // If calculation fails, try to extract tariff data from error response
          try {
            const errorData = await res.json();
            tariffRate = errorData.tariff_rate ?? errorData.tariffRate ?? 0;
            label = errorData.label || label;
          } catch {
            // If we can't parse error response, skip this origin
            continue;
          }
        }

        // 🔥 CRITICAL FIX: Only include countries that have ACTUAL tariff data
        // Skip countries that return 0% AND where the API call actually failed
        // This prevents showing countries that have no data for this specific product
        const hasValidData = res.ok || (tariffRate > 0);
        
        if (hasValidData) {
          comparisonResults.push({
            origin_country: origin.code,
            origin_label: origin.label || origin.code,
            dest_country: filters.toCountry,
            product_code: filters.productCode,
            rate_percent: tariffRate,
            label: label,
          });
        }
      } catch (err) {
        console.warn(`Failed to get rate from ${origin.code}:`, err);
        // Continue with other origins
      }

      // Small delay to avoid overwhelming the API
      await new Promise(resolve => setTimeout(resolve, 50));
    }

    // Sort by lowest rate first
    const validResults = comparisonResults.sort((a, b) => a.rate_percent - b.rate_percent);

    // FIX: Store the context of this comparison
    const currentProductLabel = getLabel(filters.productCode, lookups.products);
    const currentCountryLabel = getLabel(filters.toCountry, lookups.reporters);
    
    setLastComparisonContext({
      productCode: filters.productCode,
      toCountry: filters.toCountry,
      productLabel: currentProductLabel,
      countryLabel: currentCountryLabel
    });

    if (validResults.length === 0) {
      setLookupError("No tariff data found for the selected destination and product.");
    } else {
      setCompareData(validResults);
      setLookupError(null);
    }

  } catch (err) {
    console.error("Comparison error:", err);
    setLookupError("Failed to load comparison data. Please try again.");
  } finally {
    setLoading(false);
  }
};

  // Helper function to get label from code
  const getLabel = (code, collection) => {
    if (!code || !Array.isArray(collection)) return code;
    const match = collection.find((entry) => entry.code === code);
    return match?.label || code;
  };

  return (
    <div className="page">
      {/* --- Profitability Impact Analyzer --- */}
      <h2>Profitability Impact Simulation</h2>
      <p>Simulate how tariff changes affect your total profit, cost, and margin.</p>

      <form onSubmit={handleSimulate} className="calc-form">
        <div className="form-row">
          <label>Production Cost (per unit):</label>
          <input
            type="number"
            min="0.01"
            step="0.01"
            value={inputs.productionCost}
            onChange={(e) => handleChange("productionCost", e.target.value)}
            placeholder="Enter production cost"
            required
          />
        </div>
        <div className="form-row">
          <label>Selling Price (per unit):</label>
          <input
            type="number"
            min="0.01"
            step="0.01"
            value={inputs.sellingPrice}
            onChange={(e) => handleChange("sellingPrice", e.target.value)}
            placeholder="Enter selling price"
            required
          />
        </div>
        <div className="form-row">
          <label>Quantity:</label>
          <input
            type="number"
            min="1"
            value={inputs.quantity}
            onChange={(e) => handleChange("quantity", e.target.value)}
            placeholder="Enter quantity"
            required
          />
        </div>
        <div className="form-row">
          <label>Current Tariff %:</label>
          <input
            type="number"
            min="0"
            step="0.01"
            value={inputs.tariffRate}
            onChange={(e) => handleChange("tariffRate", e.target.value)}
            placeholder="Enter current tariff rate (0% is allowed)"
            required
          />
        </div>
        <div className="form-row">
          <label>Simulated Tariff %:</label>
          <input
            type="number"
            min="0"
            step="0.01"
            value={inputs.newTariff}
            onChange={(e) => handleChange("newTariff", e.target.value)}
            placeholder="Enter new tariff rate (0% is allowed)"
            required
          />
        </div>
        <button type="submit">Run Profit Simulation</button>
      </form>

      {result && (
        <div className="result-box">
          <h2 style={{ color: "#fff" }}>Simulation Result</h2>
          
          {/* Key Metrics Summary */}
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
            gap: '1rem',
            marginBottom: '1.5rem',
            padding: '1rem',
            backgroundColor: 'rgba(255,255,255,0.05)',
            borderRadius: '8px'
          }}>
            <div>
              <div style={{ fontSize: '0.9rem', color: '#cfd8dc' }}>Revenue</div>
              <div style={{ fontSize: '1.2rem', fontWeight: 'bold' }}>${result.revenue.toFixed(2)}</div>
            </div>
            <div>
              <div style={{ fontSize: '0.9rem', color: '#cfd8dc' }}>Profit Before</div>
              <div style={{ 
                fontSize: '1.2rem', 
                fontWeight: 'bold',
                color: result.profitBefore >= 0 ? '#aed581' : '#ff6b6b'
              }}>
                ${result.profitBefore.toFixed(2)}
              </div>
            </div>
            <div>
              <div style={{ fontSize: '0.9rem', color: '#cfd8dc' }}>Profit After</div>
              <div style={{ 
                fontSize: '1.2rem', 
                fontWeight: 'bold',
                color: result.profitAfter >= 0 ? '#aed581' : '#ff6b6b'
              }}>
                ${result.profitAfter.toFixed(2)}
              </div>
            </div>
            <div>
              <div style={{ fontSize: '0.9rem', color: '#cfd8dc' }}>Change</div>
              <div style={{ 
                fontSize: '1.2rem', 
                fontWeight: 'bold',
                color: result.diff >= 0 ? '#aed581' : '#ff6b6b'
              }}>
                {result.diff >= 0 ? '+' : ''}${result.diff.toFixed(2)}
              </div>
            </div>
          </div>

          {/* Detailed Breakdown */}
          <div style={{ marginBottom: '1.5rem' }}>
            <p><strong>Cost Breakdown:</strong></p>
            <p>Cost Before Tariff Change: ${result.costBefore.toFixed(2)}</p>
            <p>Cost After Tariff Change: ${result.costAfter.toFixed(2)}</p>
            <p>Cost Increase: ${(result.costAfter - result.costBefore).toFixed(2)}</p>
            <p>
              Margin Before: <span style={{ color: result.marginBefore >= 0 ? '#aed581' : '#ff6b6b' }}>
                {result.marginBefore.toFixed(2)}%
              </span>
            </p>
            <p>
              Margin After: <span style={{ color: result.marginAfter >= 0 ? '#aed581' : '#ff6b6b' }}>
                {result.marginAfter.toFixed(2)}%
              </span>
            </p>
            <p>Breakeven Price After Tariff: ${result.breakevenPrice.toFixed(2)} per unit</p>
          </div>

 {/* Profit Comparison Chart */}
<div className="chart-wrapper">
  <h3 style={{ color: "#fff", marginBottom: "1rem" }}>Profit Comparison</h3>
  <ResponsiveContainer width="100%" height={300}>
    <BarChart 
      data={result.chartData} 
      margin={{ top: 20, right: 30, left: 20, bottom: 30 }}
    >
      <CartesianGrid strokeDasharray="3 3" stroke="#444" />
      <XAxis 
        dataKey="name" 
        tick={{ fill: "var(--text-light)" }}
      />
      <YAxis 
        tick={{ fill: "var(--text-light)" }}
        tickFormatter={(value) => `$${value}`}
        // Remove domain prop - let Recharts auto-scale
      />
      <Tooltip
        formatter={(value) => [`$${Number(value).toFixed(2)}`, "Profit"]}
        labelFormatter={(label) => `${label}`}
        contentStyle={{ 
          backgroundColor: "var(--bg-card)", 
          borderRadius: "8px",
          border: "1px solid #555"
        }}
      />
      <Bar 
        dataKey="profit" 
        name="Profit"
        fill="#FF8C00"
        radius={[4, 4, 0, 0]} 
      />
    </BarChart>
  </ResponsiveContainer>
  
  {/* Chart Legend */}
  <div style={{ 
    display: 'flex', 
    justifyContent: 'center', 
    gap: '2rem', 
    marginTop: '1rem',
    fontSize: '0.9rem'
  }}>
    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
      <div style={{ width: '12px', height: '12px', backgroundColor: '#FF8C00', borderRadius: '2px' }}></div>
      <span style={{ color: 'var(--text-light)' }}>Profit/Loss</span>
    </div>
  </div>
</div>

          {/* Warning for negative profit */}
          {result.profitAfter < 0 && (
            <div style={{
              backgroundColor: 'rgba(244, 67, 54, 0.1)',
              border: '1px solid #f44336',
              color: '#ff6b6b',
              padding: '1rem',
              borderRadius: '8px',
              marginTop: '1rem'
            }}>
              <strong>⚠️ Warning:</strong> The new tariff rate would make this product unprofitable. 
              Consider increasing your selling price to at least ${result.breakevenPrice.toFixed(2)} per unit 
              to break even.
            </div>
          )}
        </div>
      )}

      {/* --- Tariff Comparison --- */}
      <h2 style={{ marginTop: "3rem" }}>Compare Tariff Rates</h2>
      <p>
        Compare tariff rates from countries that have trade relationships with your selected destination.
        Find which origin country offers the lowest tariff rate for your product.
      </p>

      {lookupError && (
        <div className="error-banner">
          {lookupError}
        </div>
      )}

      <form onSubmit={handleCompare} className="calc-form">
        <CountryDropdown
          label="Destination Country"
          value={filters.toCountry}
          onChange={(value) => setFilters({ toCountry: value, productCode: "" })}
          options={lookups.reporters}
          disabled={reportersLoading}
          loading={reportersLoading}
          placeholder="Select destination country"
        />

        <div className="form-row">
          <label>Valid Origin Countries:</label>
          <div style={{ color: 'var(--text-light)', fontSize: '0.9rem', marginTop: '0.5rem' }}>
            {originsLoading ? 'Finding countries with trade relationships...' :
             lookups.validOrigins.length > 0 ? 
               `${lookups.validOrigins.length} countries found` :
             filters.toCountry ? 'No trade relationships found' :
             'Select destination first'}
          </div>
        </div>

        <ProductDropdown
          label="Product"
          value={filters.productCode}
          onChange={(value) => setFilters({ ...filters, productCode: value })}
          options={lookups.products}
          disabled={productsLoading || !filters.toCountry || lookups.validOrigins.length === 0}
          loading={productsLoading}
          placeholder={
            !filters.toCountry ? "Choose destination first" :
            originsLoading ? "Finding valid origins..." :
            lookups.validOrigins.length === 0 ? "No trade relationships found" :
            productsLoading ? "Loading products..." :
            "Select product"
          }
        />

        <button 
          type="submit" 
          disabled={loading || !filters.toCountry || !filters.productCode || lookups.validOrigins.length === 0}
        >
          {loading ? `Comparing ${lookups.validOrigins.length} Countries...` : "Compare Tariff Rates"}
        </button>
      </form>

      {!filters.toCountry && (
        <div className="result-box">
          Select a destination country to find countries with trade relationships and compare tariff rates.
        </div>
      )}

      {filters.toCountry && lookups.validOrigins.length === 0 && !originsLoading && (
        <div className="result-box">
          No trade relationships found for the selected destination country. Try selecting a different destination.
        </div>
      )}

      {loading ? (
        <div className="result-box">
          <p>Loading tariff rates from {lookups.validOrigins.length} countries...</p>
          <p>This may take a moment as we check each origin country.</p>
        </div>
      ) : compareData.length > 0 ? (
        <div className="result-box">
          <h2 style={{ color: "#fff" }}>Tariff Comparison Results</h2>
          
          {/* ✅ FIXED: Use the stored comparison context instead of current filters */}
          <p>
            Showing tariff rates for <strong>{lastComparisonContext.productLabel}</strong> from{" "}
            <strong>{compareData.length} countries</strong> to{" "}
            <strong>{lastComparisonContext.countryLabel}</strong>
          </p>

          {/* Best Options Summary */}
          {compareData.length >= 3 && (
            <div style={{
              backgroundColor: '#1c2a3a',
              border: '1px solid #4caf50',
              padding: '1rem',
              borderRadius: '8px',
              marginBottom: '1.5rem'
            }}>
              <h3 style={{ color: '#aed581', marginTop: 0 }}>Best Options</h3>
              <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
                {compareData.slice(0, 3).map((item, index) => (
                  <div key={index} style={{ flex: 1, minWidth: '120px' }}>
                    <div style={{ fontSize: '0.9rem', color: '#cfd8dc' }}>
                      #{index + 1} {item.origin_label}
                    </div>
                    <div style={{ fontSize: '1.1rem', color: '#aed581', fontWeight: 'bold' }}>
                      {item.rate_percent.toFixed(1)}%
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          <div className="chart-wrapper">
            <ResponsiveContainer width="100%" height={400}>
              <BarChart 
                data={compareData} 
                margin={{ top: 20, right: 30, left: 20, bottom: 80 }}
              >
                <CartesianGrid stroke="#333" strokeDasharray="3 3" />
                <XAxis
                  dataKey="origin_label"
                  angle={-45}
                  textAnchor="end"
                  height={80}
                  tick={{ fill: "var(--text-light)", fontSize: 12 }}
                />
                <YAxis
                  tick={{ fill: "var(--text-light)" }}
                  label={{
                    value: "Tariff Rate %",
                    angle: -90,
                    dx: -10,
                    position: "middle",
                    fill: "var(--text-light)",
                  }}
                />
                <Tooltip
                  formatter={(value, name) => [`${value}%`, "Tariff Rate"]}
                  labelFormatter={(label, payload) => {
                    if (payload && payload[0]) {
                      const data = payload[0].payload;
                      return `From: ${data.origin_label}\nTo: ${lastComparisonContext.countryLabel}\nRate: ${data.rate_percent}%`;
                    }
                    return label;
                  }}
                  contentStyle={{
                    backgroundColor: "var(--bg-card)",
                    color: "var(--text-light)",
                    borderRadius: "8px",
                    whiteSpace: 'pre-line'
                  }}
                />
                <Bar 
                  dataKey="rate_percent" 
                  name="Tariff Rate" 
                  fill="#FF8C00"
                  radius={[5, 5, 0, 0]} 
                />
              </BarChart>
            </ResponsiveContainer>
          </div>

          <div className="table-scroll">
            <table className="history-table" style={{ marginTop: "1rem" }}>
              <thead>
                <tr>
                  <th>Rank</th>
                  <th>Origin Country</th>
                  <th>Tariff Rate (%)</th>
                </tr>
              </thead>
              <tbody>
                {compareData.map((row, i) => (
                  <tr key={i}>
                    <td>{i + 1}</td>
                    <td>{row.origin_label}</td>
                    <td style={{ 
                      fontWeight: i < 3 ? 'bold' : 'normal',
                      color: i < 3 ? '#aed581' : 'inherit'
                    }}>
                      {row.rate_percent.toFixed(2)}%
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        compareData.length === 0 && !loading && filters.toCountry && filters.productCode && (
          <div className="result-box">
            No tariff data found for the selected destination and product combination.
          </div>
        )
      )}
    </div>
  );
}
