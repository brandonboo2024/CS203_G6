import { useState } from "react";
import "../App.css";
import { validateForm, sanitizeInput } from "../utils/inputValidation";

// ✅ import your new shared components
import CountryDropdown from "../components/CountryDropdown.jsx";
import ProductDropdown from "../components/ProductDropdown.jsx";

export default function TariffCalc() {
  const [fromCountry, setFromCountry] = useState("");
  const [toCountry, setToCountry] = useState("");
  const [product, setProduct] = useState("");
  const [quantity, setQuantity] = useState("");
  const [calculationFrom, setFrom] = useState("");
  const [calculationTo, setTo] = useState("");
  const [fees, setFees] = useState({
    handling: false,
    inspection: false,
    processing: false,
    others: false,
  });
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const [validationErrors, setValidationErrors] = useState({});
  const [loading, setLoading] = useState(false);

  const toggleFee = (key) => {
    setFees((prev) => ({ ...prev, [key]: !prev[key] }));
  };

  const toIso = (local) => (local ? new Date(local).toISOString() : null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setValidationErrors({});
    
    // Validate form data
    const formData = {
      fromCountry,
      toCountry,
      product,
      quantity,
      calculationFrom,
      calculationTo
    };
    
    const validation = validateForm(formData, 'tariff');
    if (!validation.isValid) {
      setValidationErrors(validation.errors);
      setLoading(false);
      return;
    }
    
    if(!calculationFrom || !calculationTo){
        setError("Please select both start and end date/time.");
        setLoading(false);
        return;
    }
    const fromDate = new Date(calculationFrom);
    const toDate = new Date(calculationTo);
    if(fromDate > toDate){
        setError("End date/time must be after start date/time.");
        setLoading(false);
        return;
    }
    
    // Sanitize inputs before sending
    // const request = {
    //     fromCountry: sanitizeInput(fromCountry),
    //     toCountry: sanitizeInput(toCountry),
    //     product: sanitizeInput(product),
    //     quantity: parseInt(quantity),
    //     handling: fees.handling,
    //     inspection: fees.inspection,
    //     processing: fees.processing,
    //     others: fees.others,
    //     calculationFrom: toIso(calculationFrom),
    //     calculationTo: toIso(calculationTo),
    // };
    //
    // if (!calculationFrom || !calculationTo) {
    //   setError("Please select both start and end date/time.");
    //   return;
    // }
    //
    // const fromDate = new Date(calculationFrom);
    // const toDate = new Date(calculationTo);
    // if (fromDate > toDate) {
    //   setError("End date/time must be after start date/time.");
    //   return;
    // }

    const request = {
      fromCountry,
      toCountry,
      product,
      quantity: parseInt(quantity),
      handling: fees.handling,
      inspection: fees.inspection,
      processing: fees.processing,
      others: fees.others,
      calculationFrom: toIso(calculationFrom),
      calculationTo: toIso(calculationTo),
    };

    try {
      const token = localStorage.getItem("token");
      localStorage.getItem("token")         // is it non-null?
      console.log(atob(localStorage.getItem("token").split('.')[1])); // check roles/authorities in payload     
      console.log("Submitting:", request);
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL}/api/tariff/calculate`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json",
          Authorization: `Bearer ${token}`},
          body: JSON.stringify(request),
        }
      );

      if (!response.ok) throw new Error("Request failed");
      const data = await response.json();

      // normalize snake_case to camelCase
      const breakdown = {
        itemPrice: data.itemPrice ?? data.item_price ?? 0,
        tariffRate: data.tariffRate ?? data.tariff_rate ?? 0,
        tariffAmount: data.tariffAmount ?? data.tariff_amount ?? 0,
        handlingFee: data.handlingFee ?? data.handling_fee ?? 0,
        processingFee: data.processingFee ?? data.processing_fee ?? 0,
        inspectionFee: data.inspectionFee ?? data.inspection_fee ?? 0,
        otherFees: data.otherFees ?? data.other_fees ?? 0,
        totalPrice: data.totalPrice ?? data.total_price ?? 0,
        from: data.from ?? 0,
        to: data.to ?? 0,
      };

      const segments = Array.isArray(data.segments) ? data.segments : [];

      setResult({
        breakdown,
        segments,
        window: { from: request.calculationFrom, to: calculationTo },
      });

      // ---- Save local history ----
      const historyEntry = {
        createdAt: new Date().toISOString(),
        route: `${fromCountry} → ${toCountry}`,
        product,
        total: Number(breakdown.totalPrice ?? 0),
        tariffFrom: request.calculationFrom,
        tariffTo: request.calculationTo,
      };
      const prev = JSON.parse(localStorage.getItem("calcHistory") || "[]");
      prev.unshift(historyEntry);
      const trimmed = prev.slice(0, 50);
      localStorage.setItem("calcHistory", JSON.stringify(trimmed));
      
      setLoading(false);
    } catch (err) {
      console.error(err);
      setError(err.message || "Something went wrong.");
      setLoading(false);
    }
  };

  const getCountryName = (code) => {
    const countries = {
      SG: "Singapore",
      US: "United States",
      MY: "Malaysia",
      TH: "Thailand",
      VN: "Vietnam",
      ID: "Indonesia",
      PH: "Philippines",
      KR: "South Korea",
      IN: "India",
      AU: "Australia",
      GB: "United Kingdom",
      DE: "Germany",
      FR: "France",
      IT: "Italy",
      ES: "Spain",
      CA: "Canada",
      BR: "Brazil",
      MX: "Mexico",
      RU: "Russia",
      ZA: "South Africa",
      CN: "China",
      JP: "Japan",
    };
    return countries[code] || code;
  };

  const fmtDate = (iso) => {
    if (!iso) return "-";
    try {
      return new Date(iso).toLocaleString();
    } catch {
      return iso;
    }
  };

  return (
    <div className="calc-wrapper">
      <div className="calc-card">
        <h1>Tariff Calculator</h1>

        <form onSubmit={handleSubmit} className="calc-form">
          {/* ✅ Replaced long dropdowns with shared components */}
          <CountryDropdown
            label="Origin"
            value={fromCountry}
            onChange={setFromCountry}
          />
          <CountryDropdown
            label="Destination"
            value={toCountry}
            onChange={setToCountry}
          />
          <ProductDropdown value={product} onChange={setProduct} />

          {/* Quantity */}
          <div className="form-row">
            <label>Quantity:</label>
            <input
              type="number"
              min="1"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
              placeholder="Enter quantity"
            />
          </div>

          {/* Time window */}
          <div className="form-row">
            <label>From:</label>
            <input
              type="datetime-local"
              value={calculationFrom}
              onChange={(e) => setFrom(e.target.value)}
              required
            />
          </div>
          <div className="form-row">
            <label>To:</label>
            <input
              type="datetime-local"
              value={calculationTo}
              onChange={(e) => setTo(e.target.value)}
              required
            />
          </div>

          {/* Fees */}
          <div className="form-row">
            <label>Fees:</label>
            <div className="fees">
              {Object.keys(fees).map((key) => (
                <label key={key}>
                  <input
                    type="checkbox"
                    checked={fees[key]}
                    onChange={() => toggleFee(key)}
                  />{" "}
                  {key.charAt(0).toUpperCase() + key.slice(1)} Fee
                </label>
              ))}
            </div>
          </div>


          <button type="submit" disabled={loading}>
            {loading ? 'Calculating...' : 'Calculate'}
          </button>
        </form>

        {/* Display validation errors */}
        {Object.keys(validationErrors).length > 0 && (
          <div className="validation-errors" style={{ 
            marginTop: '1rem', 
            padding: '1rem', 
            backgroundColor: '#ffebee', 
            border: '1px solid #f44336', 
            borderRadius: '4px' 
          }}>
            <h4 style={{ color: '#d32f2f', margin: '0 0 0.5rem 0' }}>Please fix the following errors:</h4>
            {Object.entries(validationErrors).map(([field, errors]) => (
              <div key={field} style={{ marginBottom: '0.5rem' }}>
                <strong style={{ textTransform: 'capitalize', color: '#d32f2f' }}>
                  {field.replace(/([A-Z])/g, ' $1').trim()}:
                </strong>
                <ul style={{ margin: '0.25rem 0 0 1rem', padding: 0 }}>
                  {errors.map((error, index) => (
                    <li key={index} style={{ color: '#d32f2f', fontSize: '0.9rem' }}>
                      {error}
                    </li>
                  ))}
                </ul>
              </div>
            ))}
          </div>
        )}

        {result && (() => {
          // Calculate total based on only selected fees
          const calculatedTotal = 
            Number(result.breakdown.itemPrice || 0) +
            Number(result.breakdown.tariffAmount || 0) +
            (fees.handling ? Number(result.breakdown.handlingFee || 0) : 0) +
            (fees.processing ? Number(result.breakdown.processingFee || 0) : 0) +
            (fees.inspection ? Number(result.breakdown.inspectionFee || 0) : 0) +
            (fees.others ? Number(result.breakdown.otherFees || 0) : 0);

          return (
          <div className="results-wrapper" style={{ marginTop: '2rem' }}>
            <h2 style={{ color: '#fff', fontSize: '1.5rem', fontWeight: 'bold', marginBottom: '1rem' }}>Results</h2>
            
            {/* Total Import Cost Card - Orange Theme */}
            <div style={{
              backgroundColor: '#FF8C00',
              borderRadius: '8px',
              padding: '1.5rem',
              marginBottom: '2rem',
              textAlign: 'left'
            }}>
              <div style={{ color: '#000', fontSize: '1rem', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                Total Import Cost
              </div>
              <div style={{ color: '#000', fontSize: '2rem', fontWeight: 'bold', marginBottom: '0.5rem' }}>
                ${calculatedTotal.toFixed(2)}
              </div>
              <div style={{ color: '#000', fontSize: '0.9rem' }}>
                {getCountryName(fromCountry)} → {getCountryName(toCountry)}
              </div>
            </div>

            {/* Cost Breakdown Section */}
            <h3 style={{ color: '#fff', fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '1rem' }}>
              Cost Breakdown
            </h3>
            <div style={{
              backgroundColor: '#000',
              border: '1px solid #FF8C00',
              padding: '1.5rem',
              borderRadius: '8px',
              marginBottom: '1.5rem'
            }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                {/* Product Value */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ color: '#fff', fontSize: '1rem' }}>Product Value:</span>
                  <span style={{ color: '#fff', fontSize: '1rem', fontWeight: '500' }}>
                    ${Number(result.breakdown.itemPrice || 0).toFixed(2)}
                  </span>
                </div>

                {/* Tariff */}
                {result.breakdown.tariffAmount > 0 && (
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ color: '#fff', fontSize: '1rem' }}>
                      Tariff ({Number(result.breakdown.tariffRate || 0).toFixed(1)}%):
                    </span>
                    <span style={{ color: '#fff', fontSize: '1rem', fontWeight: '500' }}>
                      ${Number(result.breakdown.tariffAmount || 0).toFixed(2)}
                    </span>
                  </div>
                )}

                {/* Handling Fee - only show if selected */}
                {fees.handling && result.breakdown.handlingFee > 0 && (
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ color: '#fff', fontSize: '1rem' }}>Handling Fee:</span>
                    <span style={{ color: '#fff', fontSize: '1rem', fontWeight: '500' }}>
                      ${Number(result.breakdown.handlingFee || 0).toFixed(2)}
                    </span>
                  </div>
                )}

                {/* Processing Fee - only show if selected */}
                {fees.processing && result.breakdown.processingFee > 0 && (
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ color: '#fff', fontSize: '1rem' }}>Processing Fee:</span>
                    <span style={{ color: '#fff', fontSize: '1rem', fontWeight: '500' }}>
                      ${Number(result.breakdown.processingFee || 0).toFixed(2)}
                    </span>
                  </div>
                )}

                {/* Inspection Fee - only show if selected */}
                {fees.inspection && result.breakdown.inspectionFee > 0 && (
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ color: '#fff', fontSize: '1rem' }}>Inspection Fee:</span>
                    <span style={{ color: '#fff', fontSize: '1rem', fontWeight: '500' }}>
                      ${Number(result.breakdown.inspectionFee || 0).toFixed(2)}
                    </span>
                  </div>
                )}

                {/* Miscellaneous Charges - only show if selected */}
                {fees.others && result.breakdown.otherFees > 0 && (
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ color: '#fff', fontSize: '1rem' }}>Miscellaneous Charges:</span>
                    <span style={{ color: '#fff', fontSize: '1rem', fontWeight: '500' }}>
                      ${Number(result.breakdown.otherFees || 0).toFixed(2)}
                    </span>
                  </div>
                )}

                {/* Separator Line */}
                <hr style={{ 
                  border: 'none', 
                  borderTop: '1px solid #fff', 
                  margin: '1rem 0',
                  width: '100%',
                  opacity: 0.3
                }} />

                {/* Total Cost */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ color: '#fff', fontSize: '1.1rem', fontWeight: 'bold' }}>Total Cost:</span>
                  <span style={{ color: '#fff', fontSize: '1.1rem', fontWeight: 'bold' }}>
                    ${calculatedTotal.toFixed(2)}
                  </span>
                </div>
              </div>
            </div>

            {/* Action Buttons */}
            <div style={{ 
              display: 'flex', 
              gap: '1rem', 
              justifyContent: 'center',
              marginTop: '1.5rem'
            }}>
              <button
                type="button"
                onClick={() => {
                  // Save result functionality
                  const historyEntry = {
                    createdAt: new Date().toISOString(),
                    route: `${fromCountry} → ${toCountry}`,
                    product,
                    total: Number(result.breakdown.totalPrice || 0),
                    tariffFrom: result.window.from,
                    tariffTo: result.window.to,
                    breakdown: result.breakdown
                  };
                  const prev = JSON.parse(localStorage.getItem("calcHistory") || "[]");
                  prev.unshift(historyEntry);
                  const trimmed = prev.slice(0, 50);
                  localStorage.setItem("calcHistory", JSON.stringify(trimmed));
                  alert("Result saved successfully!");
                }}
                style={{
                  padding: '0.75rem 1.5rem',
                  borderRadius: '6px',
                  border: 'none',
                  backgroundColor: '#808080',
                  color: '#fff',
                  fontSize: '1rem',
                  cursor: 'pointer',
                  fontWeight: '500'
                }}
              >
                Save Result
              </button>
              
              <button
                type="button"
                onClick={() => {
                  // Export PDF functionality - placeholder
                  alert("PDF export functionality coming soon!");
                }}
                style={{
                  padding: '0.75rem 1.5rem',
                  borderRadius: '6px',
                  border: 'none',
                  backgroundColor: '#808080',
                  color: '#fff',
                  fontSize: '1rem',
                  cursor: 'pointer',
                  fontWeight: '500'
                }}
              >
                Export PDF
              </button>
              
              <button
                type="button"
                onClick={() => {
                  // Reset form and result
                  setResult(null);
                  setFromCountry("");
                  setToCountry("");
                  setProduct("");
                  setQuantity("");
                  setFrom("");
                  setTo("");
                  setFees({
                    handling: false,
                    inspection: false,
                    processing: false,
                    others: false,
                  });
                  setError(null);
                }}
                style={{
                  padding: '0.75rem 1.5rem',
                  borderRadius: '6px',
                  border: 'none',
                  backgroundColor: '#808080',
                  color: '#fff',
                  fontSize: '1rem',
                  cursor: 'pointer',
                  fontWeight: '500'
                }}
              >
                New Calculation
              </button>
            </div>
          </div>
          );
        })()}
      </div>
    </div>
  );
}
