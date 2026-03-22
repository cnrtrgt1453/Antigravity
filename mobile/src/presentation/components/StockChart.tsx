import React, { useMemo } from 'react';
import { View, StyleSheet, ActivityIndicator } from 'react-native';
import { WebView } from 'react-native-webview';

interface OHLCData {
  time: number;
  open: number;
  high: number;
  low: number;
  close: number;
}

interface LineData {
  time: number;
  value: number;
}

interface MarkerData {
  time: number;
  position: 'aboveBar' | 'belowBar' | 'inBar';
  color: string;
  shape: 'circle' | 'square' | 'arrowUp' | 'arrowDown';
  text: string;
}

interface StockChartProps {
  ohlc: OHLCData[];
  sma50?: LineData[];
  sma200?: LineData[];
  markers?: MarkerData[];
  isLoading?: boolean;
}

export const StockChart: React.FC<StockChartProps> = ({ 
  ohlc, 
  sma50 = [], 
  sma200 = [], 
  markers = [], 
  isLoading 
}) => {
  const chartHtml = useMemo(() => {
    const jsonOhlc = JSON.stringify(ohlc);
    const jsonSma50 = JSON.stringify(sma50);
    const jsonSma200 = JSON.stringify(sma200);
    const jsonMarkers = JSON.stringify(markers);
    
    return `
      <!DOCTYPE html>
      <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
          <script src="https://unpkg.com/lightweight-charts/dist/lightweight-charts.standalone.production.js"></script>
          <style>
            body { margin: 0; padding: 0; background-color: #0D1117; overflow: hidden; }
            #chart { width: 100vw; height: 100vh; }
          </style>
        </head>
        <body>
          <div id="chart"></div>
          <script>
            const chartOptions = { 
              layout: { 
                background: { color: '#0D1117' }, 
                textColor: '#D1D4DC',
              },
              grid: {
                vertLines: { color: '#1f2937' },
                horzLines: { color: '#1f2937' },
              },
              crosshair: {
                mode: LightweightCharts.CrosshairMode.Normal,
              },
              rightPriceScale: {
                borderColor: '#1f2937',
              },
              timeScale: {
                borderColor: '#1f2937',
                timeVisible: true,
                secondsVisible: false,
              },
            };
            
            const chart = LightweightCharts.createChart(document.getElementById('chart'), chartOptions);
            const candlestickSeries = chart.addCandlestickSeries({
              upColor: '#26a69a', downColor: '#ef5350', borderVisible: false,
              wickUpColor: '#26a69a', wickDownColor: '#ef5350',
            });
            
            // SMA50 Series (Green)
            const sma50Series = chart.addLineSeries({
              color: '#4ade80',
              lineWidth: 2,
              title: 'SMA50',
            });
            
            // SMA200 Series (Red)
            const sma200Series = chart.addLineSeries({
              color: '#ef4444',
              lineWidth: 2,
              title: 'SMA200',
            });
            
            candlestickSeries.setData(${jsonOhlc});
            sma50Series.setData(${jsonSma50});
            sma200Series.setData(${jsonSma200});
            
            candlestickSeries.setMarkers(${jsonMarkers});
            
            chart.timeScale().fitContent();
            
            window.onresize = () => {
              chart.applyOptions({ width: window.innerWidth, height: window.innerHeight });
            };
          </script>
        </body>
      </html>
    `;
  }, [ohlc, sma50, sma200, markers]);

  if (isLoading) {
    return (
      <View style={styles.loaderContainer}>
        <ActivityIndicator size="large" color="#F6C90E" testID="StockChart:Loader" />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <WebView
        originWhitelist={['*']}
        source={{ html: chartHtml }}
        style={styles.webview}
        scrollEnabled={false}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    height: 300,
    backgroundColor: '#0D1117',
  },
  webview: {
    flex: 1,
    backgroundColor: 'transparent',
  },
  loaderContainer: {
    height: 300,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#0D1117',
  },
});
