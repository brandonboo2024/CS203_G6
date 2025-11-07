export default function ProductDropdown({ value, onChange }) {
  const products = [
    "electronics", "clothing", "furniture", "food", 
    "tools", "beauty", "sports", "automotive","misc","plastic or rubber", "chem"
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
