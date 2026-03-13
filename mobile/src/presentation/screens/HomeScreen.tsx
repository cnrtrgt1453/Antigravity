// Presentation Layer — Home Screen (giriş sonrası geçici iskelet ekran)
import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet, StatusBar } from 'react-native';
import { useAuthStore } from '../stores/useAuthStore';

export const HomeScreen: React.FC = () => {
  const { user, logout } = useAuthStore();

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#0D1117" />
      <View style={styles.header}>
        <Text style={styles.greeting}>Hoş geldin 👋</Text>
        <Text style={styles.email}>{user?.displayName ?? user?.email}</Text>
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
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0D1117',
    paddingHorizontal: 24,
    paddingTop: 60,
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
    marginTop: 'auto',
    marginBottom: 40,
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
