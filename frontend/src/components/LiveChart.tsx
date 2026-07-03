"use client";

import {
  Area,
  AreaChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import type { PricePoint } from "@/lib/types";

interface Props {
  symbol: string;
  points: PricePoint[];
  up: boolean;
}

export default function LiveChart({ symbol, points, up }: Props) {
  const color = up ? "#34d399" : "#f87171";

  if (points.length < 2) {
    return (
      <div className="flex h-80 items-center justify-center rounded-xl border border-zinc-800 bg-zinc-900 text-zinc-500">
        Waiting for live data for {symbol}…
      </div>
    );
  }

  return (
    <div className="rounded-xl border border-zinc-800 bg-zinc-900 p-4">
      <h2 className="mb-4 text-lg font-semibold text-zinc-100">
        {symbol} <span className="text-sm font-normal text-zinc-500">live</span>
      </h2>
      <ResponsiveContainer width="100%" height={300}>
        <AreaChart data={points}>
          <defs>
            <linearGradient id="priceGradient" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor={color} stopOpacity={0.35} />
              <stop offset="100%" stopColor={color} stopOpacity={0} />
            </linearGradient>
          </defs>
          <CartesianGrid strokeDasharray="3 3" stroke="#27272a" />
          <XAxis
            dataKey="time"
            tickFormatter={(t: number) => new Date(t).toLocaleTimeString()}
            stroke="#71717a"
            fontSize={12}
            minTickGap={60}
          />
          <YAxis
            domain={["auto", "auto"]}
            stroke="#71717a"
            fontSize={12}
            tickFormatter={(v: number) => v.toLocaleString("en-US")}
            width={80}
          />
          <Tooltip
            contentStyle={{
              backgroundColor: "#18181b",
              border: "1px solid #3f3f46",
              borderRadius: 8,
              color: "#fafafa",
            }}
            labelFormatter={(t: number) => new Date(t).toLocaleTimeString()}
            formatter={(value: number) => [`$${value.toLocaleString("en-US")}`, "Price"]}
          />
          <Area
            type="monotone"
            dataKey="price"
            stroke={color}
            strokeWidth={2}
            fill="url(#priceGradient)"
            isAnimationActive={false}
          />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  );
}
