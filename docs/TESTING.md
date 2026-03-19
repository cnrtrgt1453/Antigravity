# 🧪 Test ve Başlatma Rehberi

Bu doküman, Antigravity projesini yerel ortamınızda ve mobil cihazınızda nasıl çalıştıracağınızı adım adım açıklar.

## 1. Hazırlık: Veritabanı (PostgreSQL)
Uygulamanın verileri saklayabilmesi için PostgreSQL üzerinde bir veritabanına ihtiyacı vardır. Java (Spring Boot), veritabanı boş olsa bile tabloları sizin için otomatik oluşturacaktır.

### Adım 0: PostgreSQL Çalışıyor mu?
Eğer veritabanına bağlanamazsanız, önce servisin çalışıp çalışmadığını kontrol edin:
1. `Ctrl + Shift + Esc` ile **Görev Yöneticisi**'ni açın.
2. **Hizmetler (Services)** sekmesine gidin.
3. Listede `postgresql-x64-xx` (xx sürüm numaranızdır) bulun.
4. Durumu **Çalışıyor (Running)** değilse sağ tıklayıp **Başlat (Start)** deyin.

### Adım 1: borsa_db Veritabanını Oluşturma
1. Bilgisayarınızda **pgAdmin 4** uygulamasını başlatın veya `psql` kullanın.
2. `borsa_db` isminde bir veritabanı oluşturun.
3. Kullanıcı adı ve şifrenin `postgres/postgres` (veya kendi ayarlarınız) olduğundan emin olun.
4. Ayarları `java-core-api/src/main/resources/application.yml` dosyasından kontrol edebilirsiniz.

## 2. Java Core API (Backend)
1. Terminalde projenin ana dizinine gidin: `cd java-core-api`
2. Projeyi derleyin ve başlatın: `mvn spring-boot:run`
3. API'nın `http://localhost:8080` adresinde çalıştığını doğrulayın.

## 3. Python Analysis Engine (Analiz Laboratuvarı)
1. Yeni bir terminal açın ve ana dizine gidin: `cd python-analysis-engine`
2. Bağımlılıkları yükleyin: `pip install -r requirements.txt`
3. Analiz motorunu başlatın: `python -m uvicorn app.main:app --host 0.0.0.0 --port 8000`

## 4. Mobil Uygulama (React Native & Expo)
1. `mobile/src/config/index.ts` dosyasındaki IP adresinin bilgisayarınızın yerel IP'si ile aynı olduğundan emin olun.
2. Terminalde `mobile` dizinine gidin.
3. `npm install` ile paketleri yükleyin.
4. `npx expo start` ile Expo'yu başlatın.
5. Telefonunuzdaki **Expo Go** uygulamasıyla QR kodu tarayın.

## 5. Otomatik Testlerin Çalıştırılması (Yeni)
QR kod tarama ihtiyacını azaltmak ve uygulama mantığını hızlıca doğrulamak için:
1. Terminalde `mobile` dizinine gidin.
2. `npm test` komutunu çalıştırın.
3. Bu komut, Jest ve React Native Testing Library kullanarak tüm birim (unit) ve bileşen (component) testlerini koşturacaktır.

### 💡 Önemli İpuçları
- **Aynı Ağ:** Bilgisayarınız ve telefonunuzun aynı Wi-Fi ağına bağlı olması gerekir.
- **Firewall:** Windows Defender'ın 8080 ve 8000 portlarını engellemediğinden emin olun.
- **Google Login:** Google ile girişin çalışması için Development Build kullanılması veya Jest üzerinde mock edilmesi önerilir.
