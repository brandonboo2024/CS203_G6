export default function ProductDropdown({ value, onChange }) {
  const products = [
    "electronics", "clothing", "furniture", "food", "books", "toys",
    "tools", "beauty", "sports", "automotive"
  ];

  return (
    <div className="form-row">
      <label>Product:</label>
      <select value={value} onChange={(e) => onChange(e.target.value)}>
        <option value="" disabled>Select Product</option>
        {products.map((p) => (
          <option key={p} value={p}>{p.charAt(0).toUpperCase() + p.slice(1)}</option>
        ))}
      </select>
    </div>
  );
}
