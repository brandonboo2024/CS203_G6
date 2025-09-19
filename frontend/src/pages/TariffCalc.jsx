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

  const handleSubmit = (e) => {
    e.preventDefault();
    setResult({ total: Math.floor(Math.random() * 500) + 100 }); // fake result for demo
  };

  return (
    <div className="calc-wrapper">
      <div className="calc-card">
        <h1>Tariff Calculator</h1>

        <form onSubmit={handleSubmit} className="calc-form">
          <div className="form-row">
            <label>Origin:</label>
            <select value={fromCountry} onChange={(e) => setFromCountry(e.target.value)}>
              <option value="">Select Origin Country</option>
              <option value="SG">Singapore</option>
              <option value="US">United States</option>
            </select>
          </div>

          <div className="form-row">
            <label>Destination:</label>
            <select value={toCountry} onChange={(e) => setToCountry(e.target.value)}>
              <option value="">Select Destination Country</option>
              <option value="CN">China</option>
              <option value="JP">Japan</option>
            </select>
          </div>

          <div className="form-row">
            <label>Product:</label>
            <select value={product} onChange={(e) => setProduct(e.target.value)}>
              <option value="">Select Product</option>
              <option value="electronics">Electronics</option>
              <option value="clothing">Clothing</option>
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
              <label>
                <input type="checkbox" checked={fees.handling} onChange={() => toggleFee("handling")} />
                Handling Fee
              </label>
              <label>
                <input type="checkbox" checked={fees.inspection} onChange={() => toggleFee("inspection")} />
                Inspection Fee
              </label>
              <label>
                <input type="checkbox" checked={fees.processing} onChange={() => toggleFee("processing")} />
                Processing Fee
              </label>
              <label>
                <input type="checkbox" checked={fees.others} onChange={() => toggleFee("others")} />
                Others
              </label>
            </div>
          </div>

          <button type="submit">Calculate</button>
        </form>

        {result && (
          <div className="result-box">
            <h2>Result</h2>
            <p>Total Tariff: {result.total}</p>
          </div>
        )}
      </div>
    </div>
  );
}
