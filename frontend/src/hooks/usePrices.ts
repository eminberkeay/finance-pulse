"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import type { PricePoint, Ticker } from "@/lib/types";

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
const WS_URL = API_URL.replace(/^http/, "ws") + "/ws/prices";
const MAX_HISTORY = 240;

export type ConnectionState = "connecting" | "connected" | "disconnected";

export function usePrices() {
  const [tickers, setTickers] = useState<Record<string, Ticker>>({});
  const [history, setHistory] = useState<Record<string, PricePoint[]>>({});
  const [connection, setConnection] = useState<ConnectionState>("connecting");
  const reconnectTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const loadedHistory = useRef<Set<string>>(new Set());

  // Seed the chart with recent 1-minute candles so it isn't empty on first view.
  const loadHistory = useCallback(async (symbol: string) => {
    if (loadedHistory.current.has(symbol)) return;
    loadedHistory.current.add(symbol);
    try {
      const res = await fetch(`${API_URL}/api/history/${symbol}?limit=60`);
      if (!res.ok) throw new Error(res.statusText);
      const seed: PricePoint[] = await res.json();
      setHistory((prev) => ({
        ...prev,
        [symbol]: [...seed, ...(prev[symbol] ?? [])].slice(-MAX_HISTORY),
      }));
    } catch {
      loadedHistory.current.delete(symbol);
    }
  }, []);

  useEffect(() => {
    let ws: WebSocket | null = null;
    let disposed = false;

    fetch(`${API_URL}/api/prices`)
      .then((res) => res.json())
      .then((initial: Ticker[]) => {
        if (disposed) return;
        setTickers(Object.fromEntries(initial.map((t) => [t.symbol, t])));
      })
      .catch(() => {});

    function connect() {
      if (disposed) return;
      setConnection("connecting");
      ws = new WebSocket(WS_URL);

      ws.onopen = () => setConnection("connected");

      ws.onmessage = (event) => {
        const ticker: Ticker = JSON.parse(event.data);
        setTickers((prev) => ({ ...prev, [ticker.symbol]: ticker }));
        setHistory((prev) => {
          const points = prev[ticker.symbol] ?? [];
          const next = [...points, { time: ticker.updatedAt, price: ticker.price }];
          return {
            ...prev,
            [ticker.symbol]: next.slice(-MAX_HISTORY),
          };
        });
      };

      ws.onclose = () => {
        if (disposed) return;
        setConnection("disconnected");
        reconnectTimer.current = setTimeout(connect, 3000);
      };
    }

    connect();

    return () => {
      disposed = true;
      if (reconnectTimer.current) clearTimeout(reconnectTimer.current);
      ws?.close();
    };
  }, []);

  return { tickers, history, connection, loadHistory };
}
