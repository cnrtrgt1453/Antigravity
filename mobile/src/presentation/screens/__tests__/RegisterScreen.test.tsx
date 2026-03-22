import { jest, describe, it, expect, beforeEach } from '@jest/globals';
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react-native';
import { RegisterScreen } from '../RegisterScreen';
import { useAuthStore } from '../../stores/useAuthStore';
import { NavigationContainer } from '@react-navigation/native';

// Mocking useAuthStore
jest.mock('../../stores/useAuthStore', () => ({
  useAuthStore: jest.fn(),
}));

describe('RegisterScreen', () => {
  const mockRegister = jest.fn();
  const mockClearError = jest.fn();
  const mockNavigation = {
    navigate: jest.fn(),
    goBack: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
    (useAuthStore as any).mockReturnValue({
      register: mockRegister,
      isLoading: false,
      error: null,
      clearError: mockClearError,
    });
  });

  it('arayüz elemanları doğru görünmelidir', () => {
    render(
      <NavigationContainer>
        <RegisterScreen navigation={mockNavigation as any} />
      </NavigationContainer>
    );

    expect(screen.getByTestId('register-fullname-input')).toBeTruthy();
    expect(screen.getByTestId('register-email-input')).toBeTruthy();
    expect(screen.getByTestId('register-password-input')).toBeTruthy();
    expect(screen.getByTestId('register-submit-button')).toBeTruthy();
  });

  it('boş alanlar için validasyon hatası vermelidir', async () => {
    render(
      <NavigationContainer>
        <RegisterScreen navigation={mockNavigation as any} />
      </NavigationContainer>
    );

    const submitButton = screen.getByTestId('register-submit-button');
    fireEvent.press(submitButton);

    await waitFor(() => {
      expect(screen.getByTestId('register-error-box')).toBeTruthy();
      expect(screen.getByText(/Lütfen tüm alanları doldurun/)).toBeTruthy();
    });
  });

  it('geçerli bilgilerle kayıt fonksiyonunu tetiklemelidir', async () => {
    render(
      <NavigationContainer>
        <RegisterScreen navigation={mockNavigation as any} />
      </NavigationContainer>
    );

    fireEvent.changeText(screen.getByTestId('register-fullname-input'), 'Caner O.');
    fireEvent.changeText(screen.getByTestId('register-email-input'), 'caner@example.com');
    fireEvent.changeText(screen.getByTestId('register-password-input'), '123456');

    const submitButton = screen.getByTestId('register-submit-button');
    fireEvent.press(submitButton);

    await waitFor(() => {
      expect(mockRegister).toHaveBeenCalledWith('Caner O.', 'caner@example.com', '123456');
    });
  });

  it('yukleme durumunda butonu pasif hale getirmelidir', () => {
    (useAuthStore as any).mockReturnValue({
      register: mockRegister,
      isLoading: true,
      error: null,
      clearError: mockClearError,
    });

    render(
      <NavigationContainer>
        <RegisterScreen navigation={mockNavigation as any} />
      </NavigationContainer>
    );

    const submitButton = screen.getByTestId('register-submit-button');
    expect(submitButton.props.accessibilityState.disabled).toBe(true);
  });

  it('Geri butonuna basıldıgında goBack cagırılmalıdır', () => {
    render(
      <NavigationContainer>
        <RegisterScreen navigation={mockNavigation as any} />
      </NavigationContainer>
    );

    const backButton = screen.getByText('←');
    fireEvent.press(backButton);

    expect(mockNavigation.goBack).toHaveBeenCalled();
  });
});
