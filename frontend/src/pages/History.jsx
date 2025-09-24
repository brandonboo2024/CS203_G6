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

      {/* Filters */}
    <div className="card filter-bar">
    <span className="filter-label">Filter:</span>
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
