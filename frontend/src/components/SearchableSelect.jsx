import { useEffect, useId, useMemo, useState } from "react";

export default function SearchableSelect({
  label,
  value,
  onChange,
  options = [],
  placeholder = "Select an option",
  disabled = false,
  loading = false,
  mode = "select", // 'select' | 'search'
  searchEnabled = true,
}) {
  const normalized = useMemo(
    () =>
      options.map((option) => {
        const entry =
          typeof option === "string"
            ? { code: option, label: option }
            : option;
        const codeText = `${entry.code ?? ""}`.trim();
        const labelText = `${entry.label ?? ""}`.trim();
        const displayLabel =
          labelText && codeText && labelText !== codeText
            ? `${labelText} (${codeText})`
            : labelText || codeText;

        return {
          ...entry,
          code: codeText,
          label: labelText,
          displayLabel,
        };
      }),
    [options]
  );
  const isDisabled = disabled || normalized.length === 0;
  const isSearchMode = searchEnabled && mode === "search";

  const [searchValue, setSearchValue] = useState("");
  const [error, setError] = useState("");
  const datalistId = useId();
  const selectId = useId();
  const searchInputId = useId();

  const findByCode = (code) =>
    normalized.find((option) => option.code === code);

  const findMatch = (input, { allowPartial = false } = {}) => {
    if (!input) return null;
    const normalizedInput = input.trim().toLowerCase();
    if (!normalizedInput) return null;

    const matcher = (option) => {
      const code = option.code?.toLowerCase() || "";
      const label = option.label?.toLowerCase() || "";
      const display = option.displayLabel?.toLowerCase() || "";
      const exactMatch =
        normalizedInput === code ||
        (!!label && normalizedInput === label) ||
        (!!display && normalizedInput === display);
      if (exactMatch) return true;
      if (!allowPartial) return false;
      return (
        (code && code.includes(normalizedInput)) ||
        (label && label.includes(normalizedInput)) ||
        (display && display.includes(normalizedInput))
      );
    };

    return normalized.find(matcher) || null;
  };

  useEffect(() => {
    if (!isSearchMode) {
      setSearchValue("");
      setError("");
      return;
    }
    if (!value) {
      setSearchValue("");
      return;
    }
    const match = findByCode(value);
    setSearchValue(
      match?.displayLabel || match?.label || match?.code || value
    );
  }, [value, isSearchMode]);

  const handleSearchBlur = () => {
    if (!searchValue) {
      setError("");
      if (value) onChange("");
      return;
    }
    const match = findMatch(searchValue, { allowPartial: true });
    if (match) {
      setError("");
      if (match.code !== value) {
        onChange(match.code);
      }
      setSearchValue(
        match.displayLabel || match.label || match.code
      );
    } else {
      setError("Please pick a value from the list.");
      if (value) {
        const prev = findByCode(value);
        setSearchValue(
          prev?.displayLabel || prev?.label || prev?.code || ""
        );
      } else {
        setSearchValue("");
      }
    }
  };

  const handleSearchInput = (text) => {
    setSearchValue(text);
    setError("");
    if (!text) {
      if (value) onChange("");
      return;
    }
    const match = findMatch(text, { allowPartial: false });
    if (match && match.code !== value) {
      onChange(match.code);
    }
  };

  const controlId = isSearchMode ? searchInputId : selectId;

  return (
    <div className="form-row search-select">
      <label htmlFor={controlId}>{label}</label>
      <div className="input-area">
        {isSearchMode ? (
          <div className="search-mode-wrapper">
            <input
              id={searchInputId}
              type="text"
              list={datalistId}
              value={searchValue}
              disabled={isDisabled}
              placeholder={
                loading
                  ? "Loading..."
                  : "Type to search by name or code"
              }
              onChange={(e) => handleSearchInput(e.target.value)}
              onBlur={handleSearchBlur}
              onKeyDown={(e) => {
                if (e.key === "Enter") {
                  e.preventDefault();
                  handleSearchBlur();
                }
              }}
            />
            <datalist id={datalistId}>
              {normalized.map(({ code, displayLabel }) => (
                <option key={code} value={displayLabel}>
                  {displayLabel}
                </option>
              ))}
            </datalist>
          </div>
        ) : (
          <select
            id={selectId}
            value={value}
            onChange={(e) => onChange(e.target.value)}
            disabled={isDisabled}
          >
            <option value="" disabled>
              {loading ? "Loading..." : placeholder}
            </option>
            {normalized.map(({ code, label: optionLabel }) => (
              <option key={code} value={code}>
                {optionLabel || code}
              </option>
            ))}
          </select>
        )}

        {isSearchMode && error && (
          <div className="input-error">{error}</div>
        )}
      </div>
    </div>
  );
}
