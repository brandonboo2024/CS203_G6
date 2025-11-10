import { createContext, useCallback, useContext, useMemo, useState } from "react";

const CalcHistoryContext = createContext(null);
const STORAGE_KEY = "calcHistory";
const MAX_ENTRIES = 50;

const readSessionHistory = () => {
  if (typeof window === "undefined" || !window.sessionStorage) return [];
  try {
    const raw = window.sessionStorage.getItem(STORAGE_KEY);
    const parsed = JSON.parse(raw || "[]");
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
};

const persistHistory = (entries) => {
  if (typeof window === "undefined" || !window.sessionStorage) return;
  if (!entries || entries.length === 0) {
    window.sessionStorage.removeItem(STORAGE_KEY);
    return;
  }
  window.sessionStorage.setItem(STORAGE_KEY, JSON.stringify(entries));
};

export function CalcHistoryProvider({ children }) {
  const [history, setHistory] = useState(() => readSessionHistory());

  const addEntry = useCallback((entry) => {
    setHistory((prev) => {
      const next = [entry, ...prev];
      const trimmed = next.slice(0, MAX_ENTRIES);
      persistHistory(trimmed);
      return trimmed;
    });
  }, []);

  const clearHistory = useCallback(() => {
    persistHistory([]);
    setHistory([]);
  }, []);

  const value = useMemo(
    () => ({
      history,
      addEntry,
      clearHistory,
    }),
    [history, addEntry, clearHistory]
  );

  return (
    <CalcHistoryContext.Provider value={value}>
      {children}
    </CalcHistoryContext.Provider>
  );
}

export function useCalcHistory() {
  const ctx = useContext(CalcHistoryContext);
  if (!ctx) {
    throw new Error("useCalcHistory must be used within a CalcHistoryProvider");
  }
  return ctx;
}
