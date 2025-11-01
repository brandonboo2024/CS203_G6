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
    } catch (err) {
      console.error(err);
      setError(err.message || "Something went wrong.");
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

        {result && (
          <div className="results-wrapper">
            <h2>Results</h2>
            <div className="total-cost-card">
              <div className="total-label">Total Import Cost</div>
              <div className="total-amount">
                ${Number(result.breakdown.totalPrice || 0).toFixed(2)}
              </div>
              <div className="route">
                {getCountryName(fromCountry)} → {getCountryName(toCountry)}
              </div>
            </div>

            {/* Add your breakdown and segments sections below (unchanged) */}
          </div>
        )}
      </div>
    </div>
  );
}
