import { jest, describe, it, expect, beforeEach } from '@jest/globals';
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react-native';
import { SignalsScreen } from '../SignalsScreen';
import { NavigationContainer } from '@react-navigation/native';

// Mock Ionicons
jest.mock('@expo/vector-icons', () => ({
  Ionicons: 'Ionicons',
}));

describe('SignalsScreen', () => {
  const mockSignalsData = {
    golden_signals: [
      {
        ticker: 'THYAO',
        signal: 'Golden Cross',
        current_price: 310.50,
        sma50: 305.00,
        sma200: 302.00,
        cross_price: 303.00,
        cross_date: '2024-03-20',
        message: 'THYAO hissesinde Golden Cross oluştu.',
      }
    ],
    dead_signals: [
      {
        ticker: 'ASELS',
        signal: 'Dead Cross',
        current_price: 55.20,
        sma50: 56.00,
        sma200: 58.00,
        cross_price: 57.50,
        cross_date: '2024-03-19',
        message: 'ASELS hissesinde Dead Cross oluştu.',
      }
    ]
  };

  beforeEach(() => {
    jest.clearAllMocks();

    global.fetch = jest.fn().mockImplementation(() =>
      Promise.resolve({
        ok: true,
        json: async () => mockSignalsData,
      })
    ) as any;
  });

  it('yukleme durumunda ActivityIndicator gostermelidir', async () => {
    // Mock fetch to be delayed indefinitely for this test
    let resolveFetch: any;
    const fetchPromise = new Promise(resolve => {
      resolveFetch = resolve;
    });
    global.fetch = jest.fn().mockReturnValue(fetchPromise) as any;

    render(
      <NavigationContainer>
        <SignalsScreen />
      </NavigationContainer>
    );

    // ActivityIndicator should be present initially
    expect(screen.getByTestId('loading-indicator')).toBeTruthy();
    
    // Resolve to avoid memory leaks/console errors
    resolveFetch({ ok: true, json: async () => ({ golden_signals: [], dead_signals: [] }) });
  });

  it('sinyalleri dogru listelemelidir', async () => {
    render(
      <NavigationContainer>
        <SignalsScreen />
      </NavigationContainer>
    );

    await waitFor(() => {
      expect(screen.getByText('THYAO')).toBeTruthy();
      expect(screen.getByText('ASELS')).toBeTruthy();
    }, { timeout: 3000 });
  });

  it('filtreleme butonları dogru calismalidir', async () => {
    render(
      <NavigationContainer>
        <SignalsScreen />
      </NavigationContainer>
    );

    await waitFor(() => screen.getByTestId('chip-GOLDEN'));

    const goldenChip = screen.getByTestId('chip-GOLDEN');
    fireEvent.press(goldenChip);

    await waitFor(() => {
      expect(screen.queryByText('THYAO')).toBeTruthy();
      expect(screen.queryByText('ASELS')).toBeNull();
    });

    const deadChip = screen.getByTestId('chip-DEAD');
    fireEvent.press(deadChip);

    await waitFor(() => {
      expect(screen.queryByText('THYAO')).toBeNull();
      expect(screen.queryByText('ASELS')).toBeTruthy();
    });
  });

  it('yeni veriler icin yenileme (pull-to-refresh) calismalidir', async () => {
    render(
      <NavigationContainer>
        <SignalsScreen />
      </NavigationContainer>
    );

    // Initial load bekleyin
    await waitFor(() => screen.getByTestId('signals-list'), { timeout: 3000 });

    // Mock'u temizle
    (global.fetch as any).mockClear();

    const refreshControl = screen.getByTestId('refresh-control');
    
    // onRefresh'i tetikle
    fireEvent(refreshControl, 'onRefresh');

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalled();
    }, { timeout: 3000 });
  });

  it('sinyal bulunamadıgında bos liste mesajı gostermelidir', async () => {
    global.fetch = jest.fn().mockImplementation(() =>
      Promise.resolve({
        ok: true,
        json: async () => ({ golden_signals: [], dead_signals: [] }),
      })
    ) as any;

    render(
      <NavigationContainer>
        <SignalsScreen />
      </NavigationContainer>
    );

    await waitFor(() => {
      expect(screen.getByText('Bu filtreye uygun sinyal bulunamadı.')).toBeTruthy();
    });
  });
});
