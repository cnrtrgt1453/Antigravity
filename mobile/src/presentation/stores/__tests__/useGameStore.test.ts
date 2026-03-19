import { jest, describe, it, expect, beforeEach } from '@jest/globals';
import { useGameStore } from '../useGameStore';

// Mocking global fetch
(global as any).fetch = jest.fn();

describe('useGameStore', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    // Reset Zustand state before each test
    const { portfolio, history, watchlist, isLoading, error } = useGameStore.getState();
    useGameStore.setState({
      portfolio: null,
      history: [],
      watchlist: [],
      isLoading: false,
      error: null,
    });
  });

  it('şasi durumunu doğru başlatmalıdır', () => {
    const state = useGameStore.getState();
    expect(state.portfolio).toBeNull();
    expect(state.history).toHaveLength(0);
    expect(state.isLoading).toBe(false);
  });

  it('fetchPortfolio başarılı olduğunda portföyü güncellemelidir', async () => {
    const mockPortfolio = {
      id: 1,
      balance: 10000,
      items: [{ id: 1, stockSymbol: 'THYAO', quantity: 10, averageCost: 200 }]
    };

    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => mockPortfolio,
    });

    await useGameStore.getState().fetchPortfolio();

    const state = useGameStore.getState();
    expect(state.portfolio).toEqual(mockPortfolio);
    expect(state.isLoading).toBe(false);
    expect(state.error).toBeNull();
  });

  it('fetchPortfolio hata aldığında error durumunu güncellemelidir', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: false,
    });

    await useGameStore.getState().fetchPortfolio();

    const state = useGameStore.getState();
    expect(state.portfolio).toBeNull();
    expect(state.error).toBe('Portföy yüklenemedi');
    expect(state.isLoading).toBe(false);
  });

  it('buyStock başarılı olduğunda portföyü güncellemeli ve geçmişi yenilemelidir', async () => {
    const mockUpdatedPortfolio = { balance: 9000, items: [] };
    
    (global.fetch as jest.Mock)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => mockUpdatedPortfolio,
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => [], // fetchHistory için
      });

    await useGameStore.getState().buyStock('THYAO', 5, 200);

    const state = useGameStore.getState();
    expect(state.portfolio).toEqual(mockUpdatedPortfolio);
    expect(global.fetch).toHaveBeenCalledTimes(2);
  });
});
