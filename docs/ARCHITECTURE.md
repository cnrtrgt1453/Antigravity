# Antigravity - Proje Mimarisi Özeti

Bu belge, Antigravity piyasa analizi ve sinyal uygulaması için hibrit (Polyglot Microservices) mimariyi özetler.

## Teknoloji Yığını

### 1. Analiz Motoru (Python)
- **Rol:** Veri çekme, matematiksel hesaplamalar ve sinyal üretimi.
- **Teknoloji:** Python 3.9+, FastAPI, Pandas, TA-Lib, `yfinance`.
- **Mantık:** Teknik analizleri (SMA, Golden/Dead Cross) gerçekleştirir ve sonuçları REST API üzerinden sunar.

### 2. Çekirdek API ve Orkestrasyon (Java Spring Boot)
- **Rol:** Arka plan orkestrasyonu, güvenlik, veri kalıcılığı ve dış servis koordinasyonu.
- **Teknoloji:** Java 17+, Spring Boot 3, Spring Security, Spring Data JPA.
- **İletişim:** Analiz sonuçları için Python motoruyla etkileşime girer ve mobil ön yüze hizmet verir.

### 3. Mobil Ön Yüz (Android Native)
- **Rol:** Kullanıcıların piyasa sinyallerini, haberleri ve sanal portföylerini görebileceği arayüz.
- **Teknoloji:** Kotlin, Jetpack Compose, Hilt (Dependency Injection), Coroutines & Flow.

### 4. Oyun ve Portföy Sistemi
- **Rol:** Kullanıcıların gerçek verilerle sanal alım-satım yapmasını sağlayan modül.
- **Mantık:** Java tarafında cüzdan ve işlem geçmişi yönetilirken, Python tarafı anlık fiyatlama ve kar/zarar hesaplamalarını sağlar.

### 5. Veritabanı Katmanı (PostgreSQL)
- **Rol:** Kullanıcı verileri, işlem geçmişi, piyasa sinyalleri ve haberler için kalıcı depolama.
- **Optimizasyon:** İzleme listesi gibi sık sorgulanan alanlar için kompozit indeksler ile optimize edilmiştir.

## Temel İş Akışları

### Günlük Analiz Döngüsü
1. **Tetikleyici:** Python'daki zamanlanmış görev veya mobilden gelen manuel tetikleme.
2. **Analiz:** Python motoru OHLC verilerini çeker ve kesişimleri hesaplar.
3. **Kalıcılık:** Sonuçlar, geçmiş takibi için PostgreSQL'de saklanır.

### Haber Senkronizasyonu
1. **Tetikleyici:** Java tarafında hafta içi 18:15'te çalışan cron görevi.
2. **Çekme:** Dış finansal haber kaynakları ile entegrasyon.
3. **Akıllı Güncelleme:** UID/Link bazlı tekilleştirme ile veri güncelliği.

### Oyun ve Alım-Satım Akışı
1. **İşlem:** Kullanıcı bir hisseyi almayı veya satmayı onaylar.
2. **Doğrulama:** Java backend bakiye ve stok miktarını kontrol eder.
3. **Güncelleme:** İşlem sonucu portföy tablosuna kaydedilir ve işlem geçmişine eklenir.
