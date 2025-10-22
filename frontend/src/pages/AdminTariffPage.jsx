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
  });
  const [popup, setPopup] = useState({ show: false, message: "", type: "" });

  const API_BASE = "http://localhost:8080/api/tariff";

  useEffect(() => {
    fetchTariffs();
  }, []);

  const fetchTariffs = async () => {
    try {
      const res = await fetch(`${API_BASE}/all`);
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
      !newTariff.rate
    ) {
      alert("Please fill in all fields!");
      return;
    }

    try {
      const res = await fetch(`${API_BASE}/add`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          ...newTariff,
          rate: parseFloat(newTariff.rate),
        }),
      });

      if (!res.ok) throw new Error("Failed to add tariff");
      await fetchTariffs();
      setNewTariff({
        product: "",
        originCountry: "",
        destinationCountry: "",
        rate: "",
      });

      // show success popup
      showPopup("Tariff added successfully!", "success");
    } catch (err) {
      console.error(err);
      showPopup("Failed to add tariff", "error");
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this tariff?")) return;

    try {
      const res = await fetch(`${API_BASE}/${id}`, { method: "DELETE" });
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

      {/* Add Form */}
      <form
        className="card"
        onSubmit={handleAdd}
        style={{ width: "100%", maxWidth: "600px" }}
      >
        <h2>Add New Tariff</h2>

        <ProductDropdown
          value={newTariff.product}
          onChange={(value) => setNewTariff({ ...newTariff, product: value })}
        />

        <CountryDropdown
          label="Origin Country"
          value={newTariff.originCountry}
          onChange={(value) =>
            setNewTariff({ ...newTariff, originCountry: value })
          }
        />

        <CountryDropdown
          label="Destination Country"
          value={newTariff.destinationCountry}
          onChange={(value) =>
            setNewTariff({ ...newTariff, destinationCountry: value })
          }
        />

        <div className="form-row">
          <label>Rate (%):</label>
          <input
            type="number"
            step="0.01"
            min="0"
            value={newTariff.rate}
            onChange={(e) =>
              setNewTariff({ ...newTariff, rate: e.target.value })
            }
          />
        </div>

        <button type="submit">Add Tariff</button>
      </form>

      {/* Tariff Table */}
      <div
        className="card"
        style={{ marginTop: "2rem", width: "100%", maxWidth: "800px" }}
      >
        <h2>Current Tariffs</h2>

        {tariffs.length === 0 ? (
          <p style={{ color: "var(--text-muted)" }}>No tariffs added yet.</p>
        ) : (
          <table className="calc-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Product</th>
                <th>Origin</th>
                <th>Destination</th>
                <th>Rate (%)</th>
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
                  <td>{t.rate}</td>
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
        )}
      </div>
    </div>
  );
}
