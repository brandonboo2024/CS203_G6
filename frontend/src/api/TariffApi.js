// If you added the proxy above, BASE is empty so you call "/api/..."
// If you didn't add the proxy, set BASE = "http://localhost:8080"
const BASE = "";

export async function getTariffs({ hsCode, country, asOf }) {
  const params = new URLSearchParams({ hsCode, country, ...(asOf && { asOf }) });
  const res = await fetch(`${BASE}/api/tariffs?${params}`);
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}

export async function syncTariffs({ hsCode, country }) {
  const params = new URLSearchParams({ hsCode, country });
  const res = await fetch(`${BASE}/api/tariffs/sync?${params}`, { method: 'POST' });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}
