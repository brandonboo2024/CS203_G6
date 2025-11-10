import SearchableSelect from "./SearchableSelect.jsx";

export default function CountryDropdown({
  placeholder = "Select Country",
  ...rest
}) {
  return (
    <SearchableSelect
      placeholder={placeholder}
      {...rest}
    />
  );
}
