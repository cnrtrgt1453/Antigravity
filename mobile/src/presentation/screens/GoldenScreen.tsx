import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, FlatList, ActivityIndicator, RefreshControl } from 'react-native';
import { Config } from '../../config';

interface SignalData {
  ticker: string;
  signal: string;
  current_price: number;
  sma50: number;
  sma200: number;
  cross_price: number | null;
  cross_date: string;
  message: string;
}

export const GoldenScreen: React.FC = () => {
  const [data, setData] = useState<SignalData[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const fetchData = async () => {
    try {
      const response = await fetch(`${Config.PYTHON_API_URL}/api/v1/analysis/latest_signals`);
      const json = await response.json();
      setData(json.golden_signals || []);
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

  const renderItem = ({ item }: { item: SignalData }) => {
    const diff = item.cross_price ? (item.current_price - item.cross_price).toFixed(2) : '-';
    
    return (
      <View style={styles.card}>
        <View style={styles.cardHeader}>
          <Text style={styles.ticker}>{item.ticker}</Text>
          <Text style={styles.date}>{item.cross_date}</Text>
        </View>
        
        <Text style={styles.message}>{item.message}</Text>
        
        <View style={styles.infoRow}>
          <View>
            <Text style={styles.label}>Mevcut Fiyat</Text>
            <Text style={styles.value}>{item.current_price?.toFixed(2)}</Text>
          </View>
          <View>
            <Text style={styles.label}>Kesişim Fiyatı</Text>
            <Text style={styles.value}>{item.cross_price?.toFixed(2)}</Text>
          </View>
          <View>
            <Text style={styles.label}>Değişim</Text>
            <Text style={[styles.value, { color: '#3FB950' }]}>+{diff}</Text>
          </View>
        </View>
      </View>
    );
  };

  if (loading) {
    return <ActivityIndicator style={styles.loader} color="#F6C90E" />;
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Golden Cross (7 Gün)</Text>
      <FlatList
        data={data}
        renderItem={renderItem}
        keyExtractor={(item) => item.ticker}
        contentContainerStyle={styles.list}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor="#F6C90E" />}
        ListEmptyComponent={<Text style={styles.emptyText}>Son 7 günde Golden Cross sinyali bulunamadı.</Text>}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#0D1117', paddingHorizontal: 16, paddingTop: 60 },
  title: { fontSize: 24, fontWeight: '800', color: '#F6C90E', marginBottom: 20 },
  list: { paddingBottom: 20 },
  loader: { flex: 1, backgroundColor: '#0D1117' },
  card: {
    backgroundColor: '#161B22',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    borderWidth: 1,
    borderColor: '#23863655',
  },
  cardHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 },
  ticker: { fontSize: 18, fontWeight: '700', color: '#FFFFFF' },
  date: { color: '#8B949E', fontSize: 13 },
  message: { color: '#C9D1D9', fontSize: 14, marginBottom: 12, lineHeight: 20 },
  infoRow: { flexDirection: 'row', justifyContent: 'space-between', borderTopWidth: 1, borderTopColor: '#30363D', paddingTop: 12 },
  label: { color: '#8B949E', fontSize: 11, marginBottom: 4 },
  value: { color: '#FFFFFF', fontSize: 14, fontWeight: '700' },
  emptyText: { color: '#8B949E', textAlign: 'center', marginTop: 40 },
});
