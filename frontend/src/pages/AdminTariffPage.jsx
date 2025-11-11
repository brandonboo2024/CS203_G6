import { useState, useEffect } from "react";
import CountryDropdown from "../components/CountryDropdown";
import ProductDropdown from "../components/ProductDropdown";

export default function AdminTariffPage() {
  const [tariffs, setTariffs] = useState([]);
  const [newTariff, setNewTariff] = useState({
    product: "",
    originCountry: "",
    destinationCountry: "",
    rate: "",
    label: "",
    validFrom: "",
    validTo: "",
    notes: "",
    allowOverride: false,
  });
  const [popup, setPopup] = useState({ show: false, message: "", type: "" });
  const [lookups, setLookups] = useState({
    reporters: [],
    partners: [],
    products: [],
  });
  const [lookupError, setLookupError] = useState(null);
  const [reportersLoading, setReportersLoading] = useState(true);
  const [partnersLoading, setPartnersLoading] = useState(false);
  const [productsLoading, setProductsLoading] = useState(false);

  const apiBase = (import.meta.env.VITE_API_BASE_URL || "").replace(/\/$/, "");
  const tariffApiBase = `${apiBase}/api/tariff`;
  const token = localStorage.getItem("token");

  useEffect(() => {
    fetchTariffs();
  }, []);

  useEffect(() => {
    const loadReporters = async () => {
      setReportersLoading(true);
      try {
        const data = await fetchLookupJson("/api/lookups");
        setLookups((prev) => ({ ...prev, reporters: data?.reporters ?? [] }));
        setLookupError(null);
      } catch (err) {
        console.error(err);
        setLookupError(
          err.message ||
            "Unable to load origin options. Please try again later."
        );
        setLookups((prev) => ({ ...prev, reporters: [] }));
      } finally {
        setReportersLoading(false);
      }
    };
    loadReporters();
  }, [apiBase]);

  useEffect(() => {
    if (!newTariff.originCountry) {
      setLookups((prev) => ({ ...prev, partners: [], products: [] }));
      setPartnersLoading(false);
      setProductsLoading(false);
      return;
    }

    const loadPartners = async () => {
      setPartnersLoading(true);
      try {
        const data = await fetchLookupJson(
          `/api/lookups/reporters/${newTariff.originCountry}/partners`
        );
        setLookups((prev) => ({ ...prev, partners: data, products: [] }));
        setLookupError(null);
      } catch (err) {
        console.error(err);
        setLookupError(
          err.message ||
            "Unable to load destination options for the selected origin."
        );
        setLookups((prev) => ({ ...prev, partners: [], products: [] }));
      } finally {
        setPartnersLoading(false);
      }
    };

    loadPartners();
  }, [newTariff.originCountry, apiBase]);

  useEffect(() => {
    if (!newTariff.originCountry || !newTariff.destinationCountry) {
      setLookups((prev) => ({ ...prev, products: [] }));
      setProductsLoading(false);
      return;
    }

    const loadProducts = async () => {
      setProductsLoading(true);
      try {
        const data = await fetchLookupJson(
          `/api/lookups/reporters/${newTariff.originCountry}/partners/${newTariff.destinationCountry}/products`
        );
        setLookups((prev) => ({ ...prev, products: data }));
        setLookupError(null);
      } catch (err) {
        console.error(err);
        setLookupError(
          err.message ||
            "No tariff data exists for that country/product combination."
        );
        setLookups((prev) => ({ ...prev, products: [] }));
      } finally {
        setProductsLoading(false);
      }
    };

    loadProducts();
  }, [newTariff.originCountry, newTariff.destinationCountry, apiBase]);

  const fetchLookupJson = async (path) => {
    const response = await fetch(`${apiBase}${path}`);
    let payload = null;
    try {
      payload = await response.json();
    } catch {
      payload = null;
    }
    if (!response.ok) {
      throw new Error(
        (payload && (payload.message || payload.error)) ||
          "Failed to fetch lookup data"
      );
    }
    return payload;
  };

  const fetchTariffs = async () => {
    try {
      const res = await fetch(`${tariffApiBase}/all`, {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });
      if (!res.ok) throw new Error("Failed to fetch tariffs");
      const data = await res.json();
      setTariffs(data);
    } catch (err) {
      console.error(err);
    }
  };

  const showPopup = (message, type = "success") => {
    setPopup({ show: true, message, type });
    setTimeout(() => setPopup({ show: false, message: "", type: "" }), 2000);
  };

  const handleAdd = async (e) => {
    e.preventDefault();
    if (
      !newTariff.product.trim() ||
      !newTariff.originCountry.trim() ||
      !newTariff.destinationCountry.trim() ||
      !newTariff.rate ||
      !newTariff.label.trim() ||
      !newTariff.validFrom ||
      !newTariff.validTo
    ) {
      alert("Please fill in all fields!");
      return;
    }

    if (new Date(newTariff.validFrom) > new Date(newTariff.validTo)) {
      alert("Valid from date must be before valid to date.");
      return;
    }

    try {
      const res = await fetch(`${tariffApiBase}/add`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify({
        product: newTariff.product.trim(),
        originCountry: newTariff.originCountry.trim(),
        destinationCountry: newTariff.destinationCountry.trim(),
        rate: parseFloat(newTariff.rate),
        label: newTariff.label.trim(),
        validFrom: newTariff.validFrom,
        validTo: newTariff.validTo,
        notes: newTariff.notes ? newTariff.notes.trim() : "",
        allowOverride: Boolean(newTariff.allowOverride),
      }),
    });

      if (!res.ok) {
        const payload = await res.json().catch(() => null);
        throw new Error(payload?.message || "Failed to add tariff");
      }
      await fetchTariffs();
      setNewTariff({
        product: "",
        originCountry: "",
        destinationCountry: "",
        rate: "",
        label: "",
        validFrom: "",
        validTo: "",
        notes: "",
        allowOverride: false,
      });

      // show success popup
      showPopup("Tariff added successfully!", "success");
    } catch (err) {
      console.error(err);
      showPopup(err.message || "Failed to add tariff", "error");
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this tariff?")) return;

    try {
      const res = await fetch(`${tariffApiBase}/${id}`, {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${token}`, // ✅ ADD THIS
      },
    });

      if (!res.ok) throw new Error("Failed to delete tariff");
      await fetchTariffs();

      // show delete popup
      showPopup("Tariff deleted successfully!", "delete");
    } catch (err) {
      console.error(err);
      showPopup("Failed to delete tariff", "error");
    }
  };

  return (
    <div className="page">
      {/* Reusable popup */}
      {popup.show && (
        <div className={`popup ${popup.type}`}>
          {popup.message}
        </div>
      )}

      <h1 style={{ color: "var(--accent)" }}>Admin Tariff Management</h1>

      {lookupError && (
        <div className="error-banner" style={{ maxWidth: "600px", marginBottom: "1rem" }}>
          {lookupError}
        </div>
      )}

      {/* Add Form */}
      <form
        className="card"
        onSubmit={handleAdd}
        style={{ width: "100%", maxWidth: "700px" }}
      >
        <h2>Add New Tariff</h2>

        <CountryDropdown
          label="Origin Country"
          value={newTariff.originCountry}
          onChange={(value) => {
            setLookups((prev) => ({ ...prev, partners: [], products: [] }));
            setNewTariff((prev) => ({
              ...prev,
              originCountry: value,
              destinationCountry: "",
              product: "",
            }));
          }}
          options={lookups.reporters}
          loading={reportersLoading}
          disabled={reportersLoading}
          placeholder="Select origin country"
        />

        <CountryDropdown
          label="Destination Country"
          value={newTariff.destinationCountry}
          onChange={(value) => {
            setLookups((prev) => ({ ...prev, products: [] }));
            setNewTariff((prev) => ({
              ...prev,
              destinationCountry: value,
              product: "",
            }));
          }}
          options={lookups.partners}
          loading={partnersLoading}
          disabled={!newTariff.originCountry || partnersLoading}
          placeholder={
            newTariff.originCountry ? "Select destination country" : "Select origin first"
          }
        />

        <ProductDropdown
          value={newTariff.product}
          onChange={(value) => setNewTariff((prev) => ({ ...prev, product: value }))}
          options={lookups.products}
          loading={productsLoading}
          disabled={
            !newTariff.originCountry ||
            !newTariff.destinationCountry ||
            productsLoading
          }
          placeholder={
            newTariff.destinationCountry
              ? "Select product"
              : "Select countries first"
          }
        />

        <div className="form-row">
          <label>Rate (decimal):</label>
          <input
            type="number"
            step="0.01"
            min="0"
            max="1"
            placeholder="0.05 for 5%"
            value={newTariff.rate}
            onChange={(e) =>
              setNewTariff({ ...newTariff, rate: e.target.value })
            }
          />
        </div>

        <div className="form-row">
          <label>Label:</label>
          <input
            type="text"
            value={newTariff.label}
            onChange={(e) =>
              setNewTariff({ ...newTariff, label: e.target.value })
            }
            placeholder="e.g. ASEAN FTA Phase 2"
          />
        </div>

        <div className="form-row">
          <label>Valid From:</label>
          <input
            type="date"
            value={newTariff.validFrom}
            onChange={(e) =>
              setNewTariff({ ...newTariff, validFrom: e.target.value })
            }
          />
        </div>

        <div className="form-row">
          <label>Valid To:</label>
          <input
            type="date"
            value={newTariff.validTo}
            onChange={(e) =>
              setNewTariff({ ...newTariff, validTo: e.target.value })
            }
          />
        </div>

        <div className="form-row">
          <label>Notes:</label>
          <textarea
            rows="3"
            value={newTariff.notes}
            onChange={(e) =>
              setNewTariff({ ...newTariff, notes: e.target.value })
            }
            placeholder="Optional context for this tariff"
            style={{
              width: "90%",
              borderRadius: "var(--radius)",
              border: "2px solid var(--accent)",
              background: "var(--bg-dark)",
              color: "var(--text-light)",
              padding: "0.5rem 0.75rem",
            }}
          />
        </div>

        <div className="form-row" style={{ alignItems: "center" }}>
          <label>Override overlaps:</label>
          <div style={{ display: "flex", justifyContent: "flex-start", flexGrow: 1 }}>
            <input
              type="checkbox"
              checked={newTariff.allowOverride}
              onChange={(e) =>
                setNewTariff({ ...newTariff, allowOverride: e.target.checked })
              }
            />
          </div>
        </div>

        <button type="submit">Add Tariff</button>
      </form>

      {/* Tariff Table */}
      <div
        className="card"
        style={{ marginTop: "2rem", width: "100%", maxWidth: "1100px" }}
      >
        <h2>Current Tariffs</h2>

        {tariffs.length === 0 ? (
          <p style={{ color: "var(--text-muted)" }}>No tariffs added yet.</p>
        ) : (
          <div className="table-scroll">
            <table className="calc-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Product</th>
                  <th>Origin</th>
                  <th>Destination</th>
                  <th>Label</th>
                  <th>Valid From</th>
                  <th>Valid To</th>
                  <th>Rate (%)</th>
                  <th>Notes</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {tariffs.map((t) => (
                  <tr key={t.id}>
                    <td>{t.id}</td>
                    <td>{t.product.charAt(0).toUpperCase() + t.product.slice(1)}</td>
                    <td>{t.originCountry}</td>
                    <td>{t.destinationCountry}</td>
                    <td>{t.label}</td>
                    <td>{t.validFrom}</td>
                    <td>{t.validTo}</td>
                    <td>{Number(t.rate * 100).toFixed(2)}</td>
                    <td style={{ maxWidth: "200px", whiteSpace: "pre-wrap" }}>
                      {t.notes || "-"}
                    </td>
                    <td>
                      <button
                        onClick={() => handleDelete(t.id)}
                        className="secondary-btn"
                        style={{ padding: "0.3rem 0.8rem", margin: 0 }}
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
