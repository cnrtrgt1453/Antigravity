// Presentation Layer — Profile Screen
// Kullanıcı bilgilerini görüntüler ve oturumu kapatır.
import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  Alert,
  StatusBar,
} from 'react-native';
import { useAuthStore } from '../stores/useAuthStore';
import { useGameStore } from '../stores/useGameStore';
import { Ionicons } from '@expo/vector-icons';

export const ProfileScreen: React.FC = () => {
  const { user, logout } = useAuthStore();
  const { portfolio, watchlist, history } = useGameStore();

  const handleLogout = () => {
    Alert.alert(
      'Çıkış Yap',
      'Hesabınızdan çıkmak istediğinize emin misiniz?',
      [
        { text: 'İptal', style: 'cancel' },
        {
          text: 'Çıkış Yap',
          style: 'destructive',
          onPress: async () => {
            await logout();
          },
        },
      ]
    );
  };

  // Baş harf Circle renkleri (kullanıcıya tutarlı renk ver)
  const avatarColors = ['#F6C90E', '#58A6FF', '#3FB950', '#DA3633', '#BC8CFF'];
  const colorIndex = user?.fullName
    ? user.fullName.charCodeAt(0) % avatarColors.length
    : 0;
  const avatarColor = avatarColors[colorIndex];
  const initials = user?.fullName
    ? user.fullName
        .split(' ')
        .map((n) => n[0])
        .join('')
        .toUpperCase()
        .slice(0, 2)
    : '?';

  const totalTrades = history?.length ?? 0;
  const watchlistCount = watchlist?.length ?? 0;
  const portfolioBalance = portfolio?.balance?.toFixed(2) ?? '0.00';
  const portfolioItemCount = portfolio?.items?.length ?? 0;

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <StatusBar barStyle="light-content" backgroundColor="#0D1117" />

      {/* Üst: Avatar + İsim + E-posta */}
      <View style={styles.profileCard}>
        <View style={[styles.avatar, { backgroundColor: avatarColor + '22', borderColor: avatarColor }]}>
          <Text style={[styles.avatarText, { color: avatarColor }]}>{initials}</Text>
        </View>
        <Text style={styles.fullName}>{user?.fullName || 'Kullanıcı'}</Text>
        <Text style={styles.email}>{user?.email || ''}</Text>
        <View style={styles.memberBadge}>
          <Ionicons name="shield-checkmark" size={12} color="#F6C90E" />
          <Text style={styles.memberText}>Üye</Text>
        </View>
      </View>

      {/* İstatistikler */}
      <Text style={styles.sectionTitle}>İstatistikler</Text>
      <View style={styles.statsRow}>
        <View style={styles.statCard}>
          <Text style={styles.statValue}>{watchlistCount}</Text>
          <Text style={styles.statLabel}>Takip</Text>
        </View>
        <View style={styles.statCard}>
          <Text style={styles.statValue}>{totalTrades}</Text>
          <Text style={styles.statLabel}>İşlem</Text>
        </View>
        <View style={styles.statCard}>
          <Text style={styles.statValue}>{portfolioItemCount}</Text>
          <Text style={styles.statLabel}>Portföy</Text>
        </View>
      </View>

      {/* Bakiye Kartı */}
      <View style={styles.balanceCard}>
        <View style={styles.balanceRow}>
          <Ionicons name="wallet-outline" size={18} color="#F6C90E" />
          <Text style={styles.balanceLabel}>Oyun Bakiyesi</Text>
        </View>
        <Text style={styles.balanceValue}>{portfolioBalance} TL</Text>
      </View>

      {/* Uygulama Bilgisi */}
      <Text style={styles.sectionTitle}>Uygulama</Text>
      <View style={styles.infoCard}>
        <View style={styles.infoRow}>
          <Ionicons name="information-circle-outline" size={18} color="#8B949E" />
          <Text style={styles.infoText}>cotx Trade</Text>
          <Text style={styles.infoValue}>v1.0.0</Text>
        </View>
        <View style={[styles.infoRow, styles.infoRowBorder]}>
          <Ionicons name="trending-up-outline" size={18} color="#8B949E" />
          <Text style={styles.infoText}>Yöntem</Text>
          <Text style={styles.infoValue}>Golden / Dead Cross</Text>
        </View>
        <View style={[styles.infoRow, styles.infoRowBorder]}>
          <Ionicons name="alert-circle-outline" size={18} color="#8B949E" />
          <Text style={[styles.infoText, { flex: 1 }]}>
            Sinyaller yatırım tavsiyesi değildir.
          </Text>
        </View>
      </View>

      {/* Çıkış Yap */}
      <TouchableOpacity
        style={styles.logoutButton}
        onPress={handleLogout}
        activeOpacity={0.8}
        testID="ProfileScreen:LogoutButton"
      >
        <Ionicons name="log-out-outline" size={20} color="#F85149" />
        <Text style={styles.logoutText}>Çıkış Yap</Text>
      </TouchableOpacity>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0D1117',
  },
  content: {
    paddingHorizontal: 20,
    paddingTop: 60,
    paddingBottom: 40,
  },

  // Profile Card
  profileCard: {
    alignItems: 'center',
    marginBottom: 32,
  },
  avatar: {
    width: 88,
    height: 88,
    borderRadius: 44,
    borderWidth: 2,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 14,
  },
  avatarText: {
    fontSize: 32,
    fontWeight: '800',
  },
  fullName: {
    fontSize: 22,
    fontWeight: '800',
    color: '#FFFFFF',
    marginBottom: 4,
  },
  email: {
    fontSize: 14,
    color: '#8B949E',
    marginBottom: 10,
  },
  memberBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F6C90E18',
    borderRadius: 20,
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderWidth: 1,
    borderColor: '#F6C90E44',
    gap: 4,
  },
  memberText: {
    color: '#F6C90E',
    fontSize: 11,
    fontWeight: '700',
  },

  // Section Title
  sectionTitle: {
    fontSize: 13,
    fontWeight: '700',
    color: '#8B949E',
    textTransform: 'uppercase',
    letterSpacing: 1,
    marginBottom: 12,
  },

  // Stats
  statsRow: {
    flexDirection: 'row',
    gap: 10,
    marginBottom: 12,
  },
  statCard: {
    flex: 1,
    backgroundColor: '#161B22',
    borderRadius: 14,
    padding: 16,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#30363D',
  },
  statValue: {
    fontSize: 24,
    fontWeight: '800',
    color: '#FFFFFF',
    marginBottom: 4,
  },
  statLabel: {
    fontSize: 12,
    color: '#8B949E',
    fontWeight: '600',
  },

  // Balance Card
  balanceCard: {
    backgroundColor: '#161B22',
    borderRadius: 14,
    padding: 16,
    marginBottom: 28,
    borderWidth: 1,
    borderColor: '#30363D',
  },
  balanceRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    marginBottom: 8,
  },
  balanceLabel: {
    color: '#8B949E',
    fontSize: 13,
    fontWeight: '600',
  },
  balanceValue: {
    color: '#F6C90E',
    fontSize: 28,
    fontWeight: '800',
  },

  // Info Card
  infoCard: {
    backgroundColor: '#161B22',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#30363D',
    marginBottom: 28,
    overflow: 'hidden',
  },
  infoRow: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 14,
    gap: 10,
  },
  infoRowBorder: {
    borderTopWidth: 1,
    borderTopColor: '#30363D',
  },
  infoText: {
    flex: 1,
    color: '#C9D1D9',
    fontSize: 14,
  },
  infoValue: {
    color: '#8B949E',
    fontSize: 13,
    fontWeight: '600',
  },

  // Logout
  logoutButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    backgroundColor: '#F8514922',
    borderRadius: 14,
    padding: 16,
    borderWidth: 1,
    borderColor: '#F8514944',
  },
  logoutText: {
    color: '#F85149',
    fontSize: 16,
    fontWeight: '700',
  },
});
