export default function CountryDropdown({ label, value, onChange }) {
  const countries = [
    ["SG", "Singapore"], ["US", "United States"], ["MY", "Malaysia"],
    ["TH", "Thailand"], ["VN", "Vietnam"], ["ID", "Indonesia"],
    ["PH", "Philippines"], ["KR", "South Korea"], ["IN", "India"],
    ["AU", "Australia"], ["GB", "United Kingdom"], ["DE", "Germany"],
    ["FR", "France"], ["IT", "Italy"], ["ES", "Spain"], ["CA", "Canada"],
    ["BR", "Brazil"], ["MX", "Mexico"], ["RU", "Russia"], ["ZA", "South Africa"],
    ["CN", "China"], ["JP", "Japan"]
  ];

  return (
    <div className="form-row">
      <label>{label}</label>
      <select value={value} onChange={(e) => onChange(e.target.value)}>
        <option value="" disabled>Select Country</option>
        {countries.map(([code, name]) => (
          <option key={code} value={code}>{name}</option>
        ))}
      </select>
    </div>
  );
}
