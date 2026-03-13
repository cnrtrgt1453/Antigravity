import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  ActivityIndicator,
  KeyboardAvoidingView,
  Platform,
  StatusBar,
} from 'react-native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../../../App';

type NavigationProp = NativeStackNavigationProp<RootStackParamList, 'Register'>;

interface Props {
  navigation: NavigationProp;
}

export const RegisterScreen: React.FC<Props> = ({ navigation }) => {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleRegister = async () => {
    // Şimdilik sadece mock loading
    setIsLoading(true);
    setError(null);
    try {
      setTimeout(() => {
        setIsLoading(false);
        // Doğrudan Login ekranına geri gönder, normalde backend'e post edilir.
        navigation.navigate('Login');
      }, 1000);
    } catch (e: any) {
      setError(e.message);
      setIsLoading(false);
    }
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <StatusBar barStyle="light-content" backgroundColor="#0D1117" />

      {/* Geri Butonu & Başlık */}
      <View style={styles.headerContainer}>
        <TouchableOpacity style={styles.backButton} onPress={() => navigation.goBack()}>
          <Text style={styles.backIcon}>←</Text>
        </TouchableOpacity>
        <Text style={styles.title}>Kayıt Ol</Text>
        <Text style={styles.subtitle}>Borsa Analiz Özel Dünyasına Katılın</Text>
      </View>

      {/* Kart Alanı */}
      <View style={styles.card}>
        {/* Ad Soyad */}
        <View style={styles.inputWrapper}>
          <Text style={styles.label}>Ad Soyad</Text>
          <TextInput
            style={styles.input}
            placeholder="Caner O..."
            placeholderTextColor="#4A5568"
            autoCapitalize="words"
            autoCorrect={false}
            value={fullName}
            onChangeText={setFullName}
            onFocus={() => setError(null)}
          />
        </View>

        {/* E-posta */}
        <View style={styles.inputWrapper}>
          <Text style={styles.label}>E-posta</Text>
          <TextInput
            style={styles.input}
            placeholder="ornek@email.com"
            placeholderTextColor="#4A5568"
            keyboardType="email-address"
            autoCapitalize="none"
            autoCorrect={false}
            value={email}
            onChangeText={setEmail}
            onFocus={() => setError(null)}
          />
        </View>

        {/* Şifre */}
        <View style={styles.inputWrapper}>
          <Text style={styles.label}>Şifre</Text>
          <View style={styles.passwordRow}>
            <TextInput
              style={[styles.input, styles.passwordInput]}
              placeholder="••••••••"
              placeholderTextColor="#4A5568"
              secureTextEntry={!showPassword}
              value={password}
              onChangeText={setPassword}
              onFocus={() => setError(null)}
            />
            <TouchableOpacity
              style={styles.eyeButton}
              onPress={() => setShowPassword(!showPassword)}
            >
              <Text style={styles.eyeIcon}>{showPassword ? '🙈' : '👁️'}</Text>
            </TouchableOpacity>
          </View>
        </View>

        {/* Hata Mesajı */}
        {error ? (
          <View style={styles.errorBox}>
            <Text style={styles.errorText}>⚠️  {error}</Text>
          </View>
        ) : null}

        {/* Kayıt Ol Butonu */}
        <TouchableOpacity
          style={[styles.primaryButton, isLoading && styles.buttonDisabled]}
          onPress={handleRegister}
          disabled={isLoading}
          activeOpacity={0.8}
        >
          {isLoading ? (
            <ActivityIndicator color="#0D1117" />
          ) : (
            <Text style={styles.primaryButtonText}>Ücretsiz Kayıt Ol</Text>
          )}
        </TouchableOpacity>
        
        {/* Zaten Hesabım Var Linki */}
        <TouchableOpacity style={styles.loginLinkButton} onPress={() => navigation.navigate('Login')}>
          <Text style={styles.loginLinkText}>Hesabınız var mı? <Text style={{fontWeight: '700', color: '#F6C90E'}}>Giriş Yapın</Text></Text>
        </TouchableOpacity>
      </View>

    </KeyboardAvoidingView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0D1117',
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 24,
  },
  headerContainer: {
    width: '100%',
    marginBottom: 36,
    alignItems: 'center',
    position: 'relative',
  },
  backButton: {
    position: 'absolute',
    left: 0,
    top: 5,
    padding: 8,
    zIndex: 10,
  },
  backIcon: {
    color: '#F6C90E',
    fontSize: 24,
    fontWeight: 'bold',
  },
  title: {
    fontSize: 28,
    fontWeight: '700',
    color: '#FFFFFF',
    letterSpacing: 0.5,
  },
  subtitle: {
    fontSize: 13,
    color: '#6B7280',
    marginTop: 6,
    textAlign: 'center',
  },
  card: {
    width: '100%',
    backgroundColor: '#161B22',
    borderRadius: 20,
    padding: 24,
    borderWidth: 1,
    borderColor: '#21262D',
    shadowColor: '#000',
    shadowOpacity: 0.4,
    shadowRadius: 20,
    elevation: 10,
  },
  inputWrapper: {
    marginBottom: 16,
  },
  label: {
    fontSize: 13,
    fontWeight: '600',
    color: '#9CA3AF',
    marginBottom: 8,
    letterSpacing: 0.3,
  },
  input: {
    backgroundColor: '#0D1117',
    borderWidth: 1,
    borderColor: '#21262D',
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 14,
    fontSize: 15,
    color: '#FFFFFF',
    width: '100%',
  },
  passwordRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  passwordInput: {
    flex: 1,
    paddingRight: 48,
  },
  eyeButton: {
    position: 'absolute',
    right: 14,
    padding: 4,
  },
  eyeIcon: {
    fontSize: 18,
  },
  errorBox: {
    backgroundColor: '#2D1111',
    borderRadius: 10,
    borderWidth: 1,
    borderColor: '#7F1D1D',
    padding: 12,
    marginBottom: 16,
  },
  errorText: {
    color: '#FCA5A5',
    fontSize: 13,
    lineHeight: 18,
  },
  primaryButton: {
    backgroundColor: '#F6C90E',
    borderRadius: 14,
    paddingVertical: 15,
    alignItems: 'center',
    marginTop: 8,
    shadowColor: '#F6C90E',
    shadowOpacity: 0.4,
    shadowRadius: 10,
    elevation: 6,
  },
  buttonDisabled: {
    opacity: 0.7,
  },
  primaryButtonText: {
    color: '#0D1117',
    fontSize: 16,
    fontWeight: '700',
    letterSpacing: 0.5,
  },
  loginLinkButton: {
    alignItems: 'center',
    marginTop: 24,
    padding: 4,
  },
  loginLinkText: {
    color: '#9CA3AF',
    fontSize: 14,
  },
});
