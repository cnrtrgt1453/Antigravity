// Application Configuration
// In a real environment, these would be populated from .env via react-native-config or similar
export const Config = {
  PYTHON_API_URL: 'http://192.168.1.132:8000',
  JAVA_API_URL: 'http://192.168.1.132:8080',
  REFRESH_INTERVALS: {
    MARKET_DATA: 21600000, // 6 hours
    SIGNALS: 600000,      // 10 minutes
    COOLDOWN: 60000       // 1 minute
  }
};
