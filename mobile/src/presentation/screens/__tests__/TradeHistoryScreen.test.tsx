import { jest, describe, it, expect, beforeEach } from '@jest/globals';
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react-native';
import { TradeHistoryScreen } from '../TradeHistoryScreen';
import { useGameStore } from '../../stores/useGameStore';
import { NavigationContainer } from '@react-navigation/native';

// Mocking useGameStore
jest.mock('../../stores/useGameStore', () => ({
  useGameStore: jest.fn(),
}));

// Mocking Ionicons
jest.mock('@expo/vector-icons', () => ({
  Ionicons: 'Ionicons',
}));

describe('TradeHistoryScreen', () => {
  const mockFetchHistory = jest.fn();
  const mockHistory = [
    {
      id: 1,
      type: 'BUY',
      timestamp: '2024-03-20T10:00:00Z',
      stockSymbol: 'THYAO',
      totalAmount: 3105.00,
      quantity: 10,
      price: 310.50,
      commission: 1.55,
    },
    {
      id: 2,
      type: 'SELL',
      timestamp: '2024-03-21T14:30:00Z',
      stockSymbol: 'ASELS',
      totalAmount: 550.00,
      quantity: 10,
      price: 55.00,
      commission: 0.28,
    }
  ];

  beforeEach(() => {
    jest.clearAllMocks();
    (useGameStore as any).mockReturnValue({
      history: mockHistory,
      isLoading: false,
      fetchHistory: mockFetchHistory,
    });
  });

  it('yukleme durumunda ActivityIndicator gostermelidir', async () => {
    (useGameStore as any).mockReturnValue({
      history: [],
      isLoading: true,
      fetchHistory: mockFetchHistory,
    });

    render(
      <NavigationContainer>
        <TradeHistoryScreen />
      </NavigationContainer>
    );

    expect(screen.getByTestId('loading-indicator')).toBeTruthy();
  });

  it('islem gecmisini dogru listelemelidir', async () => {
    render(
      <NavigationContainer>
        <TradeHistoryScreen />
      </NavigationContainer>
    );

    await waitFor(() => {
      expect(screen.getByText('THYAO')).toBeTruthy();
      expect(screen.getByText('ASELS')).toBeTruthy();
      expect(screen.getByText('ALIM')).toBeTruthy();
      expect(screen.getByText('SATIM')).toBeTruthy();
      expect(screen.getByText('3105.00 TL')).toBeTruthy();
      expect(screen.getByText('550.00 TL')).toBeTruthy();
    });
  });

  it('boş gecmiş durumunda uyarı mesajı gostermelidir', async () => {
    (useGameStore as any).mockReturnValue({
      history: [],
      isLoading: false,
      fetchHistory: mockFetchHistory,
    });

    render(
      <NavigationContainer>
        <TradeHistoryScreen />
      </NavigationContainer>
    );

    await waitFor(() => {
      expect(screen.getByText('Henüz bir işlem kaydı bulunmuyor.')).toBeTruthy();
    });
  });

  it('acılısta fetchHistory cagırılmalıdır', () => {
    render(
      <NavigationContainer>
        <TradeHistoryScreen />
      </NavigationContainer>
    );

    expect(mockFetchHistory).toHaveBeenCalled();
  });
});
