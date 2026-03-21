import { MarketInstrument } from '../../domain/entities/MarketInstrument';
import { MarketRepository } from '../../domain/repositories/MarketRepository';
import { Config } from '../../config';

export interface SignalData {
  ticker: string;
  signal: string;
  color: string;
  message: string;
  cross_date: string | null;
  current_price: number | null;
  last_updated: string | null;
}

export interface SignalsResponse {
  golden_signals: SignalData[];
  dead_signals: SignalData[];
}

export interface CooldownStatus {
  can_scan: boolean;
  remaining_seconds: number;
}

export class ApiMarketRepository implements MarketRepository {
  async getLatestSignals(): Promise<SignalsResponse> {
    const response = await fetch(`${Config.JAVA_API_URL}/api/v1/signals`);
    if (!response.ok) throw new Error('Sinyaller alınamadı.');
    return await response.json();
  }

  async getCooldownStatus(): Promise<CooldownStatus> {
    const response = await fetch(`${Config.PYTHON_API_URL}/api/v1/analysis/cooldown_status`);
    if (!response.ok) throw new Error('Cooldown durumu alınamadı.');
    return await response.json();
  }

  async triggerFullScan(): Promise<{ success: boolean; message: string }> {
    const response = await fetch(`${Config.PYTHON_API_URL}/api/v1/analysis/run_full_scan_now`);
    const result = await response.json();
    if (!response.ok) {
        return { success: false, message: result.detail || 'Tarama hatası' };
    }
    return { success: true, message: 'Tarama başarıyla başlatıldı.' };
  }

  async getMarketSummary(): Promise<MarketInstrument[]> {
    try {
      const response = await fetch('https://finans.truncgil.com/today.json');
      if (!response.ok) {
        throw new Error('Piyasa verileri alınamadı.');
      }
      
      const data = await response.json();
      
      // The API returns values as strings with commas like "34,5010"
      const parsePrice = (priceStr: string | undefined): number => {
        if (!priceStr) return 0;
        return parseFloat(priceStr.replace('.', '').replace(',', '.')); // Handle Turkish format "3.450,10" if present or "34,50" -> 34.50
      };

      const instruments: MarketInstrument[] = [];

      // 1. Dolar
      if (data['USD']) {
        const current = parsePrice(data['USD'].Alış);
        const previous = parsePrice(data['USD'].Tür) === 0 ? current : current / (1 + parseFloat((data['USD'].Değişim || '0').replace('%', '').replace(',', '.')) / 100);
        const isUpward = (data['USD'].Değişim || '').includes('%'); // Depending on the api, usually +/- indicator or we can just parse the direction arrow
        // Actually a better way to check trend is just to compare the value of 'Değişim' which starts with %- or %+
        const changeStr = typeof data['USD'].Değişim === 'string' ? data['USD'].Değişim : '';
        const isUp = !changeStr.startsWith('%-');
        
        instruments.push({
          id: 'USD',
          name: 'Dolar',
          symbol: 'USD/TRY',
          currentPrice: current,
          previousPrice: current, // Without historic, tricky, let's just use current unless calculated.
          isUpwardTrend: isUp
        });
      }

      // 2. Euro
      if (data['EUR']) {
        const current = parsePrice(data['EUR'].Alış);
        const changeStr = typeof data['EUR'].Değişim === 'string' ? data['EUR'].Değişim : '';
        const isUp = !changeStr.startsWith('%-');
        instruments.push({
          id: 'EUR',
          name: 'Euro',
          symbol: 'EUR/TRY',
          currentPrice: current,
          previousPrice: current,
          isUpwardTrend: isUp
        });
      }
      
      // 3. Sterlin
      if (data['GBP']) {
        const current = parsePrice(data['GBP'].Alış);
         const changeStr = typeof data['GBP'].Değişim === 'string' ? data['GBP'].Değişim : '';
        const isUp = !changeStr.startsWith('%-');
        instruments.push({
          id: 'GBP',
          name: 'Sterlin',
          symbol: 'GBP/TRY',
          currentPrice: current,
          previousPrice: current,
          isUpwardTrend: isUp
        });
      }

      // 4. Altın (Gram Altın)
      if (data['gram-altin']) {
        const current = parsePrice(data['gram-altin'].Alış);
        const changeStr = typeof data['gram-altin'].Değişim === 'string' ? data['gram-altin'].Değişim : '';
        const isUp = !changeStr.startsWith('%-');
        instruments.push({
          id: 'GLD',
          name: 'Altın',
          symbol: 'XAU/TRY',
          currentPrice: current,
          previousPrice: current,
          isUpwardTrend: isUp
        });
      }

      // 5. Gümüş
      if (data['gumus']) {
        const current = parsePrice(data['gumus'].Alış);
        const changeStr = typeof data['gumus'].Değişim === 'string' ? data['gumus'].Değişim : '';
        const isUp = !changeStr.startsWith('%-');
        instruments.push({
          id: 'SLV',
          name: 'Gümüş',
          symbol: 'XAG/TRY',
          currentPrice: current,
          previousPrice: current,
          isUpwardTrend: isUp
        });
      }

      return instruments;
    } catch (error) {
      console.error("Market veri cekme hatasi: ", error);
      // Fallback data in case the public API fails
      return [
        { id: 'USD', name: 'Dolar', symbol: 'USD/TRY', currentPrice: 34.25, previousPrice: 34.00, isUpwardTrend: true },
        { id: 'EUR', name: 'Euro', symbol: 'EUR/TRY', currentPrice: 37.80, previousPrice: 38.00, isUpwardTrend: false },
        { id: 'GBP', name: 'Sterlin', symbol: 'GBP/TRY', currentPrice: 44.50, previousPrice: 44.20, isUpwardTrend: true },
        { id: 'GLD', name: 'Altın', symbol: 'XAU/TRY', currentPrice: 2800.50, previousPrice: 2750.00, isUpwardTrend: true },
        { id: 'SLV', name: 'Gümüş', symbol: 'XAG/TRY', currentPrice: 35.40, previousPrice: 36.10, isUpwardTrend: false },
      ];
    }
  }
}
