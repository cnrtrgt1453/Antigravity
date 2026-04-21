import { jest, describe, it, expect, beforeEach } from '@jest/globals';
import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react-native';
import { GameScreen } from '../GameScreen';
import { useGameStore } from '../../stores/useGameStore';
import { NavigationContainer } from '@react-navigation/native';

// Mocking useGameStore
jest.mock('../../stores/useGameStore', () => ({
  useGameStore: jest.fn(),
}));

// Mocking Expo vector icons
jest.mock('@expo/vector-icons', () => ({
  Ionicons: 'Ionicons',
}));

describe('GameScreen', () => {
  const mockState = {
    portfolio: { balance: 15000, items: [] },
    history: [],
    watchlist: ['THYAO'],
    isLoading: false,
    fetchPortfolio: jest.fn(),
    fetchHistory: jest.fn(),
    fetchWatchlist: jest.fn(),
    removeFromWatchlist: jest.fn(),
    buyStock: jest.fn(),
    sellStock: jest.fn(),
  };

  beforeEach(() => {
    (useGameStore as any).mockReturnValue(mockState);
    global.fetch = jest.fn().mockImplementation(() => Promise.resolve({
      ok: true,
      json: async () => [{ ticker: 'THYAO', current_price: 250 }],
    })) as any;
  });

  it('başlığı ve bakiyeyi doğru göstermelidir', async () => {
    render(
      <NavigationContainer>
        <GameScreen />
      </NavigationContainer>
    );

    expect(screen.getByText('Oyun Paneli')).toBeTruthy();
    expect(screen.getByText('15000.00 TL')).toBeTruthy();
  });

  it('izleme listesindeki hisseyi listelemelidir', async () => {
    render(
      <NavigationContainer>
        <GameScreen />
      </NavigationContainer>
    );

    // THYAO sembolünün ekranda olduğunu kontrol et
    expect(screen.getByText('THYAO')).toBeTruthy();
  });

  it('portföy boşsa uygun metni göstermelidir', () => {
    render(
      <NavigationContainer>
        <GameScreen />
      </NavigationContainer>
    );

    expect(screen.getByText('Portföy Boş')).toBeTruthy();
  });
});
