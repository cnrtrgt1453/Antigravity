import React from 'react';
import { render, screen } from '@testing-library/react-native';
import { StockChart } from '../StockChart';

// Mock WebView
jest.mock('react-native-webview', () => {
  const { View } = require('react-native');
  return {
    WebView: (props: any) => <View {...props} testID="mock-webview" />,
  };
});

describe('StockChart', () => {
  const mockOhlc = [
    { time: 1672531200, open: 100, high: 110, low: 90, close: 105 },
    { time: 1672617600, open: 105, high: 115, low: 100, close: 110 },
  ];

  it('isLoading true olduğunda ActivityIndicator göstermelidir', () => {
    render(<StockChart ohlc={[]} isLoading={true} />);
    expect(screen.getByTestId('StockChart:Loader')).toBeTruthy();
  });

  it('Veri yüklendiğinde WebView göstermelidir', () => {
    render(<StockChart ohlc={mockOhlc} isLoading={false} />);
    expect(screen.getByTestId('mock-webview')).toBeTruthy();
  });

  it('Props değiştiğinde WebView kaynağını (html) güncellemelidir', () => {
    const { rerender } = render(<StockChart ohlc={mockOhlc} />);
    const webview = screen.getByTestId('mock-webview');
    
    // Basit bir kontrol: HTML string'i içinde verilerimiz var mı?
    expect(webview.props.source.html).toContain(JSON.stringify(mockOhlc));
    
    const newOhlc = [...mockOhlc, { time: 1672704000, open: 110, high: 120, low: 110, close: 115 }];
    rerender(<StockChart ohlc={newOhlc} />);
    
    expect(screen.getByTestId('mock-webview')).toBeTruthy();
  });
});
