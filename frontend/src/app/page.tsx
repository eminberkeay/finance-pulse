"use client";

import { useState } from "react";
import LiveChart from "@/components/LiveChart";
import PriceCard from "@/components/PriceCard";
import { usePrices } from "@/hooks/usePrices";

const CONNECTION_LABELS = {
  connecting: { text: "Connecting…", color: "bg-yellow-500" },
  connected: { text: "Live", color: "bg-emerald-500" },
  disconnected: { text: "Reconnecting…", color: "bg-red-500" },
} as const;

export default function Home() {
  const { tickers, history, connection } = usePrices();
  const [selected, setSelected] = useState("BTCUSDT");

  const sorted = Object.values(tickers).sort((a, b) =>
    a.symbol.localeCompare(b.symbol),
  );
  const status = CONNECTION_LABELS[connection];
  const selectedTicker = tickers[selected];

  return (
    <div className="min-h-screen bg-zinc-950 px-6 py-8">
      <div className="mx-auto max-w-5xl">
        <header className="mb-8 flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-zinc-50">
              Finance<span className="text-cyan-400">Pulse</span>
            </h1>
            <p className="text-sm text-zinc-500">
              Real-time crypto dashboard · Spring Boot + Kafka-style streaming via WebSocket
            </p>
          </div>
          <div className="flex items-center gap-2 rounded-full border border-zinc-800 bg-zinc-900 px-3 py-1.5 text-sm text-zinc-300">
            <span className={`h-2 w-2 rounded-full ${status.color} animate-pulse`} />
            {status.text}
          </div>
        </header>

        {sorted.length === 0 ? (
          <div className="flex h-64 items-center justify-center rounded-xl border border-zinc-800 bg-zinc-900 text-zinc-500">
            Waiting for market data — is the backend running on port 8080?
          </div>
        ) : (
          <>
            <div className="mb-6 grid grid-cols-2 gap-4 md:grid-cols-3">
              {sorted.map((ticker) => (
                <PriceCard
                  key={ticker.symbol}
                  ticker={ticker}
                  selected={ticker.symbol === selected}
                  onSelect={setSelected}
                />
              ))}
            </div>
            <LiveChart
              symbol={selected}
              points={history[selected] ?? []}
              up={(selectedTicker?.changePercent ?? 0) >= 0}
            />
          </>
        )}
      </div>
    </div>
  );
}
