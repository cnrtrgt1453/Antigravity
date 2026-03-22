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

// Mocking GoogleSignin
jest.mock('@react-native-google-signin/google-signin', () => {
  return {
    GoogleSignin: {
      hasPlayServices: jest.fn(() => Promise.resolve(true)),
      signIn: jest.fn(() => Promise.resolve({ data: { idToken: 'test-token' } })),
      configure: jest.fn(),
    },
    statusCodes: {
      SIGN_IN_CANCELLED: 'SIGN_IN_CANCELLED',
      IN_PROGRESS: 'IN_PROGRESS',
      PLAY_SERVICES_NOT_AVAILABLE: 'PLAY_SERVICES_NOT_AVAILABLE',
    },
  };
});

// Mocking react-native-fbsdk-next
jest.mock('react-native-fbsdk-next', () => {
  return {
    LoginManager: {
      logInWithPermissions: jest.fn(() => Promise.resolve({ isCancelled: false })),
    },
    AccessToken: {
      getCurrentAccessToken: jest.fn(() => Promise.resolve({ accessToken: 'fb-test-token' })),
    },
  };
}, { virtual: true });

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
    loginWithSocial: jest.fn(),
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

  it('Google ile Giriş butonuna tıklandığında loginWithSocial (GOOGLE) fonksiyonunu çağırmalıdır', async () => {
    render(
      <NavigationContainer>
        <LoginScreen navigation={mockNavigation as any} />
      </NavigationContainer>
    );

    const googleButton = screen.getByText('Google ile Devam Et');
    fireEvent.press(googleButton);

    await waitFor(() => {
      expect(mockAuthState.loginWithSocial).toHaveBeenCalledWith('test-token', 'GOOGLE');
    });
  });

  it('Facebook ile Giriş butonuna tıklandığında loginWithSocial (FACEBOOK) fonksiyonunu çağırmalıdır', async () => {
    render(
      <NavigationContainer>
        <LoginScreen navigation={mockNavigation as any} />
      </NavigationContainer>
    );

    const facebookButton = screen.getByText('Facebook ile Devam Et');
    fireEvent.press(facebookButton);

    await waitFor(() => {
      expect(mockAuthState.loginWithSocial).toHaveBeenCalledWith('fb-test-token', 'FACEBOOK');
    });
  });
});
