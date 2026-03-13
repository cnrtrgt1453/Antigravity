# Borsa Analiz Uygulaması Teknoloji Planı

Golden Cross ve Dead Cross gibi teknik analiz göstergelerini takip eden, günlük hisse taraması yapan mobil uygulama için belirlenen **hibrit (Polyglot Microservices)** mimari planı.

## Kesinleşen Teknoloji Yığını

### 1. Hesaplama Motoru ve Analiz (Python)
*   **Görev:** Borsa verilerini çekmek, ağır matematiksel hesaplamaları yapmak ve sinyalleri üretmek.
*   **Araçlar:** Python, FastAPI (sunum için), Pandas ve TA-Lib (analiz için), `yfinance` (baştaki ücretsiz veri kaynağı).
*   **Gelecek Planı:** Başlangıçta maliyetleri sıfırda tutmak için `yfinance` ile sadece gün sonu (kapanış) verileri üzerinden çalışılacaktır. Uygulama gelir getirmeye başladığında daha güvenilir ücretli API'lere (Matriks, Foreks vb.) geçilecektir. İlerleyen süreçte saatlik sinyallerin de eklenebilmesi için PostgreSQL veritabanı esnek tasarlanacaktır.

### 2. Ana Sunucu ve İş Mantığı (Java Spring Boot)
*   **Görev:** Sistem bileşenlerini koordine etmek, güvenliği sağlamak, geçmiş verileri depolamak ve Firebase ile iletişim kurmak.
*   **Araçlar:** Java 17+, Spring Boot, Spring Security, Spring Data JPA, Firebase Admin SDK.
*   **Mimari Karar:** Ağır matematiksel hesaplamaların Python'da, güvenli yönetim ve koordinasyonun Java'da kalacağı bu mikroservis (polyglot) mimarisi aynen korunacaktır.

### 3. Veritabanı Stratejisi (PostgreSQL + Firebase)
*   **"Mutfak" (PostgreSQL):** Java'nın kontrolünde olan, tüm geçmiş fiyat hareketlerinin (OHLC) ve binlerce satırlık verinin ücretsiz saklandığı ana SQL veritabanımız.
*   **"Vitrin" (Firebase Firestore):** Sadece o gün ortaya çıkan 15-20 hisselik "Golden Cross / Dead Cross" özet listelerinin tutulduğu, mobil uygulamanın anında ve çok hızlı okuyacağı NoSQL veritabanımız. Bu sayede Firebase limitlerine (okuma/yazma) asla takılmayız.

### 4. Güvenlik ve Bildirimler (Firebase)
*   **Firebase Authentication:** Kullanıcı kayıt, giriş ve şifre işlemleri. Mobil uygulama aldığı Token'ı Java'ya doğrulatır.
*   **Firebase Cloud Messaging (FCM):** Yeni bir kesişim sinyali oluştuğunda kullanıcılara anlık bildirim atılması. 

### 5. Mobil Uygulama (Frontend)
*   **Görev:** Kullanıcıya verileri şık bir şekilde sunmak, grafikleri çizmek.
*   **Araçlar:** React Native (Expo). Web teknolojilerine yakınlığı ve hızlı canlı önizleme imkanı.

---

## Mimari Akış (Günlük Döngü)

1.  **Tetiklenme:** Java Spring Boot, her gün borsa kapanış saatinde (örn: 18:30) bir `@Scheduled` görev çalıştırır.
2.  **Veri Analizi:** Java, Python FastAPI servisine "Bugünün analizini yap" diye HTTP isteği atar. Python `yfinance` ile verileri çeker, Pandas ile 50/200 günlük ortalamaları (SMA) ve Golden/Dead Cross hisselerini, anlık fiyattan yüzde sapmalarıyla birlikte bulur ve Java'ya döner.
3.  **Kaydetme (Mutfak):** Java, gelen bu detaylı analizi ve geçmiş verileri PostgreSQL veritabanına kaydeder.
4.  **Vitrin Güncelleme:** Java, PostgreSQL'e kaydettiği verilerin içinden sadece *bugün kesişim yaşamış* hisselerin kısa bir özetini alır ve Firebase Firestore'a "Vitrin Verisi" olarak kopyalar.
5.  **Bildirim:** Java, Firebase FCM üzerinden kullanıcılara "Yeni hisselerde Golden Cross tespit edildi" bildirimi yollar.
6.  **Kullanıcı Gösterimi:** Kullanıcı gelen bildirime tıklar, React Native uygulamasını açar. Uygulama doğrudan Firebase'e (Vitrin'e) bağlanarak o günkü hisse listesini milisaniyeler içinde gösterir. Derin geçmiş veri veya grafik detayları istendiğinde uygulama Java REST API'sine (PostgreSQL'e) istek atar.
