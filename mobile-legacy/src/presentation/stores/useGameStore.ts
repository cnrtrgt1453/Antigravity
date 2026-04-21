import { create } from 'zustand';
import { Config } from '../../config';
import { useAuthStore } from './useAuthStore';

interface PortfolioItem {
  id: number;
  stockSymbol: string;
  quantity: number;
  averageCost: number;
}

interface Portfolio {
  id: number;
  balance: number;
  items: PortfolioItem[];
}

interface TradeHistory {
  id: number;
  stockSymbol: string;
  type: 'BUY' | 'SELL';
  quantity: number;
  price: number;
  commission: number;
  totalAmount: number;
  timestamp: string;
}

interface GameState {
  portfolio: Portfolio | null;
  history: TradeHistory[];
  watchlist: string[];
  isLoading: boolean;
  error: string | null;

  fetchPortfolio: () => Promise<void>;
  fetchHistory: () => Promise<void>;
  fetchWatchlist: () => Promise<void>;
  
  buyStock: (symbol: string, quantity: number, price: number) => Promise<void>;
  sellStock: (symbol: string, quantity: number, price: number) => Promise<void>;
  
  addToWatchlist: (symbol: string) => Promise<void>;
  removeFromWatchlist: (symbol: string) => Promise<void>;
}

export const useGameStore = create<GameState>((set, get) => ({
  portfolio: null,
  history: [],
  watchlist: [],
  isLoading: false,
  error: null,

  fetchPortfolio: async () => {
    set({ isLoading: true, error: null });
    try {
      const response = await fetch(`${Config.JAVA_API_URL}/api/game/portfolio`);
      if (!response.ok) throw new Error('Portföy yüklenemedi');
      const data = await response.json();
      set({ portfolio: data });
    } catch (err: any) {
      set({ error: err.message });
    } finally {
      set({ isLoading: false });
    }
  },

  fetchHistory: async () => {
    try {
      const response = await fetch(`${Config.JAVA_API_URL}/api/game/history`);
      if (!response.ok) throw new Error('İşlem geçmişi yüklenemedi');
      const data = await response.json();
      set({ history: data });
    } catch (err: any) {
      console.error(err);
    }
  },

  fetchWatchlist: async () => {
    try {
      const response = await fetch(`${Config.JAVA_API_URL}/api/game/watchlist`);
      if (!response.ok) throw new Error('İzleme listesi yüklenemedi');
      const data = await response.json();
      set({ watchlist: data });
    } catch (err: any) {
      console.error(err);
    }
  },

  buyStock: async (symbol, quantity, price) => {
    set({ isLoading: true, error: null });
    try {
      const response = await fetch(`${Config.JAVA_API_URL}/api/game/buy`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ symbol, quantity, price }),
      });
      if (!response.ok) {
        const errData = await response.json();
        throw new Error(errData.error || 'Alım işlemi başarısız');
      }
      const updatedPortfolio = await response.json();
      set({ portfolio: updatedPortfolio });
      get().fetchHistory(); // Geçmişi tazele
    } catch (err: any) {
      set({ error: err.message });
      throw err;
    } finally {
      set({ isLoading: false });
    }
  },

  sellStock: async (symbol, quantity, price) => {
    set({ isLoading: true, error: null });
    try {
      const response = await fetch(`${Config.JAVA_API_URL}/api/game/sell`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ symbol, quantity, price }),
      });
      if (!response.ok) {
        const errData = await response.json();
        throw new Error(errData.error || 'Satım işlemi başarısız');
      }
      const updatedPortfolio = await response.json();
      set({ portfolio: updatedPortfolio });
      get().fetchHistory(); // Geçmişi tazele
    } catch (err: any) {
      set({ error: err.message });
      throw err;
    } finally {
      set({ isLoading: false });
    }
  },

  addToWatchlist: async (symbol) => {
    try {
      const response = await fetch(`${Config.JAVA_API_URL}/api/game/watchlist`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ symbol }),
      });
      if (response.ok) {
        set(state => ({ watchlist: [...state.watchlist, symbol] }));
      }
    } catch (err) {
      console.error(err);
    }
  },

  removeFromWatchlist: async (symbol) => {
    try {
      const response = await fetch(`${Config.JAVA_API_URL}/api/game/watchlist/${symbol}`, {
        method: 'DELETE',
      });
      if (response.ok) {
        set(state => ({ watchlist: state.watchlist.filter(s => s !== symbol) }));
      }
    } catch (err) {
      console.error(err);
    }
  },
}));
