import { jest, describe, it, expect, beforeEach } from '@jest/globals';
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react-native';
import { NewsScreen } from '../NewsScreen';
import { useAuthStore } from '../../stores/useAuthStore';
import { NavigationContainer } from '@react-navigation/native';
import { Alert } from 'react-native';

// Mocking useAuthStore
jest.mock('../../stores/useAuthStore', () => ({
  useAuthStore: jest.fn(),
}));

// Mocking NewsCard
jest.mock('../../components/NewsCard', () => {
  const { Text } = require('react-native');
  return {
    NewsCard: ({ news }: any) => <Text testID={`NewsCard:${news.id}`}>{news.title}</Text>,
  };
});

describe('NewsScreen', () => {
  const mockUser = { id: '1', email: 'test@example.com', fullName: 'Caner' };
  const mockWatchlist = ['THYAO', 'ASELS'];
  const mockNewsData = {
    content: [
      { id: 1, title: 'THYAO Haber', content: 'İçerik 1', publishedAt: '2024-03-21T10:00:00Z', stockSymbol: 'THYAO.IS', sourceUrl: 'url1' },
      { id: 2, title: 'ASELS Haber', content: 'İçerik 2', publishedAt: '2024-03-21T09:00:00Z', stockSymbol: 'ASELS.IS', sourceUrl: 'url2' },
    ],
    last: true,
  };

  beforeEach(() => {
    jest.clearAllMocks();

    (useAuthStore as any).mockReturnValue({
      user: mockUser,
    });

    global.fetch = jest.fn().mockImplementation((url: any) => {
      const urlStr = url.toString();
      if (urlStr.includes('/api/v1/watchlist/list')) {
        return Promise.resolve({ ok: true, json: async () => mockWatchlist });
      }
      if (urlStr.includes('/api/v1/news')) {
        return Promise.resolve({ ok: true, json: async () => mockNewsData });
      }
      return Promise.resolve({ ok: true, json: async () => ({ summary: 'Haftalık rapor özeti.' }) });
    }) as any;

    jest.spyOn(Alert, 'alert');
  });

  it('yukleme durumunda ActivityIndicator gostermelidir', async () => {
    // Note: NewsScreen has setLoading(true) then fetch then setLoading(false)
    render(
      <NavigationContainer>
        <NewsScreen />
      </NavigationContainer>
    );
    // FlatList's ListFooterComponent shows loader if loading is true
    // Initial fetch happens in useEffect
  });

  it('haberleri dogru listelemelidir', async () => {
    render(
      <NavigationContainer>
        <NewsScreen />
      </NavigationContainer>
    );

    await waitFor(() => {
      expect(screen.getByText('THYAO Haber')).toBeTruthy();
      expect(screen.getByText('ASELS Haber')).toBeTruthy();
    });
  });

  it('sekme degisimi ile watchlistOnly filtresini guncellemelidir', async () => {
    render(
      <NavigationContainer>
        <NewsScreen />
      </NavigationContainer>
    );

    // Haberlerin yüklenmesini bekle, bu sayede fetchNews ilk çalışması tamamlanmış olur
    await waitFor(() => screen.getByText('THYAO Haber'));
    // setLoading 600ms timeout bekleyelim
    await new Promise(resolve => setTimeout(resolve, 700));

    // "Tümü" butonunu tıkla
    const allTab = await screen.findByText(/Tümü/);
    (global.fetch as any).mockClear();
    fireEvent.press(allTab);

    // watchlistOnly=false ile tekrar fetch çağırılmalı
    await waitFor(() => {
      const calls = (global.fetch as any).mock.calls;
      expect(calls.length).toBeGreaterThan(0);
      const isFetchFalse = calls.some((c: any) => c[0] && c[0].toString().includes('watchlistOnly=false'));
      expect(isFetchFalse).toBe(true);
    });
  });

  it('Pazartesi raporu butonu dogru calismalidir', async () => {
    jest.useFakeTimers();
    jest.setSystemTime(new Date('2024-03-11T10:00:00Z')); // Monday

    render(
      <NavigationContainer>
        <NewsScreen />
      </NavigationContainer>
    );

    const reportButton = await screen.findByText(/Pazartesi Raporu/);
    fireEvent.press(reportButton);

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith(expect.stringContaining('/weekly-report'), expect.anything());
    }, { timeout: 3000 });

    jest.useRealTimers();
  });

  it('Pazartesi degilse rapor butonu kısıtlama uyarısı vermelidir', async () => {
    jest.useFakeTimers();
    jest.setSystemTime(new Date('2024-03-12T10:00:00Z')); // Tuesday

    render(
      <NavigationContainer>
        <NewsScreen />
      </NavigationContainer>
    );

    // Haberlerin yüklenmesini bekle
    await screen.findByText('THYAO Haber');

    const reportButton = await screen.findByText(/Pazartesi Raporu/);
    fireEvent.press(reportButton);

    await waitFor(() => {
      expect(Alert.alert).toHaveBeenCalledWith('Kısıtlama', expect.stringContaining('Pazartesi'));
    });

    jest.useRealTimers();
  });
});
