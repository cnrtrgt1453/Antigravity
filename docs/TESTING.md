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
2. Bağımlılıkları yükleyin: `py -m pip install -r requirements.txt`
3. Analiz motorunu başlatın: `py -m uvicorn app.main:app --host 0.0.0.0 --port 8000`

## 4. Mobil Uygulama (React Native & Development Build) 📱

Google ile Giriş gibi native (yerel) özelliklerin çalışması için **Expo Go kullanılamaz.** Bunun yerine bir "Development Build" (Geliştirme Yapısı) kurmanız gerekir.

### Adım 1: IP Adresini Güncelleyin
`mobile/src/config/index.ts` dosyasındaki `JAVA_API_URL` ve `PYTHON_API_URL` adreslerinin bilgisayarınızın güncel yerel IP'si (örn: `192.168.1.132`) ile aynı olduğundan emin olun.

### Adım 2: Fiziksel Cihazı Bağlayın
1. Telefonunuzda **Geliştirici Seçenekleri**'ni ve **USB Hata Ayıklama**'yı (USB Debugging) aktif edin.
2. Telefonu USB ile bilgisayara bağlayın.
3. Bilgisayarda ve telefonda çıkan "Hata ayıklamaya izin verilsin mi?" uyarılarına **Evet** deyin.

### Adım 3: Uygulamayı Telefona Kurun
Üçüncü bir terminal açın ve şu komutları çalıştırın:
```powershell
cd mobile
npx expo run:android --no-build-cache
```
*Not: Bu işlem ilk seferde birkaç dakika sürebilir. İşlem bittiğinde uygulama telefonunuzda otomatik olarak açılacaktır.*

## 5. Tam Sistem Testi (Dashboard)
Uygulamanın tam kapasite çalışması için şu 3 terminalin de **aynı anda** açık ve çalışır olduğundan emin olun:
- **Terminal 1:** Java Backend (Port 8080)
- **Terminal 2:** Python Backend (Port 8000)
- **Terminal 3:** Mobil Uygulama (Geliştirme Sunucusu)

## 6. Otomatik Testlerin Çalıştırılması
Birim testlerini hızlıca doğrulamak için:
```powershell
cd mobile
npm test
```

### 💡 Kritik İpuçları
- **Aynı Ağ:** Telefon ve bilgisayar **aynı Wi-Fi** ağına bağlı olmalıdır.
- **Python Hatası:** Eğer `python` komutu hata verirse `python3` veya `py` komutlarını deneyin.
- **Google Login:** `DEVELOPER_ERROR` alırsanız, Firebase'deki SHA-1 parmak izinin projenin içindeki `debug.keystore` ile eşleştiğinden emin olun.
