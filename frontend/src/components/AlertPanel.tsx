"use client";

import { useState } from "react";
import type { PriceAlert } from "@/hooks/useAlerts";

interface Props {
  symbols: string[];
  alerts: PriceAlert[];
  onAdd: (symbol: string, targetPrice: number, direction: "above" | "below") => void;
  onRemove: (id: string) => void;
}

export default function AlertPanel({ symbols, alerts, onAdd, onRemove }: Props) {
  const [symbol, setSymbol] = useState("");
  const [price, setPrice] = useState("");
  const [direction, setDirection] = useState<"above" | "below">("above");

  const inputClass =
    "rounded-lg border border-zinc-700 bg-zinc-800 px-3 py-2 text-sm text-zinc-100 focus:border-cyan-500 focus:outline-none";

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    const target = parseFloat(price);
    const chosen = symbol || symbols[0];
    if (!chosen || !Number.isFinite(target) || target <= 0) return;
    onAdd(chosen, target, direction);
    setPrice("");
  }

  return (
    <div className="rounded-xl border border-zinc-800 bg-zinc-900 p-4">
      <h2 className="mb-3 text-lg font-semibold text-zinc-100">
        Price alerts{" "}
        <span className="text-sm font-normal text-zinc-500">
          notified when a target is crossed
        </span>
      </h2>

      <form onSubmit={handleSubmit} className="mb-4 flex flex-wrap gap-2">
        <select
          value={symbol || symbols[0] || ""}
          onChange={(e) => setSymbol(e.target.value)}
          className={inputClass}
          aria-label="Symbol"
        >
          {symbols.map((s) => (
            <option key={s} value={s}>
              {s}
            </option>
          ))}
        </select>
        <select
          value={direction}
          onChange={(e) => setDirection(e.target.value as "above" | "below")}
          className={inputClass}
          aria-label="Direction"
        >
          <option value="above">rises above</option>
          <option value="below">drops below</option>
        </select>
        <input
          type="number"
          step="any"
          min="0"
          required
          placeholder="Target price ($)"
          value={price}
          onChange={(e) => setPrice(e.target.value)}
          className={`${inputClass} w-40`}
          aria-label="Target price"
        />
        <button
          type="submit"
          className="rounded-lg bg-cyan-600 px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-cyan-500"
        >
          Add alert
        </button>
      </form>

      {alerts.length === 0 ? (
        <p className="text-sm text-zinc-500">No alerts yet.</p>
      ) : (
        <ul className="space-y-2">
          {alerts.map((alert) => (
            <li
              key={alert.id}
              className={`flex items-center justify-between rounded-lg border px-3 py-2 text-sm ${
                alert.triggered
                  ? "border-amber-600/60 bg-amber-950/40 text-amber-300"
                  : "border-zinc-800 bg-zinc-950 text-zinc-300"
              }`}
            >
              <span>
                {alert.triggered ? "🔔 " : ""}
                <strong>{alert.symbol}</strong> {alert.direction === "above" ? "≥" : "≤"} $
                {alert.targetPrice.toLocaleString()}
                {alert.triggered && <span className="ml-2 text-xs">triggered</span>}
              </span>
              <button
                onClick={() => onRemove(alert.id)}
                className="text-zinc-500 transition-colors hover:text-red-400"
                aria-label={`Remove alert for ${alert.symbol}`}
              >
                ✕
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
