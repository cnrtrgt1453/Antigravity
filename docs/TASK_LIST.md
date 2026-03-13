# Borsa Analiz Uygulaması Görev Listesi

## 1. Planlama ve Temel Klasör Yapısı 
- [x] Ana proje dizininin oluşturulması (`Antigravity/` içinde)
- [x] GitHub docs klasörünün ve strateji planlarının oluşturulması.
- [ ] Python Hesaplama Motoru (`python-analysis-engine/`) iskeletinin oluşturulması
- [ ] Java Spring Boot Ana Sunucu (`java-core-api/`) iskeletinin oluşturulması
- [ ] SOLID ve Temiz Mimari (Clean Architecture) klasör hiyerarşisinin belirlenmesi

## 2. Python Hesaplama Motoru Geliştirmeleri
- [ ] FastAPI projesinin başlatılması
- [ ] `yfinance` ile veri çekme servisinin (SOLID: Sorumlulukların ayrılması) yazılması
- [ ] TA-Lib ile Golden/Dead Cross hesaplama modülünün yazılması
- [ ] REST Controller uç noktalarının (endpoints) oluşturulması

## 3. Java Spring Boot (Core API) Geliştirmeleri
- [ ] Spring Boot projesinin başlatılması (Java 17+, PostgreSQL, JPA, Web vb.)
- [ ] Temiz mimari katmanlarının oluşturulması (Controllers, Services, Repositories, Entities)
- [ ] Bağımlılık Enjeksiyonu (DI) ve Interface'lerin (Arayüzlerin) tanımlanması
- [ ] PostgreSQL ile veritabanı bağlantısının kurulması
- [ ] Firebase Admin SDK entegrasyonu (Bildirimler ve "Vitrin" veritabanı için)
- [ ] Python motoru ile iletişim kuracak `@Scheduled` Spring job'ının yazılması

## 4. Mobil Geliştirme (React Native)
- [x] React Native (Expo) projesinin başlatılması
- [x] Clean Architecture (Domain, Data, Presentation) iskeletinin kurulması
- [x] Firebase Authentication (Kayıt/Giriş) ekranlarının temelleri ve Zustand entegrasyonu
- [ ] Ana ekran (Vitrin) - Firebase Firestore'dan anlık veri okuma
- [ ] Hisse detay grafikleri ve geçmiş veriler için REST API çağrıları
