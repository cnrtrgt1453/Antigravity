// Presentation Layer — Login Screen
// Bu ekran sadece useAuthStore üzerinden Domain UseCase'leri çağırır.
// Firebase, Axios veya başka hiçbir harici bağımlılık içermez. (SRP + DIP)
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
  Animated,
} from 'react-native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../../types/navigation';
import { useAuthStore } from '../stores/useAuthStore';
import Constants from 'expo-constants';
import { Alert } from 'react-native';

type NavigationProp = NativeStackNavigationProp<RootStackParamList, 'Login'>;

interface Props {
  navigation: NavigationProp;
}

export const LoginScreen: React.FC<Props> = ({ navigation }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);

  const { login, loginWithGoogle, isLoading, error, clearError } = useAuthStore();

  const handleLogin = async () => {
    if (error) clearError();
    await login(email, password);
  };

  const handleGoogleLogin = async () => {
    if (Constants.appOwnership === 'expo') {
      Alert.alert(
        'Expo Go Kısıtlaması',
        'Google ile giriş özelliği Expo Go uygulamasında çalışmamaktadır. Bunu test etmek için "Development Build" gereklidir. Şimdilik e-posta/şifre ile giriş yapabilirsiniz.',
        [{ text: 'Tamam' }]
      );
      return;
    }

    if (error) clearError();
    try {
      const { GoogleSignin } = require('@react-native-google-signin/google-signin');
      await GoogleSignin.hasPlayServices();
      const response = await GoogleSignin.signIn();
      const idToken = response.data?.idToken;

      if (idToken) {
        await loginWithGoogle(idToken);
      } else {
        throw new Error('Google Sign-In idToken alınamadı.');
      }
    } catch (err: any) {
      const { statusCodes } = require('@react-native-google-signin/google-signin');
      if (err.code === statusCodes.SIGN_IN_CANCELLED) {
        console.log('Kullanıcı Google girişini iptal etti');
      } else if (err.code === statusCodes.IN_PROGRESS) {
        // Zaten işlemde
      } else if (err.code === statusCodes.PLAY_SERVICES_NOT_AVAILABLE) {
        console.error('Play services mevcut değil veya güncel değil');
      } else {
        console.error('Bazı Google hataları oluştu:', err);
      }
    }
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <StatusBar barStyle="light-content" backgroundColor="#0D1117" />

      {/* Logo & Başlık */}
      <View style={styles.headerContainer}>
        <View style={styles.logoWrapper}>
          <Text style={styles.logoIcon}>📈</Text>
        </View>
        <Text style={styles.title}>Borsa Analiz</Text>
        <Text style={styles.subtitle}>Golden Cross & Dead Cross Takip Sistemi</Text>
      </View>

      {/* Kart Alanı */}
      <View style={styles.card}>
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
            onFocus={clearError}
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
              onFocus={clearError}
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

        {/* Giriş Butonu */}
        <TouchableOpacity
          style={[styles.loginButton, isLoading && styles.loginButtonDisabled]}
          onPress={handleLogin}
          disabled={isLoading}
          activeOpacity={0.8}
        >
          {isLoading ? (
            <ActivityIndicator color="#0D1117" />
          ) : (
            <Text style={styles.loginButtonText}>Giriş Yap</Text>
          )}
        </TouchableOpacity>

        <TouchableOpacity style={styles.forgotButton}>
          <Text style={styles.forgotText}>Şifreni mi unuttun?</Text>
        </TouchableOpacity>

        {/* Veya Ayracı */}
        <View style={styles.dividerContainer}>
          <View style={styles.dividerLine} />
          <Text style={styles.dividerText}>VEYA</Text>
          <View style={styles.dividerLine} />
        </View>

        {/* Google ile Giriş Butonu */}
        <TouchableOpacity
          style={styles.googleButton}
          onPress={handleGoogleLogin}
          disabled={isLoading}
        >
          <Text style={styles.googleIcon}>G</Text>
          <Text style={styles.googleButtonText}>Google ile Devam Et</Text>
        </TouchableOpacity>

        {/* Kayıt Ol Linki */}
        <TouchableOpacity style={styles.registerLinkButton} onPress={() => navigation.navigate('Register')}>
          <Text style={styles.registerLinkText}>Hesabınız yok mu? <Text style={{ fontWeight: '700', color: '#F6C90E' }}>Kayıt Olun</Text></Text>
        </TouchableOpacity>
      </View>

      {/* Alt Bilgi */}
      <Text style={styles.footer}>
        Teknik analiz sinyalleri yatırım tavsiyesi değildir.
      </Text>
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
    alignItems: 'center',
    marginBottom: 36,
  },
  logoWrapper: {
    width: 72,
    height: 72,
    borderRadius: 20,
    backgroundColor: '#161B22',
    borderWidth: 1,
    borderColor: '#F6C90E30',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 16,
    shadowColor: '#F6C90E',
    shadowOpacity: 0.3,
    shadowRadius: 12,
    elevation: 8,
  },
  logoIcon: {
    fontSize: 36,
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
  loginButton: {
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
  loginButtonDisabled: {
    opacity: 0.7,
  },
  loginButtonText: {
    color: '#0D1117',
    fontSize: 16,
    fontWeight: '700',
    letterSpacing: 0.5,
  },
  forgotButton: {
    alignItems: 'center',
    marginTop: 16,
    padding: 4,
  },
  forgotText: {
    color: '#58A6FF',
    fontSize: 14,
  },
  dividerContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginVertical: 24,
  },
  dividerLine: {
    flex: 1,
    height: 1,
    backgroundColor: '#30363D',
  },
  dividerText: {
    color: '#8B949E',
    paddingHorizontal: 12,
    fontSize: 12,
    fontWeight: '600',
  },
  googleButton: {
    flexDirection: 'row',
    backgroundColor: '#FFFFFF',
    borderRadius: 14,
    paddingVertical: 14,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 8,
  },
  googleIcon: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#4285F4',
    marginRight: 10,
  },
  googleButtonText: {
    color: '#000000',
    fontSize: 15,
    fontWeight: '600',
  },
  registerLinkButton: {
    alignItems: 'center',
    marginTop: 16,
    padding: 4,
  },
  registerLinkText: {
    color: '#9CA3AF',
    fontSize: 14,
  },
  footer: {
    marginTop: 28,
    fontSize: 11,
    color: '#374151',
    textAlign: 'center',
  },
});
