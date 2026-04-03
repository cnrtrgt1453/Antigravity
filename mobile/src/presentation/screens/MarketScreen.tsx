import React, { useEffect, useState } from 'react';
import { 
  View, 
  Text, 
  StyleSheet, 
  FlatList, 
  ActivityIndicator, 
  RefreshControl, 
  TouchableOpacity, 
  Alert,
  ScrollView,
} from 'react-native';
import { Config } from '../../config';
import { useGameStore } from '../stores/useGameStore';
import { Ionicons } from '@expo/vector-icons';
import { ChartBottomSheet } from '../components/ChartBottomSheet';
import { MarketCardSkeleton } from '../components/CardSkeleton';
import { StatusMessage } from '../components/StatusMessage';

interface MarketData {
  symbol: string;
  name: string;
  category: string;
  // Analysis fields (from Python)
  signal?: string;
  current_price?: number;
  sma50?: number;
  sma200?: number;
  cross_price?: number | null;
}

export const MarketScreen: React.FC = () => {
  const [data, setData] = useState<MarketData[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [analysisCache, setAnalysisCache] = useState<any[]>([]);
  
  // Chart Bottom Sheet State
  const [isChartVisible, setIsChartVisible] = useState(false);
  const [selectedStock, setSelectedStock] = useState<{symbol: string, name: string} | null>(null);
  
  const { watchlist, addToWatchlist, removeFromWatchlist, fetchWatchlist } = useGameStore();

  useEffect(() => {
    fetchWatchlist();
  }, []);

  const fetchAnalysisData = async () => {
    try {
      const response = await fetch(`${Config.PYTHON_API_URL}/api/v1/analysis/all_market_data`);
      const analysis = await response.json();
      setAnalysisCache(analysis);
      return analysis;
    } catch (e) {
      console.error("Python analysis fetch failed", e);
      return analysisCache;
    }
  };

  const fetchData = async (reset = false) => {
    if (!reset && (loadingMore || !hasMore)) return;

    const currentPage = reset ? 0 : page;
    if (reset) {
      setLoading(true);
    } else {
      setLoadingMore(true);
    }

    try {
      // 1. Get or Refresh Analysis Cache (Python)
      let currentAnalysis = analysisCache;
      if (reset || analysisCache.length === 0) {
        currentAnalysis = await fetchAnalysisData();
      }

      // 2. Fetch paginated stocks from Java
      const stocksResponse = await fetch(`${Config.JAVA_API_URL}/api/v1/stocks?page=${currentPage}&size=20`);
      const stocksPage = await stocksResponse.json();
      const stocks = stocksPage.content;

      // 3. Map with analysis
      const mappedData = stocks.map((s: any) => {
        const analysis = currentAnalysis.find((a: any) => a.ticker === s.symbol);
        return {
          symbol: s.symbol,
          name: s.name,
          category: s.category,
          ...analysis
        };
      });

      if (reset) {
        setData(mappedData);
        setPage(1);
      } else {
        setData(prev => [...prev, ...mappedData]);
        setPage(currentPage + 1);
      }

      setHasMore(!stocksPage.last);
    } catch (e) {
      console.error(e);
    } finally {
      // For UX: Keep skeleton visible for at least 600ms if it was a reset
      if (reset) {
        setTimeout(() => setLoading(false), 600);
      } else {
        setLoadingMore(false);
      }
      setRefreshing(false);
    }
  };

  useEffect(() => {
    fetchData(true);
  }, []);

  const onRefresh = () => {
    setRefreshing(true);
    fetchData(true);
  };

  const toggleWatch = async (symbol: string) => {
    const isWatched = watchlist.includes(symbol);
    try {
      if (isWatched) {
        await removeFromWatchlist(symbol);
      } else {
        await addToWatchlist(symbol);
      }
    } catch (e) {
      Alert.alert("Hata", "İşlem gerçekleştirilemedi.");
    }
  };

  const handleLoadMore = () => {
    if (!loading && !loadingMore && hasMore) {
      fetchData();
    }
  };

  const handleOpenChart = (symbol: string, name: string) => {
    setSelectedStock({ symbol, name });
    setIsChartVisible(true);
  };

  const renderItem = ({ item }: { item: MarketData }) => {
    // 1. Sembol Tıraşlama:
    const displaySymbol = item.symbol.split('.')[0];
    const isWatched = watchlist.includes(item.symbol); // API/Watchlist işlemleri için orijinal 'item.symbol' kullanmaya devam ediyoruz
    
    // 2. Renklendirme Mantığı (Backend'den gelen en güncel sinyale göre):
    let borderColor = '#30363D'; // NO_SIGNAL veya NOT_ENOUGH_DATA (Gri)
    let badgeText = null;
    let badgeBgColor = '#30363D';

    if (item.signal === 'GOLDEN_CROSS') {
        borderColor = '#238636'; // Yeşil Kalın Çerçeve
        badgeText = 'Golden Cross';
        badgeBgColor = '#238636'; 
    } else if (item.signal === 'DEAD_CROSS') {
        borderColor = '#DA3633'; // Kırmızı Kalın Çerçeve
        badgeText = 'Dead Cross';
        badgeBgColor = '#DA3633';
    }

    const diff = item.cross_price && item.current_price ? (item.current_price - item.cross_price).toFixed(2) : '-';
    const diffColor = item.cross_price && item.current_price ? (item.current_price >= item.cross_price ? '#3FB950' : '#F85149') : '#8B949E';

    return (
      <TouchableOpacity 
        style={[styles.card, { borderColor: borderColor, borderWidth: item.signal === 'NO_SIGNAL' ? 1 : 2 }]}
        onPress={() => handleOpenChart(item.symbol, item.name)}
        activeOpacity={0.8}
      >
        <View style={styles.cardHeader}>
          <View style={{ flex: 1 }}>
            <Text style={styles.ticker}>{displaySymbol}</Text>
            <Text style={styles.name}>{item.name}</Text>
          </View>
          <View style={{ flexDirection: 'row', alignItems: 'center' }}>
            {badgeText && (
              <View style={[styles.badge, { backgroundColor: badgeBgColor }]}>
                <Text style={styles.badgeText}>{badgeText}</Text>
              </View>
            )}
            <TouchableOpacity 
              style={styles.watchButton} 
              onPress={() => toggleWatch(item.symbol)}
              activeOpacity={0.7}
              testID={`MarketScreen:WatchButton:${item.symbol}`}
            >
              <Ionicons 
                name={isWatched ? "eye" : "eye-outline"} 
                size={22} 
                color={isWatched ? "#F6C90E" : "#8B949E"} 
              />
            </TouchableOpacity>
          </View>
        </View>
        
        <View style={styles.infoRow}>
          <View>
            <Text style={styles.label}>Fiyat</Text>
            <Text style={styles.value}>{item.current_price?.toFixed(2) || '-'}</Text>
          </View>
          <View>
            <Text style={styles.label}>SMA50</Text>
            <Text style={styles.value}>{item.sma50?.toFixed(2) || '-'}</Text>
          </View>
          <View>
            <Text style={styles.label}>SMA200</Text>
            <Text style={styles.value}>{item.sma200?.toFixed(2) || '-'}</Text>
          </View>
        </View>

        <View style={styles.diffContainer}>
          <Text style={styles.label}>Kesişimden Beri Fark:</Text>
          <Text style={[styles.diffValue, { color: diffColor }]}>
            {item.cross_price ? (parseFloat(diff) > 0 ? `+${diff}` : diff) : '-'}
          </Text>
        </View>
      </TouchableOpacity>
    );
  };

  if (loading) {
    return (
      <View style={styles.container}>
        <Text style={styles.title}>Piyasalar</Text>
        <ScrollView showsVerticalScrollIndicator={false}>
          {[1, 2, 3, 4, 5].map(i => <MarketCardSkeleton key={i} />)}
        </ScrollView>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Piyasalar</Text>
      {data.length === 0 ? (
        <StatusMessage 
          type="empty" 
          title="Veri Bulunamadı" 
          message="Şu an piyasada görüntülenecek herhangi bir veri bulunmuyor. Lütfen daha sonra tekrar deneyin."
          onRetry={() => fetchData(true)}
        />
      ) : (
        <FlatList
          testID="MarketScreen:FlatList"
          data={data}
          renderItem={renderItem}
          keyExtractor={(item) => item.symbol}
          contentContainerStyle={styles.list}
          refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor="#F6C90E" />}
          onEndReached={handleLoadMore}
          onEndReachedThreshold={0.5}
          ListFooterComponent={loadingMore ? <ActivityIndicator style={{ marginVertical: 20 }} color="#F6C90E" /> : null}
        />
      )}

      {/* Hisse Grafiği Alt Paneli */}
      <ChartBottomSheet
        isVisible={isChartVisible}
        onClose={() => setIsChartVisible(false)}
        symbol={selectedStock?.symbol || null}
        name={selectedStock?.name || null}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#0D1117', paddingHorizontal: 16, paddingTop: 60 },
  loaderContainer: { flex: 1, backgroundColor: '#0D1117', justifyContent: 'center', alignItems: 'center' },
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
  name: { fontSize: 12, color: '#8B949E', marginTop: 2 },
  badge: { paddingHorizontal: 8, paddingVertical: 4, borderRadius: 6 },
  badgeText: { color: '#FFFFFF', fontSize: 12, fontWeight: '700' },
  infoRow: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 12 },
  label: { color: '#8B949E', fontSize: 12, marginBottom: 4 },
  value: { color: '#FFFFFF', fontSize: 15, fontWeight: '600' },
  diffContainer: { borderTopWidth: 1, borderTopColor: '#30363D', paddingTop: 10, flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  diffValue: { fontSize: 16, fontWeight: '700' },
  watchButton: {
    padding: 6,
    marginLeft: 8,
    borderRadius: 8,
    backgroundColor: '#30363D44',
  },
});
