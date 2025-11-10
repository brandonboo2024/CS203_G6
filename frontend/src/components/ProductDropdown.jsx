import SearchableSelect from "./SearchableSelect.jsx";

export default function ProductDropdown({
  placeholder = "Select Product",
  ...rest
}) {
  return (
    <SearchableSelect
      placeholder={placeholder}
      {...rest}
    />
  );
}
