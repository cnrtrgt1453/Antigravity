import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, FlatList, ActivityIndicator, RefreshControl } from 'react-native';
import { Config } from '../../config';

interface MarketData {
  ticker: string;
  signal: string;
  current_price: number;
  sma50: number;
  sma200: number;
  cross_price: number | null;
  message: string;
}

export const MarketScreen: React.FC = () => {
  const [data, setData] = useState<MarketData[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const fetchData = async () => {
    try {
      const response = await fetch(`${Config.PYTHON_API_URL}/api/v1/analysis/all_market_data`);
      const json = await response.json();
      setData(json);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const onRefresh = () => {
    setRefreshing(true);
    fetchData();
  };

  const renderItem = ({ item }: { item: MarketData }) => {
    const isUptrend = item.sma50 > item.sma200;
    const diff = item.cross_price ? (item.current_price - item.cross_price).toFixed(2) : '-';
    const diffColor = item.cross_price ? (item.current_price >= item.cross_price ? '#3FB950' : '#F85149') : '#8B949E';

    return (
      <View style={[styles.card, { borderColor: isUptrend ? '#238636' : '#DA3633' }]}>
        <View style={styles.cardHeader}>
          <Text style={styles.ticker}>{item.ticker}</Text>
          <View style={[styles.badge, { backgroundColor: isUptrend ? '#238636' : '#DA3633' }]}>
            <Text style={styles.badgeText}>{isUptrend ? 'Yükseliş' : 'Düşüş'}</Text>
          </View>
        </View>
        
        <View style={styles.infoRow}>
          <View>
            <Text style={styles.label}>Fiyat</Text>
            <Text style={styles.value}>{item.current_price?.toFixed(2)}</Text>
          </View>
          <View>
            <Text style={styles.label}>SMA50</Text>
            <Text style={styles.value}>{item.sma50?.toFixed(2)}</Text>
          </View>
          <View>
            <Text style={styles.label}>SMA200</Text>
            <Text style={styles.value}>{item.sma200?.toFixed(2)}</Text>
          </View>
        </View>

        <View style={styles.diffContainer}>
          <Text style={styles.label}>Kesişimden Beri Fark:</Text>
          <Text style={[styles.diffValue, { color: diffColor }]}>
            {item.cross_price ? (parseFloat(diff) > 0 ? `+${diff}` : diff) : '-'}
          </Text>
        </View>
      </View>
    );
  };

  if (loading) {
    return <ActivityIndicator style={styles.loader} color="#58A6FF" />;
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Piyasalar</Text>
      <FlatList
        data={data}
        renderItem={renderItem}
        keyExtractor={(item) => item.ticker}
        contentContainerStyle={styles.list}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor="#58A6FF" />}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#0D1117', paddingHorizontal: 16, paddingTop: 60 },
  title: { fontSize: 28, fontWeight: '800', color: '#FFFFFF', marginBottom: 20 },
  list: { paddingBottom: 20 },
  loader: { flex: 1, backgroundColor: '#0D1117' },
  card: {
    backgroundColor: '#161B22',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    borderWidth: 1,
  },
  cardHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 },
  ticker: { fontSize: 18, fontWeight: '700', color: '#FFFFFF' },
  badge: { paddingHorizontal: 8, paddingVertical: 4, borderRadius: 6 },
  badgeText: { color: '#FFFFFF', fontSize: 12, fontWeight: '700' },
  infoRow: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 12 },
  label: { color: '#8B949E', fontSize: 12, marginBottom: 4 },
  value: { color: '#FFFFFF', fontSize: 15, fontWeight: '600' },
  diffContainer: { borderTopWidth: 1, borderTopColor: '#30363D', paddingTop: 10, flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  diffValue: { fontSize: 16, fontWeight: '700' },
});
