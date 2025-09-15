import { useState } from "react";

export default function TariffCalc() {
  const [fromCountry, setFromCountry] = useState("");
  const [toCountry, setToCountry] = useState("");
  const [product, setProduct] = useState("");
  const [result, setResult] = useState(null);

  const handleSubmit = (e) => {
    e.preventDefault();
    // TODO: later call backend API
    setResult({ total: 123.45 });
  };

  return (
    <div>
      <h1>Tariff Calculator</h1>
      <form onSubmit={handleSubmit} style={{ display: "grid", gap: "1rem", maxWidth: "300px" }}>
        <input
          placeholder="From Country"
          value={fromCountry}
          onChange={(e) => setFromCountry(e.target.value)}
        />
        <input
          placeholder="To Country"
          value={toCountry}
          onChange={(e) => setToCountry(e.target.value)}
        />
        <input
          placeholder="Product"
          value={product}
          onChange={(e) => setProduct(e.target.value)}
        />
        <button type="submit">Calculate</button>
      </form>

      {result && (
        <div style={{ marginTop: "1rem" }}>
          <h2>Result</h2>
          <p>Total Tariff: {result.total}</p>
        </div>
      )}
    </div>
  );
}
