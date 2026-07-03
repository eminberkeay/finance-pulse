export interface Ticker {
  symbol: string;
  price: number;
  openPrice: number;
  highPrice: number;
  lowPrice: number;
  volume: number;
  changePercent: number;
  updatedAt: number;
}

export interface PricePoint {
  time: number;
  price: number;
}
