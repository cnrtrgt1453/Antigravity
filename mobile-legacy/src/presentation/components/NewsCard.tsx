import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Linking } from 'react-native';

interface NewsCardProps {
  news: {
    title: string;
    content: string;
    publishedAt: string;
    stockSymbol: string;
    sourceUrl: string;
  };
}

export const NewsCard: React.FC<NewsCardProps> = ({ news }) => {
  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('tr-TR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const handlePress = () => {
    if (news.sourceUrl) {
      Linking.openURL(news.sourceUrl);
    }
  };

  return (
    <TouchableOpacity style={styles.card} onPress={handlePress} activeOpacity={0.7}>
      <View style={styles.header}>
        <View style={styles.symbolBadge}>
          <Text style={styles.symbolText}>{news.stockSymbol.replace('.IS', '')}</Text>
        </View>
        <Text style={styles.dateText}>{formatDate(news.publishedAt)}</Text>
      </View>
      
      <Text style={styles.title}>{news.title}</Text>
      <Text style={styles.content} numberOfLines={3}>{news.content}</Text>
      
      <View style={styles.footer}>
        <Text style={styles.sourceText}>Kaynak: KAP / Haberler</Text>
        <Text style={styles.moreText}>Devamını Oku →</Text>
      </View>
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#161B22',
    borderRadius: 16,
    padding: 16,
    marginBottom: 16,
    borderWidth: 1,
    borderColor: '#30363D',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 10,
  },
  symbolBadge: {
    backgroundColor: '#23863622',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: '#238636',
  },
  symbolText: {
    color: '#3FB950',
    fontSize: 12,
    fontWeight: '700',
  },
  dateText: {
    color: '#8B949E',
    fontSize: 11,
  },
  title: {
    fontSize: 16,
    fontWeight: '700',
    color: '#F0F6FC',
    marginBottom: 8,
  },
  content: {
    fontSize: 14,
    color: '#8B949E',
    lineHeight: 20,
    marginBottom: 12,
  },
  footer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderTopWidth: 1,
    borderTopColor: '#21262D',
    paddingTop: 10,
  },
  sourceText: {
    fontSize: 11,
    color: '#6E7681',
  },
  moreText: {
    fontSize: 12,
    color: '#58A6FF',
    fontWeight: '600',
  },
});
