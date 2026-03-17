import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { LoginScreen } from './src/presentation/screens/LoginScreen';
import { RegisterScreen } from './src/presentation/screens/RegisterScreen';
import { HomeScreen } from './src/presentation/screens/HomeScreen';
import { NewsScreen } from './src/presentation/screens/NewsScreen';
import { useAuthStore } from './src/presentation/stores/useAuthStore';
import Constants from 'expo-constants';

// Google Sign-in sadece gerçek native buildler'de veya development buildler'de çalışır.
// Expo Go içindeyken bu kütüphane çökmemesi için sadece uygun ortamda yüklenir.
if (Constants.appOwnership !== 'expo') {
  try {
    const { GoogleSignin } = require('@react-native-google-signin/google-signin');
    GoogleSignin.configure({
      webClientId: '777162969154-ha4tnq6c6bu0b4ijcpb01ae8m3d9gpc9.apps.googleusercontent.com',
      offlineAccess: true,
    });
  } catch (e) {
    console.warn('Google Sign-in yüklenemedi:', e);
  }
}

export type RootStackParamList = {
  Login: undefined;
  Register: undefined;
  Home: undefined;
  News: undefined;
};

const Stack = createNativeStackNavigator<RootStackParamList>();

export default function App() {
  const { user } = useAuthStore();

  return (
    <SafeAreaProvider>
      <NavigationContainer>
        <Stack.Navigator screenOptions={{ headerShown: false }}>
          {user ? (
            <>
              <Stack.Screen name="Home" component={HomeScreen} />
              <Stack.Screen name="News" component={NewsScreen} />
            </>
          ) : (
            <>
              <Stack.Screen name="Login" component={LoginScreen} />
              <Stack.Screen name="Register" component={RegisterScreen} />
            </>
          )}
        </Stack.Navigator>
      </NavigationContainer>
    </SafeAreaProvider>
  );
}
