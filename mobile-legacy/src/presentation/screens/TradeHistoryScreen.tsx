import React, { useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  ActivityIndicator,
  RefreshControl
} from 'react-native';
import { useGameStore } from '../stores/useGameStore';
import { Ionicons } from '@expo/vector-icons';

export const TradeHistoryScreen: React.FC = () => {
  const { history, isLoading, fetchHistory } = useGameStore();

  useEffect(() => {
    fetchHistory();
  }, []);

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleString('tr-TR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const renderItem = ({ item }: { item: any }) => {
    const isBuy = item.type === 'BUY';
    return (
      <View style={styles.card}>
        <View style={styles.cardHeader}>
          <View style={[styles.typeBadge, { backgroundColor: isBuy ? '#23863622' : '#DA363322' }]}>
            <Text style={[styles.typeText, { color: isBuy ? '#3FB950' : '#F85149' }]}>
              {isBuy ? 'ALIM' : 'SATIM'}
            </Text>
          </View>
          <Text style={styles.date}>{formatDate(item.timestamp)}</Text>
        </View>
        
        <View style={styles.mainRow}>
          <Text style={styles.symbol}>{item.stockSymbol}</Text>
          <Text style={styles.total}>{item.totalAmount.toFixed(2)} TL</Text>
        </View>

        <View style={styles.detailsRow}>
          <View style={styles.detail}>
            <Text style={styles.label}>Miktar</Text>
            <Text style={styles.value}>{item.quantity} Adet</Text>
          </View>
          <View style={styles.detail}>
            <Text style={styles.label}>Birim Fiyat</Text>
            <Text style={styles.value}>{item.price.toFixed(2)} TL</Text>
          </View>
          <View style={styles.detail}>
            <Text style={styles.label}>Komisyon</Text>
            <Text style={styles.value}>{item.commission.toFixed(2)} TL</Text>
          </View>
        </View>
      </View>
    );
  };

  if (isLoading && history.length === 0) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" color="#F6C90E" testID="loading-indicator" />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>İşlem Geçmişi</Text>
      </View>
      <FlatList
        testID="trade-history-list"
        data={history}
        renderItem={renderItem}
        keyExtractor={(item) => item.id.toString()}
        contentContainerStyle={styles.list}
        refreshControl={<RefreshControl testID="refresh-control" refreshing={isLoading} onRefresh={fetchHistory} tintColor="#F6C90E" />}
        ListEmptyComponent={
          <View style={styles.emptyContainer}>
            <Ionicons name="receipt-outline" size={64} color="#30363D" />
            <Text style={styles.emptyText}>Henüz bir işlem kaydı bulunmuyor.</Text>
          </View>
        }
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#0D1117' },
  header: { paddingHorizontal: 20, paddingTop: 60, marginBottom: 20 },
  title: { fontSize: 24, fontWeight: '800', color: '#FFFFFF' },
  list: { paddingHorizontal: 20, paddingBottom: 40 },
  center: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: '#0D1117' },
  
  card: {
    backgroundColor: '#161B22',
    borderRadius: 16,
    padding: 16,
    marginBottom: 16,
    borderWidth: 1,
    borderColor: '#30363D',
  },
  cardHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 },
  typeBadge: { paddingHorizontal: 8, paddingVertical: 4, borderRadius: 6 },
  typeText: { fontSize: 11, fontWeight: '800' },
  date: { color: '#8B949E', fontSize: 12 },
  
  mainRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 },
  symbol: { color: '#FFFFFF', fontSize: 20, fontWeight: '800' },
  total: { color: '#FFFFFF', fontSize: 18, fontWeight: '700' },
  
  detailsRow: { flexDirection: 'row', justifyContent: 'space-between', borderTopWidth: 1, borderTopColor: '#30363D', paddingTop: 12 },
  detail: { alignItems: 'center' },
  label: { color: '#8B949E', fontSize: 11, marginBottom: 4 },
  value: { color: '#FFFFFF', fontSize: 13, fontWeight: '600' },

  emptyContainer: { flex: 1, alignItems: 'center', justifyContent: 'center', marginTop: 100 },
  emptyText: { color: '#8B949E', fontSize: 16, marginTop: 20, textAlign: 'center' },
});
