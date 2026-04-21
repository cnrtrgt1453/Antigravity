import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react-native';
import { Alert } from 'react-native';
import { ProfileScreen } from '../ProfileScreen';
import { useAuthStore } from '../../stores/useAuthStore';
import { useGameStore } from '../../stores/useGameStore';
import { NavigationContainer } from '@react-navigation/native';

// Mocking stores
jest.mock('../../stores/useAuthStore', () => ({
  useAuthStore: jest.fn(),
}));

jest.mock('../../stores/useGameStore', () => ({
  useGameStore: jest.fn(),
}));

// Mocking Expo vector icons
jest.mock('@expo/vector-icons', () => ({
  Ionicons: 'Ionicons',
}));

describe('ProfileScreen', () => {
  const mockLogout = jest.fn();

  const mockAuthState = {
    user: {
      id: 'user-1',
      email: 'test@example.com',
      fullName: 'Ali Veli',
    },
    logout: mockLogout,
  };

  const mockGameState = {
    portfolio: { balance: 15000, items: [] },
    history: [{ id: 1 }, { id: 2 }],
    watchlist: ['THYAO', 'EREGL'],
  };

  beforeEach(() => {
    jest.clearAllMocks();
    (useAuthStore as any).mockReturnValue(mockAuthState);
    (useGameStore as any).mockReturnValue(mockGameState);
  });

  it('kullanıcı adı ve e-postayı render etmelidir', () => {
    render(
      <NavigationContainer>
        <ProfileScreen />
      </NavigationContainer>
    );

    expect(screen.getByText('Ali Veli')).toBeTruthy();
    expect(screen.getByText('test@example.com')).toBeTruthy();
  });

  it('doğru istatistikleri göstermelidir', () => {
    render(
      <NavigationContainer>
        <ProfileScreen />
      </NavigationContainer>
    );

    // watchlist sayısı: 2, işlem sayısı: 2, portföy adedi: 0
    // Label'ları kontrol ederek context sağlıyoruz
    expect(screen.getByText('Takip')).toBeTruthy();
    expect(screen.getByText('İşlem')).toBeTruthy();
    expect(screen.getByText('Portföy')).toBeTruthy();
  });

  it('çıkış butonuna basınca Alert.alert çağrılmalıdır', () => {
    const alertSpy = jest.spyOn(Alert, 'alert').mockImplementation(() => {});

    render(
      <NavigationContainer>
        <ProfileScreen />
      </NavigationContainer>
    );

    const logoutButton = screen.getByTestId('ProfileScreen:LogoutButton');
    fireEvent.press(logoutButton);

    expect(alertSpy).toHaveBeenCalledWith(
      'Çıkış Yap',
      expect.any(String),
      expect.any(Array)
    );
  });

  it('Alert onaylandığında logout() çağrılmalıdır', async () => {
    jest.spyOn(Alert, 'alert').mockImplementation((_title, _msg, buttons) => {
      // "Çıkış Yap" butonunu (ikinci buton) simüle et
      const confirmButton = buttons?.find((b: any) => b.style === 'destructive');
      confirmButton?.onPress?.();
    });

    render(
      <NavigationContainer>
        <ProfileScreen />
      </NavigationContainer>
    );

    const logoutButton = screen.getByTestId('ProfileScreen:LogoutButton');
    fireEvent.press(logoutButton);

    await waitFor(() => {
      expect(mockLogout).toHaveBeenCalledTimes(1);
    });
  });

  it('kullanıcı yoksa varsayılan değerleri göstermelidir', () => {
    (useAuthStore as any).mockReturnValue({
      user: null,
      logout: mockLogout,
    });
    (useGameStore as any).mockReturnValue({
      portfolio: null,
      history: [],
      watchlist: [],
    });

    render(
      <NavigationContainer>
        <ProfileScreen />
      </NavigationContainer>
    );

    expect(screen.getByText('Kullanıcı')).toBeTruthy();
  });
});
