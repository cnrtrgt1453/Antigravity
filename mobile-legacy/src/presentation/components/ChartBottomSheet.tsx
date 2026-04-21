import React, { useEffect, useState } from 'react';
import { 
  View, 
  Text, 
  StyleSheet, 
  Modal, 
  TouchableOpacity, 
  Dimensions,
  TouchableWithoutFeedback
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { StockChart } from './StockChart';
import { Config } from '../../config';

interface ChartBottomSheetProps {
  isVisible: boolean;
  onClose: () => void;
  symbol: string | null;
  name: string | null;
}

export const ChartBottomSheet: React.FC<ChartBottomSheetProps> = ({ 
  isVisible, 
  onClose, 
  symbol,
  name 
}) => {
  const [data, setData] = useState<any>({});
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isVisible && symbol) {
      fetchChartData();
    }
  }, [isVisible, symbol]);

  const fetchChartData = async () => {
    setLoading(true);
    try {
      // Python API'den OHLC verisi çekiyoruz
      const response = await fetch(`${Config.PYTHON_API_URL}/api/v1/analysis/ohlc?ticker=${symbol}&period=1mo&interval=1d`);
      const ohlc = await response.json();
      setData(ohlc);
    } catch (e) {
      console.error("Chart data fetch failed", e);
    } finally {
      setLoading(false);
    }
  };

  if (!symbol) return null;

  return (
    <Modal
      visible={isVisible}
      transparent
      animationType="slide"
      onRequestClose={onClose}
    >
      <TouchableWithoutFeedback onPress={onClose}>
        <View style={styles.overlay} />
      </TouchableWithoutFeedback>
      
      <View style={styles.content}>
        <View style={styles.header}>
          <View>
            <Text style={styles.symbol}>{symbol}</Text>
            <Text style={styles.name}>{name}</Text>
          </View>
          <TouchableOpacity 
            onPress={onClose} 
            style={styles.closeButton}
            testID="ChartBottomSheet:CloseButton"
          >
            <Ionicons name="close" size={24} color="#FFFFFF" />
          </TouchableOpacity>
        </View>
        
        <View style={styles.chartContainer}>
          <StockChart 
            ohlc={data.ohlc || []} 
            sma50={data.sma50 || []}
            sma200={data.sma200 || []}
            markers={data.markers || []}
            isLoading={loading} 
          />
        </View>
        
        <View style={styles.footer}>
          <Text style={styles.footerText}>Son 1 Aylık Performans (Günlük)</Text>
        </View>
      </View>
    </Modal>
  );
};

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
  },
  content: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    backgroundColor: '#161B22',
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    paddingTop: 20,
    paddingBottom: 40,
    minHeight: 450,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    marginBottom: 20,
  },
  symbol: {
    fontSize: 20,
    fontWeight: '800',
    color: '#FFFFFF',
  },
  name: {
    fontSize: 14,
    color: '#8B949E',
  },
  closeButton: {
    backgroundColor: '#30363D',
    borderRadius: 20,
    width: 36,
    height: 36,
    justifyContent: 'center',
    alignItems: 'center',
  },
  chartContainer: {
    height: 300,
    width: '100%',
  },
  footer: {
    marginTop: 10,
    alignItems: 'center',
  },
  footerText: {
    color: '#8B949E',
    fontSize: 12,
  },
});
