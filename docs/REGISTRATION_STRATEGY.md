# Kullanıcı Kayıt, Sosyal Giriş ve Veri Toplama Stratejisi

Borsa Analiz uygulamamızda kullanıcıyı tanımak, ileride ona özelleştirilmiş analizler ve hisse önerileri veya bildirimler sunabilmek için stratejiktir. Kullanıcı tabanını büyütmek ve "Geniş kitlelere ulaşmak" hedefimiz doğrultusunda uyguladığımız Kayıt ve Veri Toplama stratejisi aşağıdadır.

## 1. Sosyal Giriş (Social Login) Stratejisi

Sosyal girişler (Google, Apple, Facebook), mobil uygulamalarda form doldurma zahmetini ortadan kaldırdığı için dönüşüm (conversion) oranlarını %40'a kadar artırabilmektedir.

### Seçilen Opsiyonlar
*   **Google ile Giriş (Öncelikli):** Android ekosisteminin doğası gereği ve en yaygın kullanılan e-posta sağlayıcısı olması nedeniyle ilk ve en önemli entegrasyonumuzdur.
*   **Apple ile Giriş (Zorunlu):** Uygulamamızı App Store'da (iOS) yayınlayacağımız için; Apple kuralları gereği (uygulamada herhangi bir sosyal giriş varsa) **Apple ile Giriş** seçeneğini koymak zorunludur.
*   **Facebook ile Giriş (Ertelendi):** Finansal/borsa gibi uygulamalara Facebook hesaplarını bağlama konusunda kullanıcıların çekimser kalabilmesi (Güvenlik algısı) nedeniyle MVP (İlk Çıkış) aşamasında eforu Google ve Apple'a harcanacaktır.

**Karar:** `E-posta/Şifre`, `Google ile Giriş` ve `Apple ile Giriş` desteklenecektir. Firebase Authentication ile yönetilecektir.

## 2. Hangi Verileri Topluyoruz? (Veri Analizi Odaklı)

Borsa Analiz uygulamamızın geleceği (örneğin Yapay Zeka ile hisse önerme, push bildirimleri optimize etme) düşünülerek sadece "anlamlı" veriler toplanacaktır.

| Veri Adı | Nereden Alınır? | Neden Tutuyoruz? (Analitik ve İş Değeri) |
| :--- | :--- | :--- |
| **Kullanıcı ID (UID)** | Firebase | Her kullanıcının veritabanındaki eşsiz anahtarı. |
| **E-posta Adresi** | Giriş Formu/Google | Uygulama dışı iletişim (Örn: Haftalık piyasa özeti bülteni) ve hesap kurtarma. |
| **Ad Soyad** | Giriş Formu/Google | Uygulama içi kişiselleştirme ("Hoş geldin Ahmet"). Birebir hitap, push bildirim tıklama oranını artırır. |
| **Profil Fotoğrafı URL** | Google/Apple | UI'da daha sıcak bir görünüm sağlamak için (Opsiyonel). |
| **Kayıt Tarihi** | Sistem | Kohort (Cohort) analizi için. Metrik ölçümleri. |
| **Son Giriş Tarihi** | Sistem | "Re-engagement" (Yeniden Etkileşim) stratejileri için. |

*Not: "Doğum tarihi", "Telefon Numarası" gibi ağır bilgiler MVP aşamasında İSTENMEYECEKTİR.*

## 3. Davranışsal Veriler (Hedeflenen)
Kayıt bilgilerinin ötesinde, kullanıcı uygulamayı kullanmaya başladığında şu davranışları loglamayı (takip etmeyi) hedefliyoruz:
*   **Favori Hisseleri (Watchlist):** Hangi hisseleri veya endeksleri takip ediyor?
*   **Tercih Ettiği Kesişim Tipi:** Hangi grafikte daha çok analiz inceliyor? (Günlük, 4 Saatlik vb.)
*   *Amaç:* Kişiye özel bildirim atmak. Örn: "Favori hissen THYAO'da bugün Dead Cross oluştu!"
