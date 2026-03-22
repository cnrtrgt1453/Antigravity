# Canlı Ortam (Production) Öncesi Yapılması Gerekenler

Bu doküman, uygulamanın marketlere (Google Play Store vb.) veya gerçek kullanıcılara açılmadan önce tamamlanması gereken kritik entegrasyon, güvenlik ve altyapı adımlarını içermektedir.

## 1. Sosyal Giriş (Social SSO) Entegrasyonlarının Gerçekleştirilmesi
Mobil uygulamada Google ve Facebook ile giriş yapabilmek için `app.json` dosyasına eklenen geçici (dummy) değerler, uygulamanın kendi gerçek kimlikleriyle (Credentials) değiştirilmelidir.

### A. Facebook (Meta) Girişi (react-native-fbsdk-next)
1. **Meta for Developers** (developers.facebook.com) portali üzerinden yeni bir uygulama oluşturun.
2. Uygulama ayarlarına gidin ve platform olarak **Android** ekleyin.
3. Android paket kodunu (Package Name) girin: `com.antigravity.mobile`
4. EAS'in ürettiği şifreleme anahtarınızı (Key Hash / Base64 formatlı SHA-1) bu ekrandaki listeye ekleyin.
   > **Not:** Key Hash'i almak için terminalinizde `eas credentials` komutunu kullanıp uygulamaya tanımlı Android Keystore şifrelerinizi görüntüleyebilirsiniz.
5. Meta portalinden **App ID** (Uygulama Kimliği) ve **Client Token** (İstemci Jetonu) değerlerini kopyalayın.
6. Projedeki `mobile/app.json` dosyasını açarak `plugins` dizisindeki Facebook bölümünü bulun ve kendi ID'lerinizle değiştirin:
   ```json
   "plugins": [
     [
       "react-native-fbsdk-next",
       {
         "appID": "BURAYA_GERCEK_APP_ID_GELECEK",
         "clientToken": "BURAYA_GERCEK_CLIENT_TOKEN_GELECEK",
         "displayName": "Borsa Analiz",
         "scheme": "fbBURAYA_GERCEK_APP_ID_GELECEK"
       }
     ]
   ]
   ```

### B. Google Girişi (@react-native-google-signin)
1. **Firebase Console** (veya Google Cloud Console) üzerinden yeni bir proje (veya mevcut projenizi) açın.
2. Projeye Android uygulamasını ekleyin. Paket adını `com.antigravity.mobile` olarak tanımlayın.
3. EAS'in ürettiği **SHA-1** parmak izinizi kaydedin (`eas credentials` üzerinden görülebilir).
4. Kayıt işlemi tamamlandıktan sonra Firebase panelinin size verdiği **`google-services.json`** adlı dosyayı indirin.
5. İndirdiğiniz bu dosyayı projedeki kök dizine (yani `mobile/` klasörünün tam içine) kopyalayın.
6. `app.json` içerisinde Android ayarlarının olduğu kısma bu dosyanın yolunu aşağıdaki gibi tanımlayın:
   ```json
   "android": {
     "package": "com.antigravity.mobile",
     "googleServicesFile": "./google-services.json"
   }
   ```

## 2. API ve Backend Bağlantıları (Environment Variables)
Uygulamanız canlı ortama çıktığında, geliştirme sürecinde kullanılan `localhost` veya ev içi Wi-Fi IP adresleri erişilemez olacaktır.
1. `mobile/update-ip.js` gibi yerel test süreçlerine yarayan otomatizasyon dosyaları yerine `.env` üzerinden gerçek internet sunucunuzun (VPS, VDS, AWS, Heroku vb.) adreslerini kullanmalısınız.
2. Expo üzerinde derleme yaparken EAS ortamlarına `.env` şifrelerinizi `eas.json` dosyanızda belirtebilir veya Expo web sitesi üzerinden "Secrets" olarak kaydedebilirsiniz.

## 3. Yeni Derleme ve Test (Pre-Production Build)
Tüm ayarlarınızı gerçek versiyonlarıyla değiştirdikten sonra canlı sürüm hatası yapıp yapmadığınızı test etmek için bir kez AAB/APK oluşturun.
```bash
eas build -p android --profile production
```
Play Store'a **APK dosyası yerine her zaman AAB dosyası** (Android App Bundle) yüklemeniz gerektiğini unutmayın. `eas.json` içindeki `"production"` build türü bu işlemi otomatik olarak ayarlamaktadır.

> **Son Kontrol:** Her şeyden eminseniz Firebase ve Facebook panellerinden sosyal girişlerinizi "Test/Development" modundan "Live" veya "Production" moduna almayı (Onaylamayı) unutmayın. Aksi takdirde uygulamanızın Google ve Facebook girişleri sadece geliştirici hesaplarına açık kalır.
