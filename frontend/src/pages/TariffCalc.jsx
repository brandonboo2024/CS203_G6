import { useEffect, useState } from "react";
import "../App.css";
import { validateForm, sanitizeInput } from "../utils/inputValidation";

// ✅ import your new shared components
import CountryDropdown from "../components/CountryDropdown.jsx";
import ProductDropdown from "../components/ProductDropdown.jsx";

const normalizePriceDetails = (details, fallbackHsCode, fallbackMessage = null) => ({
  tariffRate: details?.tariffRate ?? details?.tariff_rate ?? null,
  missingHsCode:
    details?.missingHsCode ?? details?.missing_hs_code ?? fallbackHsCode,
  message: details?.message ?? fallbackMessage,
  suggestedBasePrice:
    details?.suggestedBasePrice ??
    details?.suggested_base_price ??
    null,
  label: details?.label ?? null,
  source: details?.source ?? details?.source_label ?? null,
  validFrom: details?.validFrom ?? details?.valid_from ?? null,
  validTo: details?.validTo ?? details?.valid_to ?? null,
  notes: details?.notes ?? null,
  adminTariffId: details?.adminTariffId ?? details?.admin_tariff_id ?? null,
});

export default function TariffCalc() {
  const [fromCountry, setFromCountry] = useState("");
  const [toCountry, setToCountry] = useState("");
  const [product, setProduct] = useState("");
  const [quantity, setQuantity] = useState("");
  const [calculationFrom, setFrom] = useState("");
  const [calculationTo, setTo] = useState("");
  const [fees, setFees] = useState({
    handling: false,
    inspection: false,
    processing: false,
    others: false,
  });
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const [validationErrors, setValidationErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [lookupError, setLookupError] = useState(null);
  const [pendingPrice, setPendingPrice] = useState(null);
  const [customPriceValue, setCustomPriceValue] = useState("");
  const [priceError, setPriceError] = useState(null);
  const [lookups, setLookups] = useState({
    reporters: [],
    partners: [],
    products: [],
  });
  const [reportersLoading, setReportersLoading] = useState(true);
  const [partnersLoading, setPartnersLoading] = useState(false);
  const [productsLoading, setProductsLoading] = useState(false);
  const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || "").replace(/\/$/, "");
  const submitDisabled =
    loading ||
    reportersLoading ||
    partnersLoading ||
    productsLoading ||
    !!lookupError ||
    !fromCountry ||
    !toCountry ||
    !product ||
    !quantity ||
    !calculationFrom ||
    !calculationTo;

  const fetchLookupJson = async (path) => {
    const response = await fetch(`${apiBaseUrl}${path}`);
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

  useEffect(() => {
    const loadReporters = async () => {
      setReportersLoading(true);
      try {
        const data = await fetchLookupJson("/api/lookups");
        setLookups((prev) => ({
          ...prev,
          reporters: data?.reporters ?? [],
        }));
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
  }, [apiBaseUrl]);

  useEffect(() => {
    setToCountry("");
    setProduct("");
    if (!fromCountry) {
      setLookups((prev) => ({ ...prev, partners: [], products: [] }));
      return;
    }

    const loadPartners = async () => {
      setPartnersLoading(true);
      try {
        const data = await fetchLookupJson(
          `/api/lookups/reporters/${fromCountry}/partners`
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
  }, [fromCountry, apiBaseUrl]);

  useEffect(() => {
    setProduct("");
    if (!fromCountry || !toCountry) {
      setLookups((prev) => ({ ...prev, products: [] }));
      return;
    }

    const loadProducts = async () => {
      setProductsLoading(true);
      try {
        const data = await fetchLookupJson(
          `/api/lookups/reporters/${fromCountry}/partners/${toCountry}/products`
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
  }, [fromCountry, toCountry, apiBaseUrl]);

  useEffect(() => {
    setPendingPrice(null);
    setCustomPriceValue("");
    setPriceError(null);
  }, [product]);

  useEffect(() => {
    if (!pendingPrice) return;
    const suggested = pendingPrice.details?.suggestedBasePrice;
    if (suggested === null || suggested === undefined) {
      return;
    }
    const numeric = Number(suggested);
    if (Number.isNaN(numeric)) {
      return;
    }
    setCustomPriceValue(numeric.toFixed(2));
  }, [pendingPrice]);

  const toggleFee = (key) => {
    setFees((prev) => ({ ...prev, [key]: !prev[key] }));
  };

  const toIso = (local) => (local ? new Date(local).toISOString() : null);

  const selectedProduct = lookups.products.find((entry) => entry.code === product);

  const performCalculation = async (payload) => {
    const token = localStorage.getItem("token");
    const headers = { "Content-Type": "application/json" };
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }

    const response = await fetch(
      `${apiBaseUrl}/api/tariff/calculate`,
      {
        method: "POST",
        headers,
        body: JSON.stringify(payload),
      }
    );

    let data = null;
    try {
      data = await response.json();
    } catch {
      data = null;
    }

    if (!response.ok) {
      const status = response.status;
      const message =
        data?.message || data?.error || (status === 401 || status === 403
          ? "You must be signed in to calculate tariffs."
          : "Request failed");
      const messageLower = message.toLowerCase();
      if (messageLower.includes("price") && messageLower.includes("require")) {
        const err = new Error(message);
        err.priceRequired = true;
        err.priceDetails = normalizePriceDetails(
          data,
          payload.hsCode || payload.product,
          message
        );
        throw err;
      }
      throw new Error(message);
    }

    return data;
  };

  const applySuccessfulResult = (data, requestPayload) => {
    // normalize snake_case to camelCase
    const breakdown = {
      itemPrice: data.itemPrice ?? data.item_price ?? 0,
      tariffRate: data.tariffRate ?? data.tariff_rate ?? 0,
      tariffAmount: data.tariffAmount ?? data.tariff_amount ?? 0,
      handlingFee: data.handlingFee ?? data.handling_fee ?? 0,
      processingFee: data.processingFee ?? data.processing_fee ?? 0,
      inspectionFee: data.inspectionFee ?? data.inspection_fee ?? 0,
      otherFees: data.otherFees ?? data.other_fees ?? 0,
      totalPrice: data.totalPrice ?? data.total_price ?? 0,
    };

    const segments = Array.isArray(data.segments) ? data.segments : [];
    const tariffMeta = {
      label: data.label || null,
      source: data.source || null,
      validFrom: data.validFrom || null,
      validTo: data.validTo || null,
      notes: data.notes || null,
      adminTariffId: data.adminTariffId ?? null,
    };

    setResult({
      breakdown,
      segments,
      window: {
        from: requestPayload.calculationFrom,
        to: requestPayload.calculationTo,
      },
      pricePersisted: Boolean(data.pricePersisted),
      tariffMeta,
    });

    // ---- Save local history ----
    const historyEntry = {
      createdAt: new Date().toISOString(),
      route: `${getLabel(fromCountry, lookups.reporters)} → ${getLabel(
        toCountry,
        lookups.partners
      )}`,
      product: selectedProduct?.label || product,
      total: Number(breakdown.totalPrice ?? 0),
      tariffFrom: requestPayload.calculationFrom,
      tariffTo: requestPayload.calculationTo,
      tariffLabel: tariffMeta.label,
    };
    const prev = JSON.parse(localStorage.getItem("calcHistory") || "[]");
    prev.unshift(historyEntry);
    const trimmed = prev.slice(0, 50);
    localStorage.setItem("calcHistory", JSON.stringify(trimmed));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setValidationErrors({});
    setPendingPrice(null);
    setPriceError(null);
    
    // Validate form data
    const formData = {
      fromCountry,
      toCountry,
      product,
      quantity,
      calculationFrom,
      calculationTo
    };
    
    const validation = validateForm(formData, 'tariff');
    if (!validation.isValid) {
      setValidationErrors(validation.errors);
      setLoading(false);
      return;
    }
    
    if(!fromCountry || !toCountry || !product){
        setError("Please select an origin, destination, and product.");
        setLoading(false);
        return;
    }
    if(!calculationFrom || !calculationTo){
        setError("Please select both start and end date/time.");
        setLoading(false);
        return;
    }
    const fromDate = new Date(calculationFrom);
    const toDate = new Date(calculationTo);
    if(fromDate > toDate){
        setError("End date/time must be after start date/time.");
        setLoading(false);
        return;
    }

    const parsedQuantity = parseInt(quantity, 10);
    if (Number.isNaN(parsedQuantity) || parsedQuantity <= 0) {
      setError("Quantity must be a positive number.");
      setLoading(false);
      return;
    }

    // Sanitize inputs before sending
    const request = {
      fromCountry: sanitizeInput(fromCountry),
      toCountry: sanitizeInput(toCountry),
      product: sanitizeInput(product),
      quantity: parsedQuantity,
      handling: fees.handling,
      inspection: fees.inspection,
      processing: fees.processing,
      others: fees.others,
      calculationFrom: toIso(calculationFrom),
      calculationTo: toIso(calculationTo),
    };

    if (selectedProduct?.hsCode) {
      request.hsCode = sanitizeInput(selectedProduct.hsCode);
    }

    try {
      const data = await performCalculation(request);

      if (data.priceRequired) {
        setPendingPrice({
          request,
          details: normalizePriceDetails(
            data,
            request.hsCode ?? request.product,
            data?.message ?? null
          ),
        });
        setCustomPriceValue("");
        setLoading(false);
        return;
      }

      applySuccessfulResult(data, request);
      setLoading(false);
    } catch (err) {
      if (err.priceRequired) {
        setPendingPrice({
          request,
          details: normalizePriceDetails(
            err.priceDetails,
            request.hsCode ?? request.product,
            err.message
          ),
        });
        setCustomPriceValue("");
        setLoading(false);
        return;
      }
      console.error(err);
      setError(err.message || "Something went wrong.");
      setLoading(false);
    }
  };

  const submitCustomPrice = async () => {
    if (!pendingPrice) return;
    const numericPrice = Number(customPriceValue);
    if (Number.isNaN(numericPrice) || numericPrice <= 0) {
      setPriceError("Please enter a valid base price (greater than 0).");
      return;
    }

    const request = {
      ...pendingPrice.request,
      customBasePrice: numericPrice,
    };

    try {
      setLoading(true);
      setError(null);
      setPriceError(null);
      const data = await performCalculation(request);

      if (data.priceRequired) {
        // still missing price for some reason
        setPendingPrice({
          request,
          details: normalizePriceDetails(
            data,
            request.hsCode ?? request.product,
            data?.message ?? null
          ),
        });
        setLoading(false);
        return;
      }

      applySuccessfulResult(data, request);
      setPendingPrice(null);
      setCustomPriceValue("");
      setLoading(false);
    } catch (err) {
      if (err.priceRequired) {
        setPendingPrice({
          request,
          details: normalizePriceDetails(
            err.priceDetails,
            request.hsCode ?? request.product,
            err.message
          ),
        });
        setCustomPriceValue("");
        setLoading(false);
        return;
      }
      console.error(err);
      setError(err.message || "Unable to apply custom price.");
      setLoading(false);
    }
  };

  const cancelPricePrompt = () => {
    setPendingPrice(null);
    setCustomPriceValue("");
    setPriceError(null);
  };

  const getLabel = (code, collection) => {
    if (!code || !Array.isArray(collection)) return code;
    const match = collection.find((entry) => entry.code === code);
    return match?.label || code;
  };

  const fmtDate = (iso) => {
    if (!iso) return "-";
    try {
      return new Date(iso).toLocaleString();
    } catch {
      return iso;
    }
  };

  return (
    <div className="calc-wrapper">
      <div className="calc-card">
        <h1>Tariff Calculator</h1>

        <form onSubmit={handleSubmit} className="calc-form">
          {/* ✅ Replaced long dropdowns with shared components */}
          {lookupError && (
            <div className="error-banner">
              {lookupError}
            </div>
          )}
          <CountryDropdown
            label="Origin"
            value={fromCountry}
            onChange={setFromCountry}
            options={lookups.reporters}
            disabled={reportersLoading || !!lookupError}
            loading={reportersLoading}
            placeholder="Select origin country"
          />
          <CountryDropdown
            label="Destination"
            value={toCountry}
            onChange={setToCountry}
            options={lookups.partners}
            disabled={
              reportersLoading ||
              partnersLoading ||
              !!lookupError ||
              !fromCountry
            }
            loading={partnersLoading}
            placeholder={
              fromCountry ? "Select destination country" : "Select origin first"
            }
          />
          <ProductDropdown
            value={product}
            onChange={setProduct}
            options={lookups.products}
            disabled={
              reportersLoading ||
              partnersLoading ||
              productsLoading ||
              !!lookupError ||
              !fromCountry ||
              !toCountry
            }
            loading={productsLoading}
            placeholder={
              toCountry ? "Select product" : "Choose countries first"
            }
          />

          {/* Quantity */}
          <div className="form-row">
            <label>Quantity:</label>
            <input
              type="number"
              min="1"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
              placeholder="Enter quantity"
            />
          </div>

          {/* Time window */}
          <div className="form-row">
            <label>From:</label>
            <input
              type="datetime-local"
              value={calculationFrom}
              onChange={(e) => setFrom(e.target.value)}
              required
            />
          </div>
          <div className="form-row">
            <label>To:</label>
            <input
              type="datetime-local"
              value={calculationTo}
              onChange={(e) => setTo(e.target.value)}
              required
            />
          </div>

          {/* Fees */}
          <div className="form-row">
            <label>Fees:</label>
            <div className="fees">
              {Object.keys(fees).map((key) => (
                <label key={key}>
                  <input
                    type="checkbox"
                    checked={fees[key]}
                    onChange={() => toggleFee(key)}
                  />{" "}
                  {key.charAt(0).toUpperCase() + key.slice(1)} Fee
                </label>
              ))}
            </div>
          </div>


          <button
            type="submit"
            disabled={submitDisabled}
          >
            {loading ? 'Calculating...' : 'Calculate'}
          </button>
        </form>

        {error && (
          <div className="error-banner" style={{ marginTop: "1rem" }}>
            {error}
          </div>
        )}

        {/* Display validation errors */}
        {Object.keys(validationErrors).length > 0 && (
          <div className="validation-errors" style={{ 
            marginTop: '1rem', 
            padding: '1rem', 
            backgroundColor: '#ffebee', 
            border: '1px solid #f44336', 
            borderRadius: '4px' 
          }}>
            <h4 style={{ color: '#d32f2f', margin: '0 0 0.5rem 0' }}>Please fix the following errors:</h4>
            {Object.entries(validationErrors).map(([field, errors]) => (
              <div key={field} style={{ marginBottom: '0.5rem' }}>
                <strong style={{ textTransform: 'capitalize', color: '#d32f2f' }}>
                  {field.replace(/([A-Z])/g, ' $1').trim()}:
                </strong>
                <ul style={{ margin: '0.25rem 0 0 1rem', padding: 0 }}>
                  {errors.map((error, index) => (
                    <li key={index} style={{ color: '#d32f2f', fontSize: '0.9rem' }}>
                      {error}
                    </li>
                  ))}
                </ul>
              </div>
            ))}
          </div>
        )}

        {pendingPrice && (
          <div className="card" style={{ marginTop: '1.5rem', border: '1px solid #ffa000' }}>
            <h3 style={{ marginTop: 0 }}>Confirm base price</h3>
            <p>
              Please confirm the per-unit base price for HS{" "}
              {pendingPrice.details?.missingHsCode || selectedProduct?.hsCode || product}
              . We’ll apply your value to the tariff rate to compute the totals.
            </p>
            {pendingPrice.details?.label && (
              <p style={{ color: '#ffdd99' }}>
                Tariff: {pendingPrice.details.label}
              </p>
            )}
            {(pendingPrice.details?.validFrom || pendingPrice.details?.validTo) && (
              <p style={{ color: '#cfd8dc', fontSize: '0.9rem' }}>
                Effective {pendingPrice.details?.validFrom || 'now'} → {pendingPrice.details?.validTo || 'open'}
              </p>
            )}
            {pendingPrice.details?.message && (
              <p style={{ color: '#ffb74d' }}>{pendingPrice.details.message}</p>
            )}
            {pendingPrice.details?.suggestedBasePrice !== undefined &&
              pendingPrice.details?.suggestedBasePrice !== null && (
              <p style={{ fontStyle: 'italic', color: '#ffdd99' }}>
                Suggested price from saved data: $
                {Number(pendingPrice.details.suggestedBasePrice).toFixed(2)}
              </p>
            )}
            {pendingPrice.details?.tariffRate && (
              <p style={{ fontStyle: 'italic' }}>
                Tariff rate: {Number(pendingPrice.details.tariffRate).toFixed(2)}%
              </p>
            )}
            {pendingPrice.details?.notes && (
              <p style={{ color: '#cfd8dc', fontSize: '0.9rem' }}>
                Notes: {pendingPrice.details.notes}
              </p>
            )}
            <div className="form-row">
              <label>Base price (per unit):</label>
              <input
                type="number"
                min="0.01"
                step="0.01"
                value={customPriceValue}
                onChange={(e) => setCustomPriceValue(e.target.value)}
                placeholder="e.g. 120.00"
              />
            </div>
            {priceError && (
              <div className="error-banner">{priceError}</div>
            )}
            <div style={{ display: 'flex', gap: '1rem', marginTop: '0.5rem' }}>
              <button type="button" onClick={submitCustomPrice} disabled={loading}>
                {loading ? 'Saving...' : 'Use this price'}
              </button>
              <button type="button" className="secondary-btn" onClick={cancelPricePrompt}>
                Cancel
              </button>
            </div>
          </div>
        )}

        {result && (() => {
          // Calculate total based on only selected fees
          const calculatedTotal = 
            Number(result.breakdown.itemPrice || 0) +
            Number(result.breakdown.tariffAmount || 0) +
            (fees.handling ? Number(result.breakdown.handlingFee || 0) : 0) +
            (fees.processing ? Number(result.breakdown.processingFee || 0) : 0) +
            (fees.inspection ? Number(result.breakdown.inspectionFee || 0) : 0) +
            (fees.others ? Number(result.breakdown.otherFees || 0) : 0);

          return (
          <div className="results-wrapper" style={{ marginTop: '2rem' }}>
            <h2 style={{ color: '#fff', fontSize: '1.5rem', fontWeight: 'bold', marginBottom: '1rem' }}>Results</h2>
            {result.pricePersisted && (
              <div style={{
                backgroundColor: '#1c2a3a',
                border: '1px solid #4caf50',
                color: '#aed581',
                padding: '0.75rem',
                borderRadius: '6px',
                marginBottom: '1rem'
              }}>
                Base price saved for this HS code. Future calculations can reuse it.
              </div>
            )}
            
            {result.tariffMeta && (
              <div
                style={{
                  backgroundColor: '#1c2a3a',
                  border: '1px solid #4caf50',
                  color: '#e0f7fa',
                  padding: '1rem',
                  borderRadius: '8px',
                  marginBottom: '1.5rem'
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', flexWrap: 'wrap', gap: '0.5rem' }}>
                  <strong>{result.tariffMeta.label || 'Tariff details'}</strong>
                  {result.tariffMeta.source?.startsWith('admin:') && (
                    <span style={{
                      backgroundColor: '#ff8c00',
                      color: '#000',
                      borderRadius: '12px',
                      padding: '0.1rem 0.75rem',
                      fontSize: '0.85rem'
                    }}>
                      Admin managed
                    </span>
                  )}
                </div>
                {(result.tariffMeta.validFrom || result.tariffMeta.validTo) && (
                  <div style={{ fontSize: '0.9rem', marginTop: '0.3rem' }}>
                    Effective {result.tariffMeta.validFrom || 'now'} → {result.tariffMeta.validTo || 'open'}
                  </div>
                )}
                {result.tariffMeta.notes && (
                  <div style={{ fontSize: '0.9rem', marginTop: '0.5rem', color: '#cfd8dc' }}>
                    {result.tariffMeta.notes}
                  </div>
                )}
              </div>
            )}

            {/* Total Import Cost Card - Orange Theme */}
            <div style={{
              backgroundColor: '#FF8C00',
              borderRadius: '8px',
              padding: '1.5rem',
              marginBottom: '2rem',
              textAlign: 'left'
            }}>
              <div style={{ color: '#000', fontSize: '1rem', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                Total Import Cost
              </div>
              <div style={{ color: '#000', fontSize: '2rem', fontWeight: 'bold', marginBottom: '0.5rem' }}>
                ${calculatedTotal.toFixed(2)}
              </div>
              <div style={{ color: '#000', fontSize: '0.9rem' }}>
                {getLabel(fromCountry, lookups.reporters)} →{" "}
                {getLabel(toCountry, lookups.partners)}
              </div>
            </div>

            {/* Cost Breakdown Section */}
            <h3 style={{ color: '#fff', fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '1rem' }}>
              Cost Breakdown
            </h3>
            <div style={{
              backgroundColor: '#000',
              border: '1px solid #FF8C00',
              padding: '1.5rem',
              borderRadius: '8px',
              marginBottom: '1.5rem'
            }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                {/* Product Value */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ color: '#fff', fontSize: '1rem' }}>Product Value:</span>
                  <span style={{ color: '#fff', fontSize: '1rem', fontWeight: '500' }}>
                    ${Number(result.breakdown.itemPrice || 0).toFixed(2)}
                  </span>
                </div>

                {/* Tariff */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ color: '#fff', fontSize: '1rem' }}>
                    Tariff ({Number(result.breakdown.tariffRate || 0).toFixed(1)}%
                    {Number(result.breakdown.tariffRate || 0) === 0 ? ' - no tariff applies' : ''}
                    ):
                  </span>
                  <span style={{ color: '#fff', fontSize: '1rem', fontWeight: '500' }}>
                    ${Number(result.breakdown.tariffAmount || 0).toFixed(2)}
                  </span>
                </div>

                {/* Handling Fee - only show if selected */}
                {fees.handling && result.breakdown.handlingFee > 0 && (
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ color: '#fff', fontSize: '1rem' }}>Handling Fee:</span>
                    <span style={{ color: '#fff', fontSize: '1rem', fontWeight: '500' }}>
                      ${Number(result.breakdown.handlingFee || 0).toFixed(2)}
                    </span>
                  </div>
                )}

                {/* Processing Fee - only show if selected */}
                {fees.processing && result.breakdown.processingFee > 0 && (
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ color: '#fff', fontSize: '1rem' }}>Processing Fee:</span>
                    <span style={{ color: '#fff', fontSize: '1rem', fontWeight: '500' }}>
                      ${Number(result.breakdown.processingFee || 0).toFixed(2)}
                    </span>
                  </div>
                )}

                {/* Inspection Fee - only show if selected */}
                {fees.inspection && result.breakdown.inspectionFee > 0 && (
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ color: '#fff', fontSize: '1rem' }}>Inspection Fee:</span>
                    <span style={{ color: '#fff', fontSize: '1rem', fontWeight: '500' }}>
                      ${Number(result.breakdown.inspectionFee || 0).toFixed(2)}
                    </span>
                  </div>
                )}

                {/* Miscellaneous Charges - only show if selected */}
                {fees.others && result.breakdown.otherFees > 0 && (
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ color: '#fff', fontSize: '1rem' }}>Miscellaneous Charges:</span>
                    <span style={{ color: '#fff', fontSize: '1rem', fontWeight: '500' }}>
                      ${Number(result.breakdown.otherFees || 0).toFixed(2)}
                    </span>
                  </div>
                )}

                {/* Separator Line */}
                <hr style={{ 
                  border: 'none', 
                  borderTop: '1px solid #fff', 
                  margin: '1rem 0',
                  width: '100%',
                  opacity: 0.3
                }} />

                {/* Total Cost */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ color: '#fff', fontSize: '1.1rem', fontWeight: 'bold' }}>Total Cost:</span>
                  <span style={{ color: '#fff', fontSize: '1.1rem', fontWeight: 'bold' }}>
                    ${calculatedTotal.toFixed(2)}
                  </span>
                </div>
              </div>
            </div>

            {/* Action Buttons */}
            <div style={{ 
              display: 'flex', 
              gap: '1rem', 
              justifyContent: 'center',
              marginTop: '1.5rem'
            }}>
              <button
                type="button"
                onClick={() => {
                  // Save result functionality
                  const historyEntry = {
                    createdAt: new Date().toISOString(),
                    route: `${fromCountry} → ${toCountry}`,
                    product,
                    total: Number(result.breakdown.totalPrice || 0),
                    tariffFrom: result.window.from,
                    tariffTo: result.window.to,
                    breakdown: result.breakdown
                  };
                  const prev = JSON.parse(localStorage.getItem("calcHistory") || "[]");
                  prev.unshift(historyEntry);
                  const trimmed = prev.slice(0, 50);
                  localStorage.setItem("calcHistory", JSON.stringify(trimmed));
                  alert("Result saved successfully!");
                }}
                style={{
                  padding: '0.75rem 1.5rem',
                  borderRadius: '6px',
                  border: 'none',
                  backgroundColor: '#808080',
                  color: '#000000ff',
                  fontSize: '1rem',
                  cursor: 'pointer',
                  fontWeight: '500'
                }}
              >
                Save Result
              </button>
              
              <button
                type="button"
                onClick={() => {
                  // Export PDF functionality - placeholder
                  alert("PDF export functionality coming soon!");
                }}
                style={{
                  padding: '0.75rem 1.5rem',
                  borderRadius: '6px',
                  border: 'none',
                  backgroundColor: '#808080',
                  color: '#000000ff',
                  fontSize: '1rem',
                  cursor: 'pointer',
                  fontWeight: '500'
                }}
              >
                Export PDF
              </button>
              
              <button
                type="button"
                onClick={() => {
                  // Reset form and result
                  setResult(null);
                  setFromCountry("");
                  setToCountry("");
                  setProduct("");
                  setQuantity("");
                  setFrom("");
                  setTo("");
                  setFees({
                    handling: false,
                    inspection: false,
                    processing: false,
                    others: false,
                  });
                  setError(null);
                }}
                style={{
                  padding: '0.75rem 1.5rem',
                  borderRadius: '6px',
                  border: 'none',
                  backgroundColor: '#808080',
                  color: '#000000ff',
                  fontSize: '1rem',
                  cursor: 'pointer',
                  fontWeight: '500'
                }}
              >
                New Calculation
              </button>
            </div>
          </div>
          );
        })()}
      </div>
    </div>
  );
}
