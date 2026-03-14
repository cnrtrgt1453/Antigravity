import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { MarketInstrument } from '../../domain/entities/MarketInstrument';

interface Props {
  instrument: MarketInstrument;
}

export const MarketTrendCard: React.FC<Props> = ({ instrument }) => {
  const { name, symbol, currentPrice, isUpwardTrend } = instrument;

  return (
    <View style={styles.card}>
      <View style={styles.leftSection}>
        <Text style={styles.name}>{name}</Text>
        <Text style={styles.symbol}>{symbol}</Text>
      </View>
      <View style={styles.rightSection}>
        <Text style={styles.price}>{currentPrice.toFixed(2)} ₺</Text>
        <View style={[styles.trendBadge, isUpwardTrend ? styles.trendUp : styles.trendDown]}>
          <Text style={styles.trendIcon}>{isUpwardTrend ? '▲' : '▼'}</Text>
        </View>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  card: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: '#ffffff',
    padding: 16,
    marginVertical: 8,
    borderRadius: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  leftSection: {
    flexDirection: 'column',
  },
  name: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333333',
  },
  symbol: {
    fontSize: 12,
    color: '#888888',
    marginTop: 4,
  },
  rightSection: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  price: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333333',
    marginRight: 12,
  },
  trendBadge: {
    width: 28,
    height: 28,
    borderRadius: 14,
    justifyContent: 'center',
    alignItems: 'center',
  },
  trendUp: {
    backgroundColor: '#e6f4ea',
  },
  trendDown: {
    backgroundColor: '#fce8e6',
  },
  trendIcon: {
    fontSize: 12,
    color: '#fff',
  },
});
