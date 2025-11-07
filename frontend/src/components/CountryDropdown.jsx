export default function CountryDropdown({
  label,
  value,
  onChange,
  options = [],
  disabled = false,
  loading = false,
  placeholder = "Select Country",
}) {
  const normalized = options.map((option) =>
    typeof option === "string"
      ? { code: option, label: option }
      : option
  );
  const isDisabled = disabled || normalized.length === 0;

  return (
    <div className="form-row">
      <label>{label}</label>
      <select
        value={value}
        onChange={(e) => onChange(e.target.value)}
        disabled={isDisabled}
      >
        <option value="" disabled>
          {loading ? "Loading..." : placeholder}
        </option>
        {normalized.map(({ code, label }) => (
          <option key={code} value={code}>
            {label || code}
          </option>
        ))}
      </select>
    </div>
  );
}
