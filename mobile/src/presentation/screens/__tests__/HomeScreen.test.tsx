import { jest, describe, it, expect, beforeEach } from '@jest/globals';
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react-native';
import { HomeScreen } from '../HomeScreen';
import { useAuthStore } from '../../presentation/stores/useAuthStore';
import { ApiMarketRepository } from '../../data/repositories/ApiMarketRepository';
import { NavigationContainer } from '@react-navigation/native';
import { Alert } from 'react-native';

// Mocking stores and repositories
jest.mock('../../presentation/stores/useAuthStore');
jest.mock('../../data/repositories/ApiMarketRepository');

// Mocking components
jest.mock('../../presentation/components/MarketTrendCard', () => {
  const { Text } = require('react-native');
  return {
    MarketTrendCard: ({ instrument }: any) => <Text>{instrument.name}: {instrument.currentPrice}</Text>,
  };
});

describe('HomeScreen', () => {
  const mockUser = { id: '1', email: 'test@example.com', fullName: 'Caner' };
  const mockLogout = jest.fn();
  
  const mockMarketData = [
    { id: 'USD', name: 'Dolar', symbol: 'USD/TRY', currentPrice: 34.25, isUpwardTrend: true },
  ];

  const mockSignals = {
    golden_signals: [{ ticker: 'THYAO.IS', current_price: 250, cross_date: '2024-03-19' }],
    dead_signals: [],
  };

  const mockCooldown = { can_scan: true, remaining_seconds: 0 };

  beforeEach(() => {
    jest.clearAllMocks();
    
    // Mocking useAuthStore
    (useAuthStore as any).mockReturnValue({
      user: mockUser,
      logout: mockLogout,
    });

    // Mocking ApiMarketRepository methods
    (ApiMarketRepository as any).prototype.getMarketSummary.mockResolvedValue(mockMarketData);
    (ApiMarketRepository as any).prototype.getLatestSignals.mockResolvedValue(mockSignals);
    (ApiMarketRepository as any).prototype.getCooldownStatus.mockResolvedValue(mockCooldown);
    (ApiMarketRepository as any).prototype.triggerFullScan.mockResolvedValue({ success: true, message: 'Tarama başlatıldı' });

    // Mocking Alert.alert
    jest.spyOn(Alert, 'alert');
  });

  it('kullanıcı karşılama mesajını doğru göstermelidir', async () => {
    render(
      <NavigationContainer>
        <HomeScreen />
      </NavigationContainer>
    );

    await waitFor(() => {
      expect(screen.getByText('Hoş geldin 👋')).toBeTruthy();
      expect(screen.getByText('Caner')).toBeTruthy();
    });
  });

  it('piyasa özeti verilerini listelemelidir', async () => {
    render(
      <NavigationContainer>
        <HomeScreen />
      </NavigationContainer>
    );

    await waitFor(() => {
      expect(screen.getByText('Dolar: 34.25')).toBeTruthy();
    });
  });

  it('sinyalleri doğru kategorilerde göstermelidir', async () => {
    render(
      <NavigationContainer>
        <HomeScreen />
      </NavigationContainer>
    );

    await waitFor(() => {
      expect(screen.getByText('THYAO')).toBeTruthy();
      expect(screen.getByText('250.00 ₺')).toBeTruthy();
      // Dead signals empty check
      expect(screen.getByText('Şu an için aktif bir sinyal bulunmuyor.')).toBeTruthy();
    });
  });

  it('çıkış yap butonuna basıldığında logout fonksiyonunu çağırmalıdır', async () => {
    render(
      <NavigationContainer>
        <HomeScreen />
      </NavigationContainer>
    );

    const logoutButton = screen.getByText('Çıkış Yap');
    fireEvent.press(logoutButton);

    expect(mockLogout).toHaveBeenCalled();
  });

  it('cooldown aktifse tarama butonu hata uyarısı vermelidir', async () => {
    // Cooldown aktif durumu mockla
    (ApiMarketRepository as any).prototype.getCooldownStatus.mockResolvedValue({
      can_scan: false,
      remaining_seconds: 3600
    });

    render(
      <NavigationContainer>
        <HomeScreen />
      </NavigationContainer>
    );

    await waitFor(() => {
      const scanButton = screen.getByText('1s 0d');
      fireEvent.press(scanButton);
      expect(Alert.alert).toHaveBeenCalledWith(
        'Beklemeniz Gerekiyor',
        expect.stringContaining('1s 0d')
      );
    });
  });
});
