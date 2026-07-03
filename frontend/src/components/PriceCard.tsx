"use client";

import type { Ticker } from "@/lib/types";

const SYMBOL_NAMES: Record<string, string> = {
  BTCUSDT: "Bitcoin",
  ETHUSDT: "Ethereum",
  SOLUSDT: "Solana",
  BNBUSDT: "BNB",
  XRPUSDT: "XRP",
  DOGEUSDT: "Dogecoin",
  ADAUSDT: "Cardano",
  AVAXUSDT: "Avalanche",
  LINKUSDT: "Chainlink",
};

function formatPrice(price: number): string {
  return price.toLocaleString("en-US", {
    minimumFractionDigits: 2,
    maximumFractionDigits: price < 1 ? 5 : 2,
  });
}

interface Props {
  ticker: Ticker;
  selected: boolean;
  onSelect: (symbol: string) => void;
}

export default function PriceCard({ ticker, selected, onSelect }: Props) {
  const up = ticker.changePercent >= 0;

  return (
    <button
      onClick={() => onSelect(ticker.symbol)}
      className={`rounded-xl border p-4 text-left transition-all hover:scale-[1.02] ${
        selected
          ? "border-cyan-500 bg-cyan-950/40"
          : "border-zinc-800 bg-zinc-900 hover:border-zinc-600"
      }`}
    >
      <div className="flex items-center justify-between">
        <span className="text-sm font-medium text-zinc-400">
          {SYMBOL_NAMES[ticker.symbol] ?? ticker.symbol}
        </span>
        <span
          className={`rounded-full px-2 py-0.5 text-xs font-semibold ${
            up ? "bg-emerald-950 text-emerald-400" : "bg-red-950 text-red-400"
          }`}
        >
          {up ? "▲" : "▼"} {Math.abs(ticker.changePercent).toFixed(2)}%
        </span>
      </div>
      <div className="mt-2 text-2xl font-bold tabular-nums text-zinc-50">
        ${formatPrice(ticker.price)}
      </div>
      <div className="mt-1 flex gap-3 text-xs tabular-nums text-zinc-500">
        <span>H ${formatPrice(ticker.highPrice)}</span>
        <span>L ${formatPrice(ticker.lowPrice)}</span>
      </div>
    </button>
  );
}
