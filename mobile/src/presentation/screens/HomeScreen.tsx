// Presentation Layer - Home Screen
import React, { useEffect, useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, StatusBar, ScrollView, ActivityIndicator, Alert } from 'react-native';
import { useAuthStore } from '../stores/useAuthStore';
import { MarketInstrument } from '../../domain/entities/MarketInstrument';
import { ApiMarketRepository } from '../../data/repositories/ApiMarketRepository';
import { MarketTrendCard } from '../components/MarketTrendCard';

// Python API URL - Adjust if your IP changes or using simulator
// Python API URL - Using local network IP for physical device testing
const PYTHON_API_URL = 'http://192.168.1.157:8000';
const JAVA_API_URL = 'http://192.168.1.157:8080';

interface SignalData {
  ticker: string;
  signal: string;
  color: string;
  message: string;
  cross_date: string | null;
  current_price: number | null;
  last_updated: string | null;
}

export const HomeScreen: React.FC = () => {
  const { user, logout } = useAuthStore();
  const [marketData, setMarketData] = useState<MarketInstrument[]>([]);
  const [loading, setLoading] = useState(true);
  const [scanLoading, setScanLoading] = useState(false);
  const [cooldown, setCooldown] = useState<{ can_scan: boolean, remaining_seconds: number }>({ can_scan: true, remaining_seconds: 0 });
  const [signals, setSignals] = useState<{ golden_signals: SignalData[], dead_signals: SignalData[] }>({ golden_signals: [], dead_signals: [] });
  const [signalsLoading, setSignalsLoading] = useState(true);

  const fetchMarketData = async () => {
    try {
      const repo = new ApiMarketRepository();
      const data = await repo.getMarketSummary();
      setMarketData(data);
    } catch (error) {
      console.error("Failed to fetch market data", error);
    } finally {
      setLoading(false);
    }
  };

  const fetchSignals = async () => {
    try {
      setSignalsLoading(true);
      const response = await fetch(`${JAVA_API_URL}/api/v1/signals`);
      if (response.ok) {
        const data = await response.json();
        setSignals(data);
      }
    } catch (error) {
      console.log("Failed to fetch signals (Backend might be down)");
    } finally {
      setSignalsLoading(false);
    }
  };

  const fetchCooldownStatus = async () => {
    try {
      const response = await fetch(`${PYTHON_API_URL}/api/v1/analysis/cooldown_status`);
      if (response.ok) {
        const data = await response.json();
        setCooldown(data);
      }
    } catch (error) {
      console.log("Cooldown status check failed (Backend might be down)");
    }
  };

  const handleManualScan = async () => {
    if (!cooldown.can_scan) {
      Alert.alert("Beklemeniz Gerekiyor", `Bir sonraki tarama için kalan süre: ${formatCooldown(cooldown.remaining_seconds)}`);
      return;
    }

    setScanLoading(true);
    try {
      const response = await fetch(`${PYTHON_API_URL}/api/v1/analysis/run_full_scan_now`);
      const result = await response.json();
      
      if (response.ok) {
        Alert.alert("Başarılı", "Tüm enstrümanlar tarandı ve sinyaller güncellendi.");
        fetchMarketData();
        fetchSignals();
        fetchCooldownStatus();
      } else {
        Alert.alert("Hata", result.detail || "Tarama başlatılamadı.");
      }
    } catch (error) {
      Alert.alert("Bağlantı Hatası", "Python analiz motoruna ulaşılamadı. Lütfen sunucunun açık olduğundan emin olun.");
    } finally {
      setScanLoading(false);
    }
  };

  const formatCooldown = (seconds: number) => {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    return `${h}s ${m}d`;
  };

  useEffect(() => {
    fetchMarketData();
    fetchSignals();
    fetchCooldownStatus();
    
    // Refresh market data every 6 hours
    const interval = setInterval(fetchMarketData, 21600000);
    
    // Poll signals every 10 minutes
    const signalsInterval = setInterval(fetchSignals, 600000);
    
    // Poll cooldown status every minute to update UI
    const cooldownInterval = setInterval(fetchCooldownStatus, 60000);
    
    return () => {
      clearInterval(interval);
      clearInterval(signalsInterval);
      clearInterval(cooldownInterval);
    };
  }, []);

  const renderSignalList = (list: SignalData[]) => {
    if (list.length === 0) {
      return <Text style={styles.cardSubtitle}>Şu an için aktif bir sinyal bulunmuyor.</Text>;
    }
    return list.map((item, idx) => (
      <View key={idx} style={styles.signalItem}>
        <Text style={styles.signalTicker}>{item.ticker.replace('.IS', '')}</Text>
        <View style={styles.signalRight}>
          <Text style={styles.signalPrice}>{item.current_price?.toFixed(2)} ₺</Text>
          <Text style={styles.signalDate}>{item.cross_date}</Text>
        </View>
      </View>
    ));
  };

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#0D1117" />
      
      <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={styles.scrollContent}>
        <View style={styles.header}>
          <Text style={styles.greeting}>Hoş geldin 👋</Text>
          <Text style={styles.email}>{user?.fullName ?? user?.email}</Text>
        </View>

        <View style={styles.marketSection}>
          <View style={styles.sectionHeader}>
            <Text style={styles.sectionTitle}>Piyasa Özeti (₺)</Text>
            <TouchableOpacity 
              style={[styles.scanButton, !cooldown.can_scan && styles.scanButtonDisabled]} 
              onPress={handleManualScan}
              disabled={scanLoading}
            >
              {scanLoading ? (
                <ActivityIndicator size="small" color="#FFFFFF" />
              ) : (
                <Text style={styles.scanButtonText}>
                  {cooldown.can_scan ? "Şimdi Tara" : formatCooldown(cooldown.remaining_seconds)}
                </Text>
              )}
            </TouchableOpacity>
          </View>

          {loading ? (
            <ActivityIndicator size="small" color="#58A6FF" style={{ marginTop: 20 }} />
          ) : (
            marketData.map((instrument) => (
              <MarketTrendCard key={instrument.id} instrument={instrument} />
            ))
          )}
        </View>

        <View style={styles.card}>
          <View style={styles.cardHeader}>
            <Text style={styles.cardIcon}>📈</Text>
            <Text style={styles.cardTitle}>Golden Cross Sinyalleri</Text>
          </View>
          {signalsLoading ? (
            <ActivityIndicator size="small" color="#238636" />
          ) : (
            renderSignalList(signals.golden_signals)
          )}
        </View>

        <View style={styles.card}>
          <View style={styles.cardHeader}>
            <Text style={styles.cardIcon}>📉</Text>
            <Text style={styles.cardTitle}>Dead Cross Sinyalleri</Text>
          </View>
          {signalsLoading ? (
            <ActivityIndicator size="small" color="#7F1D1D" />
          ) : (
            renderSignalList(signals.dead_signals)
          )}
        </View>

        <TouchableOpacity style={styles.logoutButton} onPress={logout} activeOpacity={0.8}>
          <Text style={styles.logoutText}>Çıkış Yap</Text>
        </TouchableOpacity>
      </ScrollView>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0D1117',
    paddingTop: 60,
  },
  scrollContent: {
    paddingHorizontal: 24,
    paddingBottom: 40,
  },
  header: {
    marginBottom: 32,
  },
  greeting: {
    fontSize: 22,
    fontWeight: '700',
    color: '#FFFFFF',
  },
  email: {
    fontSize: 14,
    color: '#6B7280',
    marginTop: 4,
  },
  marketSection: {
    marginBottom: 24,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#FFFFFF',
  },
  scanButton: {
    backgroundColor: '#238636',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 8,
    minWidth: 90,
    alignItems: 'center',
  },
  scanButtonDisabled: {
    backgroundColor: '#21262D',
  },
  scanButtonText: {
    color: '#FFFFFF',
    fontSize: 12,
    fontWeight: '600',
  },
  card: {
    backgroundColor: '#161B22',
    borderRadius: 16,
    padding: 20,
    marginBottom: 16,
    borderWidth: 1,
    borderColor: '#21262D',
  },
  cardHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 12,
  },
  cardIcon: {
    fontSize: 24,
    marginRight: 10,
  },
  cardTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: '#FFFFFF',
  },
  cardSubtitle: {
    fontSize: 13,
    color: '#6B7280',
    lineHeight: 20,
  },
  signalItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 10,
    borderBottomWidth: 1,
    borderBottomColor: '#21262D',
  },
  signalTicker: {
    fontSize: 15,
    fontWeight: '600',
    color: '#FFFFFF',
  },
  signalRight: {
    alignItems: 'flex-end',
  },
  signalPrice: {
    fontSize: 14,
    color: '#C9D1D9',
    fontWeight: '500',
  },
  signalDate: {
    fontSize: 11,
    color: '#6B7280',
    marginTop: 2,
  },
  logoutButton: {
    marginTop: 20,
    borderWidth: 1,
    borderColor: '#7F1D1D',
    borderRadius: 14,
    paddingVertical: 15,
    alignItems: 'center',
  },
  logoutText: {
    color: '#FCA5A5',
    fontSize: 15,
    fontWeight: '600',
  },
});
