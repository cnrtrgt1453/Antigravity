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

#### A. pgAdmin (Görsel Arayüz) ile En Detaylı Anlatım:
1. Bilgisayarınızda **pgAdmin 4** uygulamasını başlatın.
2. Eğer sol taraftaki **Servers** altında bir şey görmüyorsanız, önce bilgisayarınızdaki yüklü PostgreSQL'i buraya tanıtmanız gerekir:
   - **Servers** üzerine sağ tıklayın -> **Register** -> **Server...**
   - **General** sekmesinde, **Name** kısmına istediğiniz bir isim yazın (Örn: `LocalDB`).
   - **Connection** sekmesine geçin:
     - **Host name/address:** `localhost` yazın.
     - **Port:** `5432` kalsın.
     - **Username:** `postgres` kalsın.
     - **Password:** Kurulumda belirlediğiniz şifreyi yazın.
     - **Save password?** seçeneğini işaretleyin.
   - **Save** butonuna basın.
3. Artık solda `LocalDB` (veya verdiğiniz isim) görünecek. Yanındaki `>` işaretine tıklayın.
4. Altındaki **Databases** yazısına sağ tıklayın.
5. **Create** -> **Database...** yolunu izleyin.
6. Açılan pencerede **Database** kutucuğuna aynen şu ismi yazın: `borsa_db`
7. Alt taraftaki **Save** butonuna tıklayın.
8. Listenin içine `borsa_db` isminin geldiğini gördüğünüzde işlem tamamdır!

#### B. psql (Komut Satırı) ile En Detaylı Anlatım:
1. Windows arama çubuğuna `SQL Shell (psql)` yazın ve açın.
2. Karşınıza gelen `Server [localhost]:`, `Database [postgres]:`, `Port [5432]:` gibi sorulara hiçbir şey yazmadan **Enter** tuşuna basarak geçin.
3. `Password for user postgres:` kısmına kurulumda belirlediğiniz şifreyi yazın (Yazarken karakterler ekranda gözükmez, bu normaldir) ve **Enter**'a basın.
4. Şöyle bir ekran görmelisiniz: `postgres=#`
5. Buraya şu komutu yazın ve sonundaki noktalı virgüle dikkat edin:
   ```sql
   CREATE DATABASE borsa_db;
   ```
6. Ekranda `CREATE DATABASE` yazısını gördüyseniz başardınız!
7. Çıkmak için `\q` yazıp **Enter**'a basın.

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

### 🔑 PostgreSQL Şifresini Hatırlamıyorsanız (Sıfırlama):
Eğer kurulum şifresini unuttuysanız şu adımları izleyerek şifresiz giriş yapıp yeni şifre belirleyebilirsiniz:

1. **Dosyayı Bulun:** `C:\Program Files\PostgreSQL\17\data\pg_hba.conf` dosyasını bulun (17 yoksa 14'e bakın).
2. **Düzenleyin:** Dosyayı **Not Defteri** ile (Yönetici olarak açarak) açın.
3. **Değiştirin:** En alttaki satırlarda `scram-sha-256` (veya `md5`) yazan yerleri geçici olarak `trust` yapın.
4. **Servisi Yeniden Başlatın:** Görev Yöneticisi'nden PostgreSQL servisini durdurup tekrar başlatın.
5. **Şifresiz Bağlanın:** `SQL Shell (psql)` açın, sadece Enter'lara basın. Şifre sormayacaktır.
6. **Yeni Şifre Koyun:** Şu komutu çalıştırın:
   ```sql
   ALTER USER postgres WITH PASSWORD 'yeni_sifreniz';
   ```
7. **Geri Alın:** `pg_hba.conf` dosyasındaki `trust` yazılarını eski haline (`scram-sha-256`) getirin ve servisi tekrar başlatın.
