# 🧪 Test ve Başlatma Rehberi

Bu doküman, Antigravity projesini yerel ortamınızda ve mobil cihazınızda nasıl çalıştıracağınızı adım adım açıklar.

## 1. Hazırlık: Veritabanı
Proje **PostgreSQL** kullanmaktadır.
- Yerelinizde bir `borsa_db` veritabanı oluşturun.
- Kullanıcı adı ve şifrenin `postgres/postgres` (veya kendi ayarlarınız) olduğundan emin olun.
- Ayarları `java-core-api/src/main/resources/application.yml` dosyasından kontrol edebilirsiniz.

## 2. Java Core API (Backend)
1. Terminalde projenin ana dizinine gidin.
2. `cd java-core-api`
3. Projeyi derleyin ve başlatın:
   ```bash
   mvn spring-boot:run
   ```
4. API'nın `http://localhost:8080` adresinde çalıştığını doğrulayın.

## 3. Python Analysis Engine (Analiz Laboratuvarı)
1. Yeni bir terminal açın ve ana dizine gidin.
2. `cd python-analysis-engine`
3. (İsteğe bağlı) Sanal ortam oluşturun ve aktif edin.
4. Bağımlılıkları yükleyin:
   ```bash
   # Eğer pip hata verirse şu komutları deneyin:
   py -m pip install -r requirements.txt
   # Veya
   python -m pip install -r requirements.txt
   ```
5. Analiz motorunu, mobil cihazınızın erişebilmesi için tüm arayüzlerde (`0.0.0.0`) başlatın:
   ```bash
   # Eğer uvicorn hata verirse:
   py -m uvicorn app.main:app --host 0.0.0.0 --port 8000
   ```

## 4. Mobil Uygulama (React Native & Expo)
Mobil cihazınızın backend servislerine ulaşabilmesi için bilgisayarınızın yerel IP adresi kullanılmalıdır.
1. `mobile/src/config/index.ts` dosyasındaki IP adresinin güncel olduğundan emin olun:
   - Mevcut Ayar: `192.168.1.176`
2. Terminalde ana dizine gidin.
3. `cd mobile`
4. Bağımlılıkları yükleyin:
   ```bash
   npm install
   ```
5. Expo'yu başlatın:
   ```bash
   npx expo start
   ```
6. Bilgisayar ekranında çıkan **QR kodu** telefonunuzdaki **Expo Go** uygulamasıyla tarayın.

### 💡 Önemli İpuçları
- **Aynı Ağ:** Bilgisayarınız ve telefonunuzun kesinlikle aynı Wi-Fi ağına bağlı olması gerekir.
- **Firewall:** Windows Defender veya Antivirüs yazılımınızın 8080 ve 8000 portlarına gelen bağlantıları engellemediğinden emin olun.
- **PowerShell Betik Hatası:** Eğer `npm` veya `npx` çalıştırırken "running scripts is disabled" hatası alırsanız, PowerShell'i yönetici olarak açıp şu komutu çalıştırın:
  ```powershell
  Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
  ```
- **Google Login:** Google ile girişin çalışması için Google Cloud Console üzerinden aldığınız `webClientId` değerlerinin `.env` dosyasında tanımlı olması gerekir.
