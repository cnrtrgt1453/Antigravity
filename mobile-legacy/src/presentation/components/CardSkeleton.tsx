import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Skeleton } from './Skeleton';

export const MarketCardSkeleton: React.FC = () => (
  <View style={styles.card}>
    <View style={styles.header}>
      <View style={styles.left}>
        <Skeleton width="40%" height={24} borderRadius={6} />
        <Skeleton width="60%" height={14} style={{ marginTop: 8 }} />
      </View>
      <Skeleton width={80} height={24} borderRadius={12} />
    </View>
    <View style={styles.infoRow}>
      <Skeleton width="25%" height={30} />
      <Skeleton width="25%" height={30} />
      <Skeleton width="25%" height={30} />
    </View>
    <View style={styles.divider} />
    <Skeleton width="100%" height={16} />
  </View>
);

export const NewsCardSkeleton: React.FC = () => (
  <View style={styles.newsCard}>
    <View style={styles.newsHeader}>
      <Skeleton width="30%" height={16} borderRadius={4} />
      <Skeleton width="20%" height={12} borderRadius={4} />
    </View>
    <Skeleton width="100%" height={20} style={{ marginVertical: 12 }} />
    <Skeleton width="80%" height={18} />
    <View style={styles.newsFooter}>
      <Skeleton width={100} height={14} />
    </View>
  </View>
);

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#161B22',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    borderWidth: 1,
    borderColor: '#30363D',
  },
  header: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 16 },
  left: { flex: 1 },
  infoRow: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 16 },
  divider: { height: 1, backgroundColor: '#30363D', marginBottom: 16 },
  
  newsCard: {
    backgroundColor: '#161B22',
    borderRadius: 16,
    padding: 16,
    marginBottom: 16,
    borderWidth: 1,
    borderColor: '#30363D',
  },
  newsHeader: { flexDirection: 'row', justifyContent: 'space-between' },
  newsFooter: { marginTop: 16, alignItems: 'flex-end' }
});
