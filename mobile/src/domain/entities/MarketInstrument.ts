export interface MarketInstrument {
  id: string; // e.g. "USD", "GLD"
  name: string; // e.g. "Dolar", "Altın"
  symbol: string; // e.g. "USD/TRY"
  currentPrice: number;
  previousPrice: number;
  isUpwardTrend: boolean;
}
