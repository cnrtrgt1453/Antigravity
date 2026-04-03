import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react-native';
import { ChartBottomSheet } from '../ChartBottomSheet';

// Mock StockChart
jest.mock('../StockChart', () => {
  const { View } = require('react-native');
  return {
    StockChart: () => <View testID="mock-stock-chart" />,
  };
});

// Mock Ionicons
jest.mock('@expo/vector-icons', () => ({
  Ionicons: 'Ionicons',
}));

// Mock fetch
global.fetch = jest.fn(() =>
  Promise.resolve({
    json: () => Promise.resolve({ ohlc: [], sma50: [], sma200: [], markers: [] }),
  })
) as jest.Mock;

describe('ChartBottomSheet', () => {
  const mockProps = {
    isVisible: true,
    onClose: jest.fn(),
    symbol: 'THYAO.IS',
    name: 'Turkish Airlines',
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('isVisible false iken hiçbir şey render etmemelidir', () => {
    render(<ChartBottomSheet {...mockProps} isVisible={false} />);
    expect(screen.queryByText('THYAO.IS')).toBeNull();
  });

  it('isVisible true iken başlık ve grafiği göstermelidir', () => {
    render(<ChartBottomSheet {...mockProps} />);
    expect(screen.getByText('THYAO.IS')).toBeTruthy();
    expect(screen.getByText('Turkish Airlines')).toBeTruthy();
    expect(screen.getByTestId('mock-stock-chart')).toBeTruthy();
  });

  it('Açıldığında veri çekme isteği atmalıdır', async () => {
    render(<ChartBottomSheet {...mockProps} />);
    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith(expect.stringContaining('THYAO.IS'));
    });
  });

  it('Kapatma butonuna basıldığında onClose fonksiyonunu çağırmalıdır', () => {
    render(<ChartBottomSheet {...mockProps} />);
    const closeButton = screen.getByTestId('ChartBottomSheet:CloseButton');
    fireEvent.press(closeButton);
    expect(mockProps.onClose).toHaveBeenCalled();
  });
});
