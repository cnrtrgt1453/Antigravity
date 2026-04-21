import { MarketInstrument } from '../entities/MarketInstrument';

export interface MarketRepository {
  getMarketSummary(): Promise<MarketInstrument[]>;
}
