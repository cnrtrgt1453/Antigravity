import React, { useEffect, useState, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  ActivityIndicator,
  Alert,
  StatusBar,
  ScrollView,
} from 'react-native';
import { useAuthStore } from '../stores/useAuthStore';
import { NewsCard } from '../components/NewsCard';
import { Config } from '../../config';
import { NewsCardSkeleton } from '../components/CardSkeleton';
import { StatusMessage } from '../components/StatusMessage';

const JAVA_API_URL = Config.JAVA_API_URL;

interface News {
  id: number;
  title: string;
  content: string;
  publishedAt: string;
  stockSymbol: string;
  sourceUrl: string;
}

export const NewsScreen: React.FC = () => {
  const { user } = useAuthStore();
  const [news, setNews] = useState<News[]>([]);
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  
  // Filters
  const [watchlistOnly, setWatchlistOnly] = useState(true);
  const [selectedSymbol, setSelectedSymbol] = useState<string | null>(null);
  const [sortOrder, setSortOrder] = useState<'desc' | 'asc'>('desc');
  const [watchlist, setWatchlist] = useState<string[]>([]);
  
  // Report State
  const [reportLoading, setReportLoading] = useState(false);

  const fetchWatchlist = async () => {
    try {
      const response = await fetch(`${JAVA_API_URL}/api/v1/watchlist/list`);
      if (response.ok) {
        const data = await response.json();
        setWatchlist(data);
      }
    } catch (error) {
      console.error("Failed to fetch watchlist", error);
    }
  };

  const fetchNews = useCallback(async (resetPage = false) => {
    if (loading || (resetPage ? false : !hasMore)) return;
    
    setLoading(true);
    const currentPage = resetPage ? 0 : page;
    
    try {
      let url = `${JAVA_API_URL}/api/v1/news?page=${currentPage}&size=10&watchlistOnly=${watchlistOnly}&sort=publishedAt,${sortOrder}`;
      if (selectedSymbol) {
        url += `&symbol=${selectedSymbol}`;
      }
      
      const response = await fetch(url);
      if (response.ok) {
        const data = await response.json();
        const newNews = data.content;
        
        if (resetPage) {
          setNews(newNews);
        } else {
          setNews(prev => [...prev, ...newNews]);
        }
        
        setHasMore(!data.last);
        setPage(currentPage + 1);
      }
    } catch (error) {
      console.error("Failed to fetch news", error);
    } finally {
      // For UX: Delay hiding skeleton just a bit for smooth transition
      if (resetPage) {
        setTimeout(() => setLoading(false), 600);
      } else {
        setLoading(false);
      }
      setRefreshing(false);
    }
  }, [page, loading, hasMore, watchlistOnly, selectedSymbol, sortOrder]);

  useEffect(() => {
    fetchWatchlist();
  }, []);

  useEffect(() => {
    fetchNews(true);
  }, [watchlistOnly, selectedSymbol, sortOrder]);

  const handleRefresh = () => {
    setRefreshing(true);
    fetchNews(true);
  };

  const handleWeeklyReport = async () => {
    const today = new Date();
    const isFirstTime = news.length === 0;

    if (today.getDay() !== 1 && !isFirstTime) { // 1 = Monday
      Alert.alert("Kısıtlama", "Haftalık Analiz Raporu sadece Pazartesi günleri alınabilir.");
      return;
    }

    setReportLoading(true);
    try {
      const response = await fetch(`${JAVA_API_URL}/api/v1/news/weekly-report`, {
        method: 'POST'
      });
      const result = await response.json();
      
      if (response.ok) {
        Alert.alert("Haftalık Rapor", result.summary);
      } else {
        Alert.alert("Hata", result.message || "Rapor oluşturulamadı.");
      }
    } catch (error) {
      Alert.alert("Bağlantı Hatası", "Backend sunucusuna ulaşılamadı.");
    } finally {
      setReportLoading(false);
    }
  };

  const isMonday = new Date().getDay() === 1;
  const canShowReport = isMonday || news.length === 0;

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#0D1117" />
      
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Haberler & Analiz</Text>
        <TouchableOpacity 
          testID="NewsScreen:ReportButton"
          style={[styles.reportButton, !canShowReport && styles.reportButtonDisabled]} 
          onPress={handleWeeklyReport}
          disabled={reportLoading}
        >
          {reportLoading ? (
            <ActivityIndicator size="small" color="#FFFFFF" />
          ) : (
            <Text style={styles.reportButtonText} testID="NewsScreen:ReportButtonText">📊 Pazartesi Raporu</Text>
          )}
        </TouchableOpacity>
      </View>

      <View style={styles.filterSection}>
        <View style={styles.tabContainer}>
          <TouchableOpacity 
            style={[styles.tab, watchlistOnly && styles.activeTab]} 
            onPress={() => { setWatchlistOnly(true); setSelectedSymbol(null); }}
          >
            <Text style={[styles.tabText, watchlistOnly && styles.activeTabText]}>Takiplerim</Text>
          </TouchableOpacity>
          <TouchableOpacity 
            style={[styles.tab, !watchlistOnly && styles.activeTab]} 
            onPress={() => { setWatchlistOnly(false); setSelectedSymbol(null); }}
          >
            <Text style={[styles.tabText, !watchlistOnly && styles.activeTabText]}>Tümü</Text>
          </TouchableOpacity>
        </View>

        <View style={styles.sortRow}>
          <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.symbolList}>
            <TouchableOpacity 
              style={[styles.symbolChip, selectedSymbol === null && styles.activeSymbolChip]}
              onPress={() => setSelectedSymbol(null)}
            >
              <Text style={[styles.symbolChipText, selectedSymbol === null && styles.activeSymbolChipText]}>Hepsi</Text>
            </TouchableOpacity>
            {(watchlistOnly ? watchlist : ["THYAO", "EREGL", "ASELS", "SISE", "SASA"]).map(symbol => (
              <TouchableOpacity 
                key={symbol}
                style={[styles.symbolChip, selectedSymbol === symbol && styles.activeSymbolChip]}
                onPress={() => setSelectedSymbol(symbol)}
              >
                <Text style={[styles.symbolChipText, selectedSymbol === symbol && styles.activeSymbolChipText]}>{symbol}</Text>
              </TouchableOpacity>
            ))}
          </ScrollView>
          <TouchableOpacity 
            style={styles.sortToggle} 
            onPress={() => setSortOrder(prev => prev === 'desc' ? 'asc' : 'desc')}
          >
            <Text style={styles.sortToggleText}>{sortOrder === 'desc' ? "🕒 En Yeni" : "🕒 En Eski"}</Text>
          </TouchableOpacity>
        </View>
      </View>

      {loading && news.length === 0 ? (
        <ScrollView contentContainerStyle={styles.listContent} showsVerticalScrollIndicator={false}>
          {[1, 2, 3, 4].map(i => <NewsCardSkeleton key={i} />)}
        </ScrollView>
      ) : (
        <FlatList
          data={news}
          renderItem={({ item }) => <NewsCard news={item} />}
          keyExtractor={item => item.id.toString()}
          contentContainerStyle={styles.listContent}
          onRefresh={handleRefresh}
          refreshing={refreshing}
          onEndReached={() => fetchNews()}
          onEndReachedThreshold={0.5}
          ListFooterComponent={loading ? <ActivityIndicator style={{ marginVertical: 20 }} color="#58A6FF" /> : null}
          ListEmptyComponent={
            <StatusMessage 
              type="empty"
              title="Haber Bulunamadı"
              message={watchlistOnly ? "Takip listenizdeki hisseler için son dönemde herhangi bir haber/analiz paylaşılmamış." : "Şu an için görüntülenecek herhangi bir haber bulunmuyor."}
              onRetry={handleRefresh}
            />
          }
        />
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0D1117',
    paddingTop: 50,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    marginBottom: 20,
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: '800',
    color: '#FFFFFF',
  },
  reportButton: {
    backgroundColor: '#238636',
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 10,
  },
  reportButtonDisabled: {
    backgroundColor: '#21262D',
    opacity: 0.6,
  },
  reportButtonText: {
    color: '#FFFFFF',
    fontSize: 12,
    fontWeight: '700',
  },
  filterSection: {
    paddingHorizontal: 20,
    marginBottom: 10,
  },
  tabContainer: {
    flexDirection: 'row',
    backgroundColor: '#161B22',
    borderRadius: 12,
    padding: 4,
    marginBottom: 12,
  },
  tab: {
    flex: 1,
    paddingVertical: 8,
    alignItems: 'center',
    borderRadius: 8,
  },
  activeTab: {
    backgroundColor: '#21262D',
    borderWidth: 1,
    borderColor: '#30363D',
  },
  tabText: {
    color: '#8B949E',
    fontSize: 14,
    fontWeight: '600',
  },
  activeTabText: {
    color: '#FFFFFF',
  },
  sortRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  symbolList: {
    flex: 1,
    marginRight: 10,
  },
  symbolChip: {
    backgroundColor: '#161B22',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 20,
    marginRight: 8,
    borderWidth: 1,
    borderColor: '#30363D',
  },
  activeSymbolChip: {
    backgroundColor: '#23863622',
    borderColor: '#238636',
  },
  symbolChipText: {
    color: '#8B949E',
    fontSize: 12,
    fontWeight: '600',
  },
  activeSymbolChipText: {
    color: '#3FB950',
  },
  sortToggle: {
    backgroundColor: '#21262D',
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 8,
  },
  sortToggleText: {
    color: '#C9D1D9',
    fontSize: 11,
    fontWeight: '600',
  },
  listContent: {
    padding: 20,
    paddingTop: 10,
  },
  emptyText: {
    color: '#8B949E',
    textAlign: 'center',
    marginTop: 50,
    fontSize: 15,
  },
});
