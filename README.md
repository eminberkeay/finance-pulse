# FinancePulse 📈

Real-time cryptocurrency dashboard built with **Spring Boot** and **Next.js**.

The backend connects to Binance's public WebSocket stream, normalizes live mini-ticker
events, and rebroadcasts them to browser clients over its own WebSocket endpoint.
The frontend renders live price cards and a streaming chart with Recharts.

![Stack](https://img.shields.io/badge/Java-21-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green) ![Next.js](https://img.shields.io/badge/Next.js-16-black) ![TypeScript](https://img.shields.io/badge/TypeScript-5-blue)

## Architecture

```
Binance WS API ──▶ Spring Boot backend ──▶ WebSocket /ws/prices ──▶ Next.js dashboard
                        │
                        └──▶ REST /api/prices (initial snapshot)
```

- **Backend** (`backend/`): Spring Boot 3, Java 21
  - `BinanceStreamClient` — resilient WebSocket client with auto-reconnect
  - `TickerMapper` — parses Binance miniTicker payloads (unit tested)
  - `PriceBroadcastHandler` — fans out ticker updates to all connected clients
  - `PriceController` — REST snapshot of latest prices + 1-minute kline history
- **Frontend** (`frontend/`): Next.js 16 (App Router), TypeScript, Tailwind CSS, Recharts
  - `usePrices` hook — WebSocket client with reconnect + rolling price history,
    seeded with recent kline history so charts are never empty
  - `useAlerts` hook — price alerts persisted in localStorage with browser notifications
  - Live price cards, a real-time area chart, and an alert panel

## Quick start with Docker

```bash
docker compose up --build
```

Then open http://localhost:3000.

## Running locally

**Backend** (port 8080):

```bash
cd backend
./mvnw spring-boot:run
```

**Frontend** (port 3000):

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:3000 — no API keys needed; Binance's public stream is free.

## Tests

```bash
cd backend
./mvnw test
```

## Configuration

Symbols are configurable in `backend/src/main/resources/application.properties`:

```properties
financepulse.symbols=btcusdt,ethusdt,solusdt,bnbusdt,xrpusdt,dogeusdt,adausdt,avaxusdt,linkusdt
```

The frontend reads the backend URL from `NEXT_PUBLIC_API_URL` (defaults to `http://localhost:8080`).
