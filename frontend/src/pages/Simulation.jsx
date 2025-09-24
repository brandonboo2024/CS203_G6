import { useState } from "react";

export default function Simulation() {
  const [baseTariff, setBaseTariff] = useState({
    origin: "US",
    destination: "SG",
    product: "Electronics",
    rate: 10,
    quantity: 100,
    unitPrice: 50,
  });

  const [newRate, setNewRate] = useState(baseTariff.rate);
  const [result, setResult] = useState(null);

  const handleSimulate = (e) => {
    e.preventDefault();
    const originalTotal = baseTariff.quantity * baseTariff.unitPrice * (1 + baseTariff.rate / 100);
    const simulatedTotal = baseTariff.quantity * baseTariff.unitPrice * (1 + newRate / 100);

    setResult({
      originalTotal,
      simulatedTotal,
      difference: simulatedTotal - originalTotal,
      percentChange: ((simulatedTotal - originalTotal) / originalTotal) * 100,
    });
  };

  return (
    <div className="page">
      <h2>Tariff Simulation</h2>

      <form onSubmit={handleSimulate} className="calc-form">
        <div className="form-row">
          <label>Origin:</label>
          <input type="text" value={baseTariff.origin} disabled />
        </div>
        <div className="form-row">
          <label>Destination:</label>
          <input type="text" value={baseTariff.destination} disabled />
        </div>
        <div className="form-row">
          <label>Product:</label>
          <input type="text" value={baseTariff.product} disabled />
        </div>
        <div className="form-row">
          <label>Base Rate %:</label>
          <input type="number" value={baseTariff.rate} disabled />
        </div>
        <div className="form-row">
          <label>New Rate %:</label>
          <input type="number" value={newRate} onChange={(e) => setNewRate(e.target.value)} />
        </div>
        <button type="submit">Run Simulation</button>
      </form>

      {result && (
        <div className="result-box">
          <h2>Simulation Result</h2>
          <p>Original Total: ${result.originalTotal.toFixed(2)}</p>
          <p>Simulated Total: ${result.simulatedTotal.toFixed(2)}</p>
          <p>Change: {result.difference.toFixed(2)} ({result.percentChange.toFixed(2)}%)</p>
        </div>
      )}
    </div>
  );
}
