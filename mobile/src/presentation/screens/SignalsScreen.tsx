import React, { useEffect, useState } from 'react';
import { 
  View, 
  Text, 
  StyleSheet, 
  FlatList, 
  ActivityIndicator, 
  RefreshControl, 
  TouchableOpacity,
  ScrollView
} from 'react-native';
import { Config } from '../../config';
import { Ionicons } from '@expo/vector-icons';

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

type FilterType = 'ALL' | 'GOLDEN' | 'DEAD';

export const SignalsScreen: React.FC = () => {
  const [allSignals, setAllSignals] = useState<SignalData[]>([]);
  const [filteredSignals, setFilteredSignals] = useState<SignalData[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [activeFilter, setActiveFilter] = useState<FilterType>('ALL');

  const fetchData = async () => {
    try {
      const response = await fetch(`${Config.PYTHON_API_URL}/api/v1/analysis/latest_signals`);
      const json = await response.json();
      
      const golden = (json.golden_signals || []).map((s: any) => ({ ...s, signalType: 'GOLDEN' }));
      const dead = (json.dead_signals || []).map((s: any) => ({ ...s, signalType: 'DEAD' }));
      
      const combined = [...golden, ...dead].sort((a, b) => 
        new Date(b.cross_date).getTime() - new Date(a.cross_date).getTime()
      );
      
      setAllSignals(combined);
      applyFilter(activeFilter, combined);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const applyFilter = (filter: FilterType, signals: any[]) => {
    if (filter === 'ALL') {
      setFilteredSignals(signals);
    } else if (filter === 'GOLDEN') {
      setFilteredSignals(signals.filter(s => s.signalType === 'GOLDEN'));
    } else if (filter === 'DEAD') {
      setFilteredSignals(signals.filter(s => s.signalType === 'DEAD'));
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  useEffect(() => {
    applyFilter(activeFilter, allSignals);
  }, [activeFilter, allSignals]);

  const onRefresh = () => {
    setRefreshing(true);
    fetchData();
  };

  const renderFilterChip = (label: string, type: FilterType) => (
    <TouchableOpacity 
      style={[
        styles.chip, 
        activeFilter === type && styles.activeChip,
        activeFilter === type && type === 'GOLDEN' && styles.activeGoldenChip,
        activeFilter === type && type === 'DEAD' && styles.activeDeadChip
      ]} 
      onPress={() => setActiveFilter(type)}
      testID={`chip-${type}`}
    >
      <Text style={[styles.chipText, activeFilter === type && styles.activeChipText]}>
        {label}
      </Text>
    </TouchableOpacity>
  );

  const renderItem = ({ item }: { item: any }) => {
    const isGolden = item.signalType === 'GOLDEN';
    const diff = item.cross_price ? (item.current_price - item.cross_price).toFixed(2) : '-';
    const diffColor = isGolden ? '#3FB950' : '#F85149';
    
    return (
      <View style={[styles.card, { borderColor: isGolden ? '#23863633' : '#DA363333' }]}>
        <View style={styles.cardHeader}>
          <View style={styles.tickerBadge}>
            <Text style={styles.ticker}>{item.ticker}</Text>
            <View style={[styles.signalIndicator, { backgroundColor: isGolden ? '#3FB950' : '#F85149' }]}>
               <Text style={styles.signalIndicatorText}>{isGolden ? 'GOLDEN' : 'DEAD'}</Text>
            </View>
          </View>
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
            <Text style={[styles.value, { color: diffColor }]}>
              {parseFloat(diff) > 0 ? '+' : ''}{diff}
            </Text>
          </View>
        </View>
      </View>
    );
  };

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#F6C90E" testID="loading-indicator" />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Sinyaller</Text>
        <Text style={styles.subtitle}>Teknik analiz al-sat sinyalleri</Text>
      </View>

      <View style={styles.filterSection}>
        <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.filterScroll}>
          {renderFilterChip('Hepsi', 'ALL')}
          {renderFilterChip('Golden Cross', 'GOLDEN')}
          {renderFilterChip('Dead Cross', 'DEAD')}
        </ScrollView>
      </View>

      <FlatList
        testID="signals-list"
        data={filteredSignals}
        renderItem={renderItem}
        keyExtractor={(item, index) => `${item.ticker}-${index}`}
        contentContainerStyle={styles.list}
        refreshControl={
          <RefreshControl 
            testID="refresh-control"
            refreshing={refreshing} 
            onRefresh={onRefresh} 
            tintColor="#F6C90E" 
            colors={['#F6C90E']}
          />
        }
        ListEmptyComponent={
          <View style={styles.emptyContainer}>
            <Ionicons name="filter-outline" size={48} color="#30363D" />
            <Text style={styles.emptyText}>Bu filtreye uygun sinyal bulunamadı.</Text>
          </View>
        }
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#0D1117' },
  loadingContainer: { flex: 1, backgroundColor: '#0D1117', justifyContent: 'center', alignItems: 'center' },
  header: { paddingTop: 60, paddingHorizontal: 20, marginBottom: 15 },
  title: { fontSize: 28, fontWeight: '800', color: '#FFFFFF' },
  subtitle: { fontSize: 14, color: '#8B949E', marginTop: 4 },
  
  filterSection: { marginBottom: 15 },
  filterScroll: { paddingHorizontal: 20 },
  chip: {
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
    backgroundColor: '#161B22',
    marginRight: 10,
    borderWidth: 1,
    borderColor: '#30363D',
  },
  activeChip: {
    backgroundColor: '#F6C90E',
    borderColor: '#F6C90E',
  },
  activeGoldenChip: {
    backgroundColor: '#238636',
    borderColor: '#238636',
  },
  activeDeadChip: {
    backgroundColor: '#DA3633',
    borderColor: '#DA3633',
  },
  chipText: { color: '#8B949E', fontWeight: '600', fontSize: 13 },
  activeChipText: { color: '#FFFFFF' },

  list: { paddingHorizontal: 16, paddingBottom: 30 },
  card: {
    backgroundColor: '#161B22',
    borderRadius: 16,
    padding: 16,
    marginBottom: 16,
    borderWidth: 1,
    shadowColor: '#000',
    shadowOpacity: 0.2,
    shadowRadius: 10,
    elevation: 4,
  },
  cardHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 12 },
  tickerBadge: { flexDirection: 'row', alignItems: 'center' },
  ticker: { fontSize: 20, fontWeight: '800', color: '#FFFFFF', marginRight: 10 },
  signalIndicator: {
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 6,
  },
  signalIndicatorText: {
    color: '#FFFFFF',
    fontSize: 10,
    fontWeight: '900',
  },
  date: { color: '#8B949E', fontSize: 12, fontWeight: '500' },
  message: { color: '#C9D1D9', fontSize: 14, marginBottom: 16, lineHeight: 22 },
  infoRow: { 
    flexDirection: 'row', 
    justifyContent: 'space-between', 
    borderTopWidth: 1, 
    borderTopColor: '#30363D88', 
    paddingTop: 12 
  },
  label: { color: '#8B949E', fontSize: 11, marginBottom: 4, fontWeight: '500' },
  value: { color: '#FFFFFF', fontSize: 15, fontWeight: '700' },
  
  emptyContainer: { alignItems: 'center', marginTop: 60 },
  emptyText: { color: '#8B949E', textAlign: 'center', marginTop: 12, fontSize: 15 },
});
