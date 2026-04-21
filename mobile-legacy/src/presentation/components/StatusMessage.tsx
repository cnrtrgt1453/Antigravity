import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

interface StatusMessageProps {
  type: 'empty' | 'error' | 'no-connection';
  title: string;
  message: string;
  onRetry?: () => void;
  icon?: string;
}

export const StatusMessage: React.FC<StatusMessageProps> = ({
  type,
  title,
  message,
  onRetry,
  icon
}) => {
  const getIcon = () => {
    if (icon) return icon;
    switch (type) {
      case 'empty': return 'search-outline';
      case 'error': return 'alert-circle-outline';
      case 'no-connection': return 'wifi-outline';
      default: return 'help-circle-outline';
    }
  };

  const getIconColor = () => {
    switch (type) {
      case 'empty': return '#8B949E';
      case 'error': return '#F85149';
      case 'no-connection': return '#F6C90E';
      default: return '#8B949E';
    }
  };

  return (
    <View style={styles.container}>
      <View style={[styles.iconContainer, { backgroundColor: getIconColor() + '11' }]}>
        <Ionicons name={getIcon() as any} size={48} color={getIconColor()} />
      </View>
      <Text style={styles.title}>{title}</Text>
      <Text style={styles.message}>{message}</Text>
      
      {onRetry && (
        <TouchableOpacity style={styles.retryButton} onPress={onRetry}>
          <Text style={styles.retryText}>Tekrar Dene</Text>
        </TouchableOpacity>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    padding: 40,
    alignItems: 'center',
    justifyContent: 'center',
    flex: 1,
  },
  iconContainer: {
    width: 100,
    height: 100,
    borderRadius: 50,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 20,
  },
  title: {
    fontSize: 20,
    fontWeight: '800',
    color: '#FFFFFF',
    marginBottom: 8,
    textAlign: 'center',
  },
  message: {
    fontSize: 14,
    color: '#8B949E',
    textAlign: 'center',
    lineHeight: 20,
    marginBottom: 30,
  },
  retryButton: {
    paddingHorizontal: 24,
    paddingVertical: 12,
    backgroundColor: '#1C2128',
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#30363D',
  },
  retryText: {
    color: '#F6C90E',
    fontWeight: '700',
    fontSize: 14,
  },
});
