import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { LoginScreen } from './src/presentation/screens/LoginScreen';
import { HomeScreen } from './src/presentation/screens/HomeScreen';
import { NewsScreen } from './src/presentation/screens/NewsScreen';
import { useAuthStore } from './src/presentation/stores/useAuthStore';
import Constants from 'expo-constants';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { Ionicons } from '@expo/vector-icons';

// Yeni Ekranlar
import { MarketScreen } from './src/presentation/screens/MarketScreen';
import { SignalsScreen } from './src/presentation/screens/SignalsScreen';
import { GameScreen } from './src/presentation/screens/GameScreen';
import { TradeHistoryScreen } from './src/presentation/screens/TradeHistoryScreen';
import { ProfileScreen } from './src/presentation/screens/ProfileScreen';
import { PrivacyPolicyScreen } from './src/presentation/screens/PrivacyPolicyScreen';

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

import { RootStackParamList } from './src/types/navigation';

const Stack = createNativeStackNavigator<RootStackParamList>();
const Tab = createBottomTabNavigator();

function MainTabs() {
  return (
    <Tab.Navigator
      screenOptions={{
        tabBarActiveTintColor: '#F6C90E',
        tabBarInactiveTintColor: '#8B949E',
        tabBarStyle: {
          backgroundColor: '#161B22',
          borderTopColor: '#30363D',
          height: 60,
          paddingBottom: 10,
        },
        headerShown: false,
      }}
    >
      <Tab.Screen
        name="Piyasalar"
        component={MarketScreen}
        options={{
          tabBarIcon: ({ focused, color, size }) => (
            <Ionicons name={focused ? 'stats-chart' : 'stats-chart-outline'} size={size} color={color} />
          ),
        }}
      />
      <Tab.Screen
        name="Sinyaller"
        component={SignalsScreen}
        options={{
          tabBarIcon: ({ focused, color, size }) => (
            <Ionicons name={focused ? 'flash' : 'flash-outline'} size={size} color={color} />
          ),
        }}
      />
      <Tab.Screen
        name="Haberler"
        component={NewsScreen}
        options={{
          tabBarIcon: ({ focused, color, size }) => (
            <Ionicons name={focused ? 'newspaper' : 'newspaper-outline'} size={size} color={color} />
          ),
        }}
      />
      <Tab.Screen
        name="Oyun"
        component={GameScreen}
        options={{
          tabBarIcon: ({ focused, color, size }) => (
            <Ionicons name={focused ? 'game-controller' : 'game-controller-outline'} size={size} color={color} />
          ),
        }}
      />
      <Tab.Screen
        name="Profil"
        component={ProfileScreen}
        options={{
          tabBarIcon: ({ focused, color, size }) => (
            <Ionicons name={focused ? 'person' : 'person-outline'} size={size} color={color} />
          ),
        }}
      />
    </Tab.Navigator>
  );
}

export default function App() {
  const { user } = useAuthStore();

  return (
    <SafeAreaProvider>
      <NavigationContainer>
        <Stack.Navigator screenOptions={{ headerShown: false }}>
          {user ? (
            <>
              <Stack.Screen name="Main" component={MainTabs} options={{ headerShown: false }} />
              <Stack.Screen name="TradeHistory" component={TradeHistoryScreen} options={{
                headerShown: true,
                title: 'İşlem Geçmişi',
                headerStyle: { backgroundColor: '#0D1117' },
                headerTintColor: '#FFFFFF',
                headerTitleStyle: { fontWeight: '800' }
              }} />
              <Stack.Screen name="PrivacyPolicy" component={PrivacyPolicyScreen} options={{ headerShown: false }} />
            </>
          ) : (
            <>
              <Stack.Screen name="Login" component={LoginScreen} />
            </>
          )}
        </Stack.Navigator>
      </NavigationContainer>
    </SafeAreaProvider>
  );
}
