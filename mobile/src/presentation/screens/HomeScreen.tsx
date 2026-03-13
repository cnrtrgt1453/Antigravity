// Presentation Layer - Home Screen
import React, { useEffect, useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, StatusBar, ScrollView, ActivityIndicator } from 'react-native';
import { useAuthStore } from '../stores/useAuthStore';
import { MarketInstrument } from '../../domain/entities/MarketInstrument';
import { ApiMarketRepository } from '../../data/repositories/ApiMarketRepository';
import { MarketTrendCard } from '../components/MarketTrendCard';

export const HomeScreen: React.FC = () => {
  const { user, logout } = useAuthStore();
  const [marketData, setMarketData] = useState<MarketInstrument[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
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

    fetchMarketData();
    // Refresh every 6 hours (6 * 60 * 60 * 1000 = 21600000 ms)
    const interval = setInterval(fetchMarketData, 21600000);
    return () => clearInterval(interval);
  }, []);

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#0D1117" />
      
      <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={styles.scrollContent}>
        <View style={styles.header}>
          <Text style={styles.greeting}>Hoş geldin 👋</Text>
          <Text style={styles.email}>{user?.displayName ?? user?.email}</Text>
        </View>

        <View style={styles.marketSection}>
          <Text style={styles.sectionTitle}>Piyasa Özeti (₺)</Text>
          {loading ? (
            <ActivityIndicator size="small" color="#58A6FF" style={{ marginTop: 20 }} />
          ) : (
            marketData.map((instrument) => (
              <MarketTrendCard key={instrument.id} instrument={instrument} />
            ))
          )}
        </View>

        <View style={styles.card}>
          <Text style={styles.cardIcon}>📈</Text>
          <Text style={styles.cardTitle}>Golden Cross Sinyalleri</Text>
          <Text style={styles.cardSubtitle}>Yakında burada günlük tarama sonuçları görünecek.</Text>
        </View>

        <View style={styles.card}>
          <Text style={styles.cardIcon}>📉</Text>
          <Text style={styles.cardTitle}>Dead Cross Sinyalleri</Text>
          <Text style={styles.cardSubtitle}>Yakında burada günlük tarama sonuçları görünecek.</Text>
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
  sectionTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#FFFFFF',
    marginBottom: 12,
  },
  card: {
    backgroundColor: '#161B22',
    borderRadius: 16,
    padding: 20,
    marginBottom: 16,
    borderWidth: 1,
    borderColor: '#21262D',
  },
  cardIcon: {
    fontSize: 28,
    marginBottom: 10,
  },
  cardTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: '#FFFFFF',
    marginBottom: 6,
  },
  cardSubtitle: {
    fontSize: 13,
    color: '#6B7280',
    lineHeight: 20,
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
