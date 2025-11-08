export default function ProductDropdown({
  value,
  onChange,
  options = [],
  disabled = false,
  loading = false,
  placeholder = "Select Product",
}) {
  const normalized = options.map((option) =>
    typeof option === "string"
      ? { code: option, label: option }
      : option
  );
  const isDisabled = disabled || normalized.length === 0;

  return (
    <div className="form-row">
      <label>Product:</label>
      <select
        value={value}
        onChange={(e) => onChange(e.target.value)}
        disabled={isDisabled}
      >
        <option value="" disabled>
          {loading ? "Loading..." : placeholder}
        </option>
        {normalized.map(({ code, label, priceAvailable }) => (
          <option key={code} value={code}>
            {label || code}
            {priceAvailable === false ? " – price required" : ""}
          </option>
        ))}
      </select>
    </div>
  );
}
