import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  StatusBar,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';

export const PrivacyPolicyScreen: React.FC = () => {
  const navigation = useNavigation();

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#0D1117" />
      
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backButton}>
          <Ionicons name="arrow-back" size={24} color="#FFFFFF" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Gizlilik Politikası</Text>
      </View>

      <ScrollView contentContainerStyle={styles.content}>
        <Text style={styles.updateDate}>Son Güncelleme: 3 Nisan 2024</Text>
        
        <Text style={styles.sectionTitle}>1. Giriş</Text>
        <Text style={styles.paragraph}>
          FinanceUp ("biz", "tarafımız" veya "uygulama"), kullanıcılarımızın gizliliğine önem vermektedir. 
          Bu Gizlilik Politikası, uygulamamızı kullandığınızda bilgilerinizin nasıl toplandığını, 
          kullanıldığını ve korunduğunu açıklar.
        </Text>

        <Text style={styles.sectionTitle}>2. Toplanan Bilgiler</Text>
        <Text style={styles.paragraph}>
          Uygulamamızı kullandığınızda aşağıdaki bilgileri toplayabiliriz:
          {'\n'}• Hesap Bilgileri: Adınız, e-posta adresiniz ve profil resminiz (Sosyal giriş yapıldığında).
          {'\n'}• Uygulama Verileri: İzleme listeniz, portföy hareketleriniz ve işlem geçmişiniz.
          {'\n'}• Cihaz Bilgileri: İşletim sistemi sürümü ve cihaz modeli (Hata ayıklama ve performans analizi için).
        </Text>

        <Text style={styles.sectionTitle}>3. Bilgilerin Kullanımı</Text>
        <Text style={styles.paragraph}>
          Topladığımız veriler şu amaçlarla kullanılır:
          {'\n'}• Uygulama özelliklerini ve yatırım simülasyonunu sürdürmek.
          {'\n'}• Size özel analiz raporları sunmak.
          {'\n'}• Güvenliği sağlamak ve dolandırıcılığı önlemek.
          {'\n'}• Kullanıcı deneyimini iyileştirmek.
        </Text>

        <Text style={styles.sectionTitle}>4. Veri Paylaşımı</Text>
        <Text style={styles.paragraph}>
          Verileriniz üçüncü şahıslara satılmaz. Ancak yasal zorunluluklar veya 
          bulut hizmet sağlayıcılarımız (Firebase, Google Cloud vb.) ile operasyonel 
          amaçlarla sınırlı bilgi paylaşımı yapılabilir.
        </Text>

        <Text style={styles.sectionTitle}>5. Veri Saklama ve Silme</Text>
        <Text style={styles.paragraph}>
          Verileriniz, hesabınız aktif olduğu sürece saklanır. İstediğiniz zaman uygulama 
          içindeki "Hesabımı Sil" seçeneğini kullanarak tüm verilerinizin kalıcı olarak 
          silinmesini talep edebilirsiniz.
        </Text>

        <Text style={styles.sectionTitle}>6. Güvenlik</Text>
        <Text style={styles.paragraph}>
          Verilerinizi korumak için endüstri standardı güvenlik önlemleri almaktayız. 
          Ancak internet üzerinden yapılan hiçbir iletimin %100 güvenli olmadığını belirtmek isteriz.
        </Text>

        <Text style={styles.sectionTitle}>7. İletişim</Text>
        <Text style={styles.paragraph}>
          Bu politika ile ilgili sorularınız için bizimle iletişime geçebilirsiniz.
        </Text>

        <View style={styles.footerSpace} />
      </ScrollView>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0D1117',
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingTop: 60,
    paddingBottom: 20,
    paddingHorizontal: 20,
    borderBottomWidth: 1,
    borderBottomColor: '#30363D',
  },
  backButton: {
    marginRight: 16,
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: '800',
    color: '#FFFFFF',
  },
  content: {
    padding: 20,
  },
  updateDate: {
    color: '#8B949E',
    fontSize: 12,
    marginBottom: 24,
  },
  sectionTitle: {
    color: '#FFFFFF',
    fontSize: 18,
    fontWeight: '700',
    marginTop: 24,
    marginBottom: 12,
  },
  paragraph: {
    color: '#C9D1D9',
    fontSize: 15,
    lineHeight: 22,
  },
  footerSpace: {
    height: 40,
  },
});
