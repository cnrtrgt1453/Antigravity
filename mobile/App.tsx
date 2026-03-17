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
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { Ionicons } from '@expo/vector-icons';

// Yeni Ekranlar
import { MarketScreen } from './src/presentation/screens/MarketScreen';
import { GoldenScreen } from './src/presentation/screens/GoldenScreen';
import { DeadScreen } from './src/presentation/screens/DeadScreen';

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
  Main: undefined;
};

const Stack = createNativeStackNavigator<RootStackParamList>();
const Tab = createBottomTabNavigator();

function MainTabs() {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        tabBarIcon: ({ focused, color, size }) => {
          let iconName: any;

          if (route.name === 'Piyasalar') {
            iconName = focused ? 'stats-chart' : 'stats-chart-outline';
          } else if (route.name === 'Golden Cross') {
            iconName = focused ? 'trending-up' : 'trending-up-outline';
          } else if (route.name === 'Dead Cross') {
            iconName = focused ? 'trending-down' : 'trending-down-outline';
          } else if (route.name === 'Haberler') {
            iconName = focused ? 'newspaper' : 'newspaper-outline';
          }

          return <Ionicons name={iconName} size={size} color={color} />;
        },
        tabBarActiveTintColor: '#F6C90E',
        tabBarInactiveTintColor: '#8B949E',
        tabBarStyle: {
          backgroundColor: '#161B22',
          borderTopColor: '#30363D',
          height: 60,
          paddingBottom: 10,
        },
        headerShown: false,
      })}
    >
      <Tab.Screen name="Piyasalar" component={MarketScreen} />
      <Tab.Screen name="Golden Cross" component={GoldenScreen} />
      <Tab.Screen name="Dead Cross" component={DeadScreen} />
      <Tab.Screen name="Haberler" component={NewsScreen} />
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
            <Stack.Screen name="Main" component={MainTabs} />
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
