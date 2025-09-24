export default function Dashboard() {
  const userName = localStorage.getItem("username") || "Guest";
  const recentCalculations = [
    { date: "13/09/25", route: "CN → SG", product: "Electronics", total: "$2,458.00" },
    { date: "02/09/25", route: "VN → SG", product: "Textiles", total: "$1,245.00" },
    { date: "30/08/25", route: "US → SG", product: "Machinery", total: "$8,567.00" },
  ];
  const latestNews = [
    { headline: "Headline 1", link: "#" },
    { headline: "Headline 2", link: "#" },
  ];

  return (
    <div className="dashboard-wrapper">
      <h1>Welcome Back <span className="highlight">{userName}</span>!</h1>

      {/* Recent Calculations */}
      <div className="card">
        <h2>Recent Calculations:</h2>
        <table className="calc-table">
          <thead>
            <tr>
              <th>Date</th>
              <th>Route</th>
              <th>Product</th>
              <th>Total</th>
            </tr>
          </thead>
          <tbody>
            {recentCalculations.map((row, idx) => (
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

      {/* Latest News */}
      <div className="card">
        <h2>Latest News</h2>
        <div className="news-list">
          {latestNews.map((news, idx) => (
            <div className="news-item" key={idx}>
              <span>{news.headline}</span>
              <a href={news.link}>Read more</a>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
