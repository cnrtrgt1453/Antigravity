import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  ActivityIndicator,
  RefreshControl,
  Alert,
  TextInput,
  Modal
} from 'react-native';
import { useGameStore } from '../stores/useGameStore';
import { Config } from '../../config';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../../types/navigation';
import { Skeleton } from '../components/Skeleton';
import { StatusMessage } from '../components/StatusMessage';

export const GameScreen: React.FC = () => {
  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const { 
    portfolio, 
    history, 
    watchlist, 
    isLoading, 
    fetchPortfolio, 
    fetchHistory, 
    fetchWatchlist,
    buyStock,
    sellStock,
    removeFromWatchlist
  } = useGameStore();

  const [marketData, setMarketData] = useState<any[]>([]);
  const [refreshing, setRefreshing] = useState(false);
  const [tradeModalVisible, setTradeModalVisible] = useState(false);
  const [selectedStock, setSelectedStock] = useState<any>(null);
  const [tradeType, setTradeType] = useState<'BUY' | 'SELL'>('BUY');
  const [quantity, setQuantity] = useState('1');

  const fetchAll = async () => {
    await Promise.all([
      fetchPortfolio(),
      fetchHistory(),
      fetchWatchlist(),
      fetchMarketPrices()
    ]);
  };

  const fetchMarketPrices = async () => {
    try {
      const response = await fetch(`${Config.PYTHON_API_URL}/api/v1/analysis/all_market_data`);
      const data = await response.json();
      setMarketData(data);
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    fetchAll();
  }, []);

  const onRefresh = () => {
    setRefreshing(true);
    fetchAll().finally(() => setRefreshing(false));
  };

  const getPrice = (symbol: string) => {
    const stock = marketData.find(m => m.ticker === symbol);
    return stock?.current_price || 0;
  };

  const handleTrade = async () => {
    const qty = parseInt(quantity);
    if (isNaN(qty) || qty <= 0) {
      Alert.alert("Hata", "Geçerli bir miktar giriniz.");
      return;
    }

    try {
      if (tradeType === 'BUY') {
        await buyStock(selectedStock.symbol, qty, selectedStock.price);
      } else {
        await sellStock(selectedStock.symbol, qty, selectedStock.price);
      }
      setTradeModalVisible(false);
      Alert.alert("Başarılı", "İşlem başarıyla gerçekleştirildi.");
    } catch (e: any) {
      Alert.alert("İşlem Başarısız", e.message);
    }
  };

  const openTradeModal = (symbol: string, type: 'BUY' | 'SELL') => {
    const price = getPrice(symbol);
    if (price === 0) {
      Alert.alert("Hata", "Fiyat bilgisi alınamadı.");
      return;
    }
    setSelectedStock({ symbol, price });
    setTradeType(type);
    setQuantity('1');
    setTradeModalVisible(true);
  };

  const renderPortfolioItem = (item: any) => {
    const currentPrice = getPrice(item.stockSymbol);
    const profit = (currentPrice - item.averageCost) * item.quantity;
    const profitPercent = ((currentPrice - item.averageCost) / item.averageCost) * 100;

    return (
      <View key={item.id} style={styles.stockCard}>
        <View style={styles.cardInfo}>
          <Text style={styles.stockSymbol}>{item.stockSymbol}</Text>
          <Text style={styles.stockQty}>{item.quantity} Adet</Text>
        </View>
        <View style={styles.cardPrices}>
          <Text style={styles.currentPrice}>{currentPrice.toFixed(2)} TL</Text>
          <Text style={[styles.profit, { color: profit >= 0 ? '#3FB950' : '#F85149' }]}>
            {profit >= 0 ? '+' : ''}{profit.toFixed(2)} ({profitPercent.toFixed(2)}%)
          </Text>
        </View>
        <TouchableOpacity 
          style={styles.sellBtn} 
          onPress={() => openTradeModal(item.stockSymbol, 'SELL')}
        >
          <Text style={styles.btnText}>SAT</Text>
        </TouchableOpacity>
      </View>
    );
  };

  const renderWatchlistItem = (symbol: string) => {
    const price = getPrice(symbol);
    return (
      <View key={symbol} style={styles.stockCard}>
        <View style={styles.cardInfo}>
          <Text style={styles.stockSymbol}>{symbol}</Text>
          <Text style={styles.stockPriceLabel}>Anlık Fiyat</Text>
        </View>
        <View style={styles.cardPrices}>
          <Text style={styles.currentPrice}>{price > 0 ? price.toFixed(2) : '-'} TL</Text>
        </View>
        <View style={styles.actionRow}>
          <TouchableOpacity 
            style={styles.buyBtn} 
            onPress={() => openTradeModal(symbol, 'BUY')}
          >
            <Text style={styles.btnText}>AL</Text>
          </TouchableOpacity>
          <TouchableOpacity 
            style={styles.removeBtn} 
            onPress={() => removeFromWatchlist(symbol)}
          >
            <Ionicons name="trash-outline" size={18} color="#8B949E" />
          </TouchableOpacity>
        </View>
      </View>
    );
  };

  if (isLoading && !refreshing) {
    return (
      <View style={styles.container}>
        <ScrollView contentContainerStyle={styles.scroll} showsVerticalScrollIndicator={false}>
          <Text style={styles.title}>Oyun Paneli</Text>
          <View style={styles.walletCard}>
            <Skeleton width="40%" height={14} style={{ marginBottom: 12 }} />
            <Skeleton width="60%" height={32} />
          </View>
          <View style={styles.sectionHeader}>
            <Skeleton width={100} height={20} />
          </View>
          {[1, 2].map(i => (
            <View key={i} style={styles.stockCard}>
              <View style={styles.cardInfo}>
                <Skeleton width="60%" height={16} />
                <Skeleton width="40%" height={12} style={{ marginTop: 8 }} />
              </View>
            </View>
          ))}
        </ScrollView>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <ScrollView 
        contentContainerStyle={styles.scroll}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor="#F6C90E" />}
      >
        <Text style={styles.title}>Oyun Paneli</Text>
        
        {/* Wallet Section */}
        <View style={styles.walletCard}>
           <Text style={styles.walletLabel}>Toplam Bakiye</Text>
           <Text style={styles.balance}>{portfolio?.balance?.toFixed(2) || '0.00'} TL</Text>
           <View style={styles.walletInfo}>
             <Ionicons name="information-circle-outline" size={16} color="#8B949E" />
             <Text style={styles.walletHint}>İşlemlerde %1 komisyon uygulanmaktadır.</Text>
           </View>
        </View>

        {/* Portfolio Section */}
        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>Portföyüm</Text>
          <Ionicons name="briefcase-outline" size={20} color="#F6C90E" />
        </View>
        {portfolio?.items && portfolio.items.length > 0 ? (
          portfolio.items.map(renderPortfolioItem)
        ) : (
          <View style={styles.emptyCard}>
            <StatusMessage 
              type="empty"
              title="Portföy Boş"
              message="Henüz bir yatırımınız bulunmuyor. İzleme listenizdeki hisselerden alım yaparak başlayabilirsiniz."
              icon="briefcase-outline"
            />
          </View>
        )}

        {/* Watchlist Section */}
        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>İzleme Listem (Hızlı Al/Sat)</Text>
          <Ionicons name="eye-outline" size={20} color="#F6C90E" />
        </View>
        {watchlist.length > 0 ? (
          watchlist.map(renderWatchlistItem)
        ) : (
          <View style={styles.emptyCard}>
            <StatusMessage 
              type="empty"
              title="İzleme Listesi Boş"
              message="Piyasalar ekranından hisse ekleyerek hızlı al-sat işlemlerine başlayabilirsiniz."
              icon="eye-outline"
            />
          </View>
        )}

        <TouchableOpacity 
          style={styles.historyBtn} 
          onPress={() => navigation.navigate('TradeHistory')}
        >
          <Ionicons name="time-outline" size={20} color="#FFFFFF" />
          <Text style={styles.historyBtnText}>İşlem Geçmişini Gör</Text>
        </TouchableOpacity>
      </ScrollView>

      {/* Trade Modal */}
      <Modal
        animationType="slide"
        transparent={true}
        visible={tradeModalVisible}
        onRequestClose={() => setTradeModalVisible(false)}
      >
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <Text style={styles.modalTitle}>
              {tradeType === 'BUY' ? 'Hisse Al' : 'Hisse Sat'} - {selectedStock?.symbol}
            </Text>
            <Text style={styles.modalPrice}>Fiyat: {selectedStock?.price?.toFixed(2)} TL</Text>
            
            <View style={styles.inputGroup}>
              <Text style={styles.inputLabel}>Adet</Text>
              <TextInput
                style={styles.input}
                value={quantity}
                onChangeText={setQuantity}
                keyboardType="numeric"
                placeholder="Miktar giriniz"
                placeholderTextColor="#4A5568"
              />
            </View>

            <Text style={styles.totalCost}>
              Toplam: {(parseFloat(quantity || '0') * (selectedStock?.price || 0)).toFixed(2)} TL
            </Text>

            <View style={styles.modalActions}>
              <TouchableOpacity 
                style={styles.cancelBtn} 
                onPress={() => setTradeModalVisible(false)}
              >
                <Text style={styles.cancelBtnText}>İptal</Text>
              </TouchableOpacity>
              <TouchableOpacity 
                style={[styles.confirmBtn, { backgroundColor: tradeType === 'BUY' ? '#238636' : '#DA3633' }]} 
                onPress={handleTrade}
              >
                <Text style={styles.confirmBtnText}>Onayla</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#0D1117' },
  scroll: { padding: 20, paddingBottom: 40 },
  center: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: '#0D1117' },
  title: { fontSize: 28, fontWeight: '800', color: '#FFFFFF', marginTop: 40, marginBottom: 20 },
  
  walletCard: {
    backgroundColor: '#1C2128',
    borderRadius: 20,
    padding: 24,
    marginBottom: 30,
    borderWidth: 1,
    borderColor: '#30363D',
  },
  walletLabel: { color: '#8B949E', fontSize: 14, fontWeight: '600', marginBottom: 8 },
  balance: { color: '#F6C90E', fontSize: 32, fontWeight: '800' },
  walletInfo: { flexDirection: 'row', alignItems: 'center', marginTop: 12 },
  walletHint: { color: '#8B949E', fontSize: 11, marginLeft: 6 },

  sectionHeader: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: 15, marginTop: 10 },
  sectionTitle: { fontSize: 18, fontWeight: '700', color: '#FFFFFF' },
  
  stockCard: {
    backgroundColor: '#161B22',
    borderRadius: 16,
    padding: 16,
    marginBottom: 12,
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#30363D',
  },
  cardInfo: { flex: 1 },
  stockSymbol: { color: '#FFFFFF', fontSize: 16, fontWeight: '700' },
  stockQty: { color: '#8B949E', fontSize: 13, marginTop: 4 },
  stockPriceLabel: { color: '#8B949E', fontSize: 12, marginTop: 4 },
  
  cardPrices: { flex: 1, alignItems: 'center' },
  currentPrice: { color: '#FFFFFF', fontSize: 15, fontWeight: '600' },
  profit: { fontSize: 12, marginTop: 4, fontWeight: '700' },
  
  actionRow: { flexDirection: 'row', alignItems: 'center' },
  buyBtn: { backgroundColor: '#238636', paddingHorizontal: 16, paddingVertical: 8, borderRadius: 8, marginRight: 8 },
  sellBtn: { backgroundColor: '#DA3633', paddingHorizontal: 16, paddingVertical: 8, borderRadius: 8 },
  removeBtn: { padding: 8 },
  btnText: { color: '#FFFFFF', fontSize: 13, fontWeight: '800' },
  
  historyBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#1C2128',
    borderRadius: 12,
    padding: 16,
    marginTop: 20,
    borderWidth: 1,
    borderColor: '#30363D',
  },
  historyBtnText: { color: '#FFFFFF', fontWeight: '700', marginLeft: 10 },
  emptyText: { color: '#8B949E', textAlign: 'center', paddingVertical: 20, fontStyle: 'italic' },
  emptyCard: {
    backgroundColor: '#161B22',
    borderRadius: 16,
    padding: 20,
    marginBottom: 12,
    borderWidth: 1,
    borderColor: '#30363D',
    minHeight: 200,
  },

  // Modal Styles
  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.8)', justifyContent: 'center', padding: 20 },
  modalContent: { backgroundColor: '#1C2128', borderRadius: 24, padding: 24, borderWidth: 1, borderColor: '#30363D' },
  modalTitle: { color: '#FFFFFF', fontSize: 20, fontWeight: '800', marginBottom: 10 },
  modalPrice: { color: '#F6C90E', fontSize: 16, fontWeight: '600', marginBottom: 20 },
  inputGroup: { marginBottom: 20 },
  inputLabel: { color: '#8B949E', fontSize: 13, marginBottom: 8 },
  input: {
    backgroundColor: '#0D1117',
    borderRadius: 12,
    padding: 14,
    color: '#FFFFFF',
    fontSize: 16,
    borderWidth: 1,
    borderColor: '#30363D',
  },
  totalCost: { color: '#FFFFFF', fontSize: 18, fontWeight: '700', textAlign: 'right', marginBottom: 24 },
  modalActions: { flexDirection: 'row', justifyContent: 'space-between' },
  cancelBtn: { padding: 16, flex: 1, alignItems: 'center' },
  cancelBtnText: { color: '#8B949E', fontWeight: '600' },
  confirmBtn: { padding: 16, flex: 2, borderRadius: 12, alignItems: 'center' },
  confirmBtnText: { color: '#FFFFFF', fontWeight: '800' },
});
