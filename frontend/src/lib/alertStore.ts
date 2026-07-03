import type { Ticker } from "@/lib/types";

export interface PriceAlert {
  id: string;
  symbol: string;
  targetPrice: number;
  direction: "above" | "below";
  triggered: boolean;
}

const STORAGE_KEY = "financepulse.alerts";
const EMPTY: PriceAlert[] = [];

let alerts: PriceAlert[] | null = null;
const listeners = new Set<() => void>();

function read(): PriceAlert[] {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) ?? "[]");
  } catch {
    return [];
  }
}

function snapshot(): PriceAlert[] {
  if (alerts === null) {
    alerts = typeof window === "undefined" ? EMPTY : read();
  }
  return alerts;
}

function set(next: PriceAlert[]) {
  alerts = next;
  localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
  listeners.forEach((listener) => listener());
}

function notify(alert: PriceAlert, price: number) {
  const text = `${alert.symbol} is ${alert.direction} $${alert.targetPrice.toLocaleString()} (now $${price.toLocaleString()})`;
  if (typeof Notification !== "undefined" && Notification.permission === "granted") {
    new Notification("FinancePulse price alert", { body: text });
  }
}

export const alertStore = {
  subscribe(listener: () => void): () => void {
    listeners.add(listener);
    return () => listeners.delete(listener);
  },

  getSnapshot: snapshot,

  getServerSnapshot: (): PriceAlert[] => EMPTY,

  add(symbol: string, targetPrice: number, direction: "above" | "below") {
    if (typeof Notification !== "undefined" && Notification.permission === "default") {
      Notification.requestPermission();
    }
    set([
      ...snapshot(),
      { id: crypto.randomUUID(), symbol, targetPrice, direction, triggered: false },
    ]);
  },

  remove(id: string) {
    set(snapshot().filter((alert) => alert.id !== id));
  },

  /** Marks alerts whose target price was crossed and fires a browser notification. */
  evaluate(tickers: Record<string, Ticker>) {
    let changed = false;
    const next = snapshot().map((alert) => {
      if (alert.triggered) return alert;
      const ticker = tickers[alert.symbol];
      if (!ticker) return alert;
      const hit =
        alert.direction === "above"
          ? ticker.price >= alert.targetPrice
          : ticker.price <= alert.targetPrice;
      if (!hit) return alert;
      changed = true;
      notify(alert, ticker.price);
      return { ...alert, triggered: true };
    });
    if (changed) set(next);
  },
};
