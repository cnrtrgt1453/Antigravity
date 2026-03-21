# Özellik: Market Sinyalleri (Golden/Dead Cross)

BIST piyasasındaki potansiyel alım veya satım fırsatlarını belirlemek için kullanılan otomatik teknik analiz modülüdür.

## Analiz Mantığı
- **Göstergeler:** 50 günlük ve 200 günlük Basit Hareketli Ortalamalar (SMA).
- **Golden Cross (Altın Kesişim):** 50 günlük SMA'nın 200 günlük SMA'yı yukarı yönlü kesmesi (Alım Sinyali).
- **Dead Cross (Ölüm Kesişimi):** 50 günlük SMA'nın 200 günlük SMA'yı aşağı yönlü kesmesi (Satım Sinyali).

## Sistem Etkileşimi
1. **Python Motoru:** Pandas kullanarak SMA değerlerini hesaplar ve kesişimleri tespit eder.
2. **Java Backend:** Sonuçları alır ve `MarketSignal` tablosuna kaydeder.
3. **Frontend:** Sinyalleri fiyat ve tarih bilgileriyle birlikte kullanıcı panelinde görüntüler.

## Performans ve Optimizasyon
- **Tarama Bekleme Süresi:** Sunucu yükünü azaltmak için manuel tam taramalar arasında 12 saatlik bekleme süresi uygulanır.
- **Geçmiş Takibi:** Veriler, trend analizi için PostgreSQL'de haftalık olarak arşivlenir.
