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

### B. Google Girişi (Firebase Google Sign-In)
1. **Firebase Console** (veya Google Cloud Console) üzerinden yeni bir proje (veya mevcut projenizi) açın.
2. Projeye Android uygulamasını ekleyin. Paket adını `com.antigravity.mobile` olarak tanımlayın.
3. Uygulama **SHA-1** parmak izinizi kaydedin.
4. Kayıt işlemi tamamlandıktan sonra Firebase panelinin size verdiği **`google-services.json`** adlı dosyayı indirin.
5. İndirdiğiniz bu dosyayı `mobile-android/app/` klasörünün içine kopyalayın.

## 2. API ve Backend Bağlantıları (Environment Variables)
Uygulamanız canlı ortama çıktığında, geliştirme sürecinde kullanılan `localhost` veya yerel IP adresleri erişilemez olacaktır.
1. `mobile-android/app/build.gradle.kts` veya `BuildConfig` üzerinden canlı sunucu (VPS, AWS vb.) URL adreslerinizi yapılandırın.

## 3. Canlı Derleme ve Yayınlama (Production Release Build)
Tüm ayarlarınızı doğruladıktan sonra Google Play Store için AAB (Android App Bundle) veya imzalı APK çıktısı alın.
```bash
cd mobile-android
.\gradlew bundleRelease
```
Play Store'a **APK dosyası yerine her zaman AAB dosyası** (`app-release.aab`) yüklemeniz gerektiğini unutmayın.

> **Son Kontrol:** Her şeyden eminseniz Firebase panellerinden sosyal girişlerinizi "Live" moduna almayı unutmayın. Aksi takdirde uygulamanızın Google girişleri sadece geliştirici hesaplarına açık kalır.
