import { jest, describe, it, expect, beforeEach } from '@jest/globals';
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react-native';
import { LoginScreen } from '../LoginScreen';
import { useAuthStore } from '../../stores/useAuthStore';
import { NavigationContainer } from '@react-navigation/native';

// Mocking useAuthStore
jest.mock('../../stores/useAuthStore', () => ({
  useAuthStore: jest.fn(),
}));

// Mocking Expo Constants
jest.mock('expo-constants', () => ({
  appOwnership: 'standalone',
}));

describe('LoginScreen', () => {
  const mockNavigation = {
    navigate: jest.fn(),
  };

  const mockAuthState = {
    user: null,
    isLoading: false,
    error: null,
    login: jest.fn(),
    loginWithGoogle: jest.fn(),
    clearError: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
    (useAuthStore as any).mockReturnValue(mockAuthState);
  });

  it('başlık ve input alanlarını doğru render etmelidir', () => {
    render(
      <NavigationContainer>
        <LoginScreen navigation={mockNavigation as any} />
      </NavigationContainer>
    );

    expect(screen.getByText('Borsa Analiz')).toBeTruthy();
    expect(screen.getByPlaceholderText('ornek@email.com')).toBeTruthy();
    expect(screen.getByPlaceholderText('••••••••')).toBeTruthy();
  });

  it('Giriş Yap butonuna tıklandığında login fonksiyonunu çağırmalıdır', async () => {
    render(
      <NavigationContainer>
        <LoginScreen navigation={mockNavigation as any} />
      </NavigationContainer>
    );

    const emailInput = screen.getByPlaceholderText('ornek@email.com');
    const passwordInput = screen.getByPlaceholderText('••••••••');
    const loginButton = screen.getByText('Giriş Yap');

    fireEvent.changeText(emailInput, 'test@example.com');
    fireEvent.changeText(passwordInput, 'password123');
    fireEvent.press(loginButton);

    expect(mockAuthState.login).toHaveBeenCalledWith('test@example.com', 'password123');
  });

  it('Hata oluştuğunda hata mesajını göstermelidir', () => {
    (useAuthStore as any).mockReturnValue({
      ...mockAuthState,
      error: 'E-posta veya şifre hatalı',
    });

    render(
      <NavigationContainer>
        <LoginScreen navigation={mockNavigation as any} />
      </NavigationContainer>
    );

    expect(screen.getByText(/E-posta veya şifre hatalı/)).toBeTruthy();
  });

  it('Kayıt Ol linkine tıklandığında Register ekranına yönlendirmelidir', () => {
    render(
      <NavigationContainer>
        <LoginScreen navigation={mockNavigation as any} />
      </NavigationContainer>
    );

    const registerLink = screen.getByText(/Kayıt Olun/);
    fireEvent.press(registerLink);

    expect(mockNavigation.navigate).toHaveBeenCalledWith('Register');
  });
});
