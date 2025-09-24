import { useState } from "react";
import "../App.css";

export default function TariffCalc() {
  const [fromCountry, setFromCountry] = useState("");
  const [toCountry, setToCountry] = useState("");
  const [product, setProduct] = useState("");
  const [quantity, setQuantity] = useState("");
  const [fees, setFees] = useState({
    handling: false,
    inspection: false,
    processing: false,
    others: false,
  });
  const [result, setResult] = useState(null);

  const toggleFee = (key) => {
    setFees((prev) => ({ ...prev, [key]: !prev[key] }));
  };

  const handleSubmit = async(e) => {
    e.preventDefault();
      const request = {
          fromCountry,
          toCountry,
          product,
          quantity: parseInt(quantity),
          handling: fees.handling,
          inspection: fees.inspection,
          processing: fees.processing,
          others: fees.others,
      };

      try {
          console.log("Submitting:", request);
          const response = await fetch("http://localhost:8080/api/tariff/calculate", {
              method: "POST",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify(request),
          });

          if (!response.ok) throw new Error("Request failed");

          const data = await response.json();

          // normalize snake_case to camelCase
          const breakdown = {
            itemPrice:   data.itemPrice   ?? data.item_price   ?? 0,
            tariffRate:     data.tariffRate     ?? data.tariff_rate     ?? 0,
            tariffAmount:   data.tariffAmount   ?? data.tariff_amount   ?? 0,
            handlingFee:    data.handlingFee    ?? data.handling_fee    ?? 0,
            processingFee:  data.processingFee  ?? data.processing_fee  ?? 0,
            inspectionFee:  data.inspectionFee  ?? data.inspection_fee  ?? 0,
            othersFee:      data.othersFee      ?? data.others_fee      ?? 0,
            totalPrice:      data.totalPrice      ?? data.total_price      ?? 0,
          };

          setResult({ breakdown });
      } catch (err) {
          console.error(err);
      }

  };
  // helper function to get country names
  const getCountryName = (code) => {
  const countries = {
    'SG': 'Singapore', 'US': 'United States', 'MY': 'Malaysia', 'TH': 'Thailand',
    'VN': 'Vietnam', 'ID': 'Indonesia', 'PH': 'Philippines', 'KR': 'South Korea',
    'IN': 'India', 'AU': 'Australia', 'GB': 'United Kingdom', 'DE': 'Germany',
    'FR': 'France', 'IT': 'Italy', 'ES': 'Spain', 'CA': 'Canada',
    'BR': 'Brazil', 'MX': 'Mexico', 'RU': 'Russia', 'ZA': 'South Africa',
    'CN': 'China', 'JP': 'Japan'
  };
  return countries[code] || code;
};

  return (
    <div className="calc-wrapper">
      <div className="calc-card">
        <h1>Tariff Calculator</h1>

        <form onSubmit={handleSubmit} className="calc-form">
          <div className="form-row">
            <label>Origin:</label>
            <select value={fromCountry} onChange={(e) => setFromCountry(e.target.value)}>
              <option value="" disabled>Select Origin Country</option>
              <option value="SG">Singapore</option>
              <option value="US">United States</option>
              <option value="MY">Malaysia</option>
              <option value="TH">Thailand</option>
              <option value="VN">Vietnam</option>
              <option value="ID">Indonesia</option>
              <option value="PH">Philippines</option>
              <option value="KR">South Korea</option>
              <option value="IN">India</option>
              <option value="AU">Australia</option>
              <option value="GB">United Kingdom</option>
              <option value="DE">Germany</option>
              <option value="FR">France</option>
              <option value="IT">Italy</option>
              <option value="ES">Spain</option>
              <option value="CA">Canada</option>
              <option value="BR">Brazil</option>
              <option value="MX">Mexico</option>
              <option value="RU">Russia</option>
              <option value="ZA">South Africa</option>
            </select>
          </div>

          <div className="form-row">
            <label>Destination:</label>
            <select value={toCountry} onChange={(e) => setToCountry(e.target.value)}>
              <option value="" disabled>Select Destination Country</option>
              <option value="CN">China</option>
              <option value="JP">Japan</option>
              <option value="SG">Singapore</option>
              <option value="US">United States</option>
              <option value="MY">Malaysia</option>
              <option value="TH">Thailand</option>
              <option value="VN">Vietnam</option>
              <option value="ID">Indonesia</option>
              <option value="PH">Philippines</option>
              <option value="KR">South Korea</option>
              <option value="IN">India</option>
              <option value="AU">Australia</option>
              <option value="GB">United Kingdom</option>
              <option value="DE">Germany</option>
              <option value="FR">France</option>
              <option value="IT">Italy</option>
              <option value="ES">Spain</option>
              <option value="CA">Canada</option>
              <option value="BR">Brazil</option>
              <option value="MX">Mexico</option>
              <option value="RU">Russia</option>
              <option value="ZA">South Africa</option>
            </select>
          </div>

          <div className="form-row">
            <label>Product:</label>
            <select value={product} onChange={(e) => setProduct(e.target.value)} >
              <option value="" disabled>Select Product</option>
              <option value="electronics">Electronics</option>
              <option value="clothing">Clothing</option>
              <option value="furniture">Furniture</option>
              <option value="food">Food</option>
              <option value="books">Books</option>
              <option value="toys">Toys</option>
              <option value="tools">Tools</option>
              <option value="beauty">Beauty Products</option>
              <option value="sports">Sports Equipment</option>
              <option value="automotive">Automotive Parts</option>
            </select>
          </div>

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

          <div className="form-row">
            <label>Fees:</label>
            <div className="fees">
              <label><input 
                type="checkbox"
                checked={fees.handling}
                onChange={() => toggleFee("handling")}
                /> Handling Fee
              </label>
              <label>
                  <input
                    type="checkbox"
                    checked={fees.inspection}
                    onChange={() => toggleFee("inspection")}
                  /> Inspection Fee
            </label>
            <label>
                  <input
                    type="checkbox"
                    checked={fees.processing}
                    onChange={() => toggleFee("processing")}
                  /> Processing Fee
           </label>
            <label>
              <input
                type="checkbox"
                checked={fees.others}
                onChange={() => toggleFee("others")}
              /> Others
            </label>
            </div>
          </div>


          <button type="submit">Calculate</button>
        </form>

        {result && (
          <div className="results-wrapper">
            <h2>Results</h2>
            
            {/* Main Result Card */}
            <div className="total-cost-card">
              <div className="total-label">Total Import Cost</div>
              <div className="total-amount">${Number(result.breakdown.totalPrice|| 0).toFixed(2)}</div>
              <div className="route">{getCountryName(fromCountry)} → {getCountryName(toCountry)}</div>
            </div>

            {/* Cost Breakdown */}
            <div className="breakdown-section">
              <h3>Cost Breakdown</h3>
              <div className="breakdown-table">
                <div className="breakdown-row">
                  <span>Product Value ({quantity} x {product})</span>
                  <span>${Number(result.breakdown.itemPrice || 0).toFixed(2)}</span>
                </div>
                <div className="breakdown-row">
                  <span>Tariff ({result.breakdown.tariffRate}%)</span>
                  <span>${Number(result.breakdown.tariffAmount || 0).toFixed(2)}</span>
                </div>
                {fees.handling && (
                  <div className="breakdown-row">
                    <span>Handling Fee</span>
                    <span>${Number(result.breakdown.handlingFee || 0).toFixed(2)}</span>
                  </div>
                )}
                {fees.processing && (
                  <div className="breakdown-row">
                    <span>Processing Fee</span>
                    <span>${Number(result.breakdown.processingFee || 0).toFixed(2)}</span>
                  </div>
                )}
                {fees.inspection && (
                  <div className="breakdown-row">
                    <span>Inspection Fee</span>
                    <span>${Number(result.breakdown.inspectionFee || 0).toFixed(2)}</span>
                  </div>
                )}
                {fees.others && (
                  <div className="breakdown-row">
                    <span>Miscellaneous Charges</span>
                    <span>${Number(result.breakdown.othersFee || 0).toFixed(2)}</span>
                  </div>
                )}
                <div className="breakdown-row total-row">
                  <span>Total Cost</span>
                  <span>${Number(result.breakdown.totalPrice || 0).toFixed(2)}</span>
                </div>
              </div>
            </div>

            {/* Action Buttons */}
            <div className="result-actions">
              <button className="secondary-btn">Save Result</button>
              <button className="secondary-btn">Export PDF</button>
              <button className="secondary-btn" onClick={() => {setResult(null); setFromCountry(''); setToCountry(''); setProduct(''); setQuantity(''); setFees({handling: false, inspection: false, processing: false, others: false});}}>New Calculation</button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
