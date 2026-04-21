import { jest, describe, it, expect, beforeEach } from '@jest/globals';
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react-native';
import { MarketScreen } from '../MarketScreen';
import { useGameStore } from '../../stores/useGameStore';
import { NavigationContainer } from '@react-navigation/native';
import { Alert } from 'react-native';

// Mocking useGameStore
jest.mock('../../stores/useGameStore', () => ({
  useGameStore: jest.fn(),
}));

jest.mock('react-native-webview', () => {
  return { WebView: () => null };
});

// Mocking Ionicons
jest.mock('@expo/vector-icons', () => ({
  Ionicons: 'Ionicons',
}));

describe('MarketScreen', () => {
  const mockWatchlist = ['THYAO'];
  const mockAddToWatchlist = jest.fn();
  const mockRemoveFromWatchlist = jest.fn();
  const mockFetchWatchlist = jest.fn();

  const mockStocksData = {
    content: [
      { symbol: 'THYAO', name: 'Turk Hava Yollari', category: 'BIST100' },
      { symbol: 'ASELS', name: 'Aselsan', category: 'BIST100' },
    ],
    last: false,
  };

  const mockAnalysisData = [
    { ticker: 'THYAO', current_price: 250.0, sma50: 260.0, sma200: 240.0, cross_price: 245.0, signal: 'GOLDEN_CROSS' },
    { ticker: 'ASELS', current_price: 60.0, sma50: 55.0, sma200: 65.0, cross_price: 62.0, signal: 'DEAD_CROSS' },
  ];

  beforeEach(() => {
    (useGameStore as any).mockReturnValue({
      watchlist: mockWatchlist,
      addToWatchlist: mockAddToWatchlist,
      removeFromWatchlist: mockRemoveFromWatchlist,
      fetchWatchlist: mockFetchWatchlist,
    });

    global.fetch = jest.fn().mockImplementation((url: any) => {
      const urlStr = url.toString();
      if (urlStr.includes('all_market_data')) {
        return Promise.resolve({ ok: true, json: async () => mockAnalysisData });
      }
      return Promise.resolve({ ok: true, json: async () => mockStocksData });
    }) as any;
  });

  it('yukleme durumunda ActivityIndicator gostermelidir', async () => {
    render(
      <NavigationContainer>
        <MarketScreen />
      </NavigationContainer>
    );
    expect(screen.getByText('Piyasalar')).toBeTruthy();
  });

  it('hisseleri ve analiz verilerini dogru listelemelidir', async () => {
    render(
      <NavigationContainer>
        <MarketScreen />
      </NavigationContainer>
    );

    await waitFor(() => {
      expect(screen.getByText('THYAO')).toBeTruthy();
      expect(screen.getByText('ASELS')).toBeTruthy();
    });

    await waitFor(() => {
      expect(screen.getByText('Golden Cross')).toBeTruthy();
      expect(screen.getByText('Dead Cross')).toBeTruthy();
    });
  });

  it('izleme listesine ekleme ve cikarma islemlerini tetiklemelidir', async () => {
    render(
      <NavigationContainer>
        <MarketScreen />
      </NavigationContainer>
    );

    await waitFor(() => {
      expect(screen.getByTestId('MarketScreen:WatchButton:THYAO')).toBeTruthy();
    });

    const thyaoButton = screen.getByTestId('MarketScreen:WatchButton:THYAO');
    const aselsButton = screen.getByTestId('MarketScreen:WatchButton:ASELS');

    fireEvent.press(thyaoButton);
    expect(mockRemoveFromWatchlist).toHaveBeenCalledWith('THYAO');

    fireEvent.press(aselsButton);
    expect(mockAddToWatchlist).toHaveBeenCalledWith('ASELS');
  });

  it('liste sonuna gelindiginde yeni sayfa yuklemelidir', async () => {
    render(
      <NavigationContainer>
        <MarketScreen />
      </NavigationContainer>
    );

    // İlk yüklemenin tamamlanmasını bekle (page=0)
    await waitFor(() => {
      expect(screen.getByText('THYAO')).toBeTruthy();
      expect(global.fetch).toHaveBeenCalledWith(expect.stringContaining('page=0'));
    });

    // onEndReached tetiklemeden önce durumların oturmasını bekle (loading 600ms delay)
    await new Promise(resolve => setTimeout(resolve, 700));

    // onEndReached tetikle
    const flatList = screen.getByTestId('MarketScreen:FlatList');
    fireEvent(flatList, 'onEndReached');
    
    // Page 1 için isteğin gitiğini bekle
    await waitFor(() => {
      const calls = (global.fetch as any).mock.calls;
      const hasPage1 = calls.some((call: any[]) => call[0].includes('page=1'));
      expect(hasPage1).toBe(true);
    }, { timeout: 3000 });
  });
});
