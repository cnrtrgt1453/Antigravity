# 📝 Test Senaryoları (Test Cases)

Bu doküman, Antigravity mobil uygulamasındaki ekranlar ve veri depoları (stores) için tanımlanan test senaryolarını detaylandırır.

## 1. Kimlik Doğrulama (Auth)
### useAuthStore (Birim Testi)
- [x] **Başlangıç Durumu:** Kullanıcı yok, yükleme kapalı, hata mesajı boş olmalıdır.
- [x] **Başarılı Giriş:** Geçerli bilgilerle giriş yapıldığında kullanıcı bilgileri store'a kaydedilmelidir.
- [x] **Hatalı Giriş:** Yanlış şifre/e-posta durumunda hata mesajı set edilmelidir.
- [x] **Çıkış (Logout):** Çıkış yapıldığında kullanıcı bilgileri temizlenmelidir.

### LoginScreen (Bileşen Testi)
- [x] **Arayüz Kontrolü:** E-posta, şifre alanları ve giriş butonu ekranda görünmelidir.
- [x] **Validasyon:** Boş bırakılan alanlar için uyarı verilmelidir.
- [x] **Giriş Butonu:** Tıklandığında `authStore` üzerindeki `login` fonksiyonu çağrılmalıdır.
- [x] **Kayıt Ol Yönlendirmesi:** "Kayıt Ol" butonuna basıldığında Register ekranına gidilmelidir.

## 2. Oyun ve Portföy (Game)
### useGameStore (Birim Testi)
- [x] **Portföy Getirme:** API'den gelen portföy verileri (bakiye, hisseler) doğru şekilde store'a aktarılmalıdır.
- [x] **Satın Alma:** Yeterli bakiye varsa hisse alımı yapılmalı ve bakiye düşmelidir.
- [x] **Yetersiz Bakiye:** Alım sırasında bakiye yetersizse hata döndürülmelidir.
- [x] **İzleme Listesi (Watchlist):** Listeye hisse ekleme ve çıkarma işlemleri store üzerinde doğrulanmalıdır.

### GameScreen (Bileşen Testi)
- [x] **Bakiye Gösterimi:** Kullanıcının güncel bakiyesi ekranda (örn. "15000.00 TL") doğru formatta görünmelidir.
- [x] **Boş Portföy:** Yatırım yoksa "Henüz bir yatırımınız bulunmuyor" mesajı görünmelidir.
- [x] **Hisse Listesi:** İzleme listesindeki hisselerin isimleri ve fiyatları kartlarda görünmelidir.
- [x] **Alım Butonu:** Hisse alım butonuna tıklandığında alım modülü tetiklenmelidir.

## 3. Ana Sayfa (Home)
### HomeScreen (Bileşen Testi)
- [x] **Kullanıcı Karşılama:** "Hoş geldin" mesajı ve kullanıcının adı ekranda doğru görünmelidir.
- [x] **Piyasa Özeti:** API'den gelen piyasa özeti verileri (BIST100, Altın, Gümüş) listelenmelidir.
- [x] **Sinyal Listeleri:** Golden ve Dead Cross sinyalleri farklı kategoriler altında listelenmelidir.
- [x] **Manuel Tarama:** "Şimdi Tara" butonu tıklandığında analiz motoruna istek gönderilmelidir.
- [x] **Bekleme Süresi (Cooldown):** Tarama butonunda kalan süre (örn. "2s 15d") doğru görünmelidir.
- [x] **Navigasyon:** Haberler butonuna tıklandığında Haberler ekranına yönlendirilmelidir.
- [x] **Çıkış:** Çıkış Yap butonuna basınca oturum sonlandırılmalıdır.

## 4. Geriye Kalan ve Yapılacak Testler (Pending)

### RegisterScreen (Kayıt Ekranı)
- [ ] **Ad/Soyad Validasyonu:** Boş bırakılamaz kontrolü.
- [ ] **E-posta Validasyonu:** Geçersiz format uyarısı.
- [ ] **Şifre Uyumu:** Şifre ve şifre tekrar alanlarının eşleşme kontrolü.
- [ ] **Kayıt İşlemi:** Başarılı kayıttan sonra ana sayfaya yönlendirme.

### MarketScreen (Piyasa Ekranı)
- [ ] **Hisse Listesi:** Tüm BIST hisselerinin listelenmesi.
- [ ] **Arama:** İsim veya sembole göre filtreleme.
- [ ] **İzleme Listesine Ekleme:** Kalp/Yıldız ikonuna tıklanınca `useGameStore`'un tetiklenmesi.

### NewsScreen (Haberler)
- [ ] **Haber Akışı:** Genel haberlerin listelenmesi.
- [ ] **Kişiselleştirme:** "Takip Listem" sekmesinin doğru filtreleme yapması.
- [ ] **Detay Görünümü:** Habere tıklandığında içeriğin açılması.

### SignalsScreen (Sinyaller)
- [ ] **Sinyal Kartları:** Golden Cross ve Dead Cross sinyallerinin renk kodlarıyla (Yeşil/Kırmızı) gösterimi.
- [ ] **Filtreleme:** Sinyal türüne göre filtreleme.
- [ ] **Tarih Bilgisi:** Sinyalin kaç gün önce oluştuğunun doğrulanması.

### TradeHistoryScreen (İşlem Geçmişi)
- [ ] **Kronolojik Sıra:** En son işlemlerin en üstte olması.
- [ ] **Alım/Satım Ayrımı:** İşlem türüne göre (Alış/Satış) etiketlerin doğru görünmesi.
- [ ] **Toplam Tutar:** Adet x Fiyat hesaplamasının doğru yansıması.

---
*Not: Mevcut test altyapısı (Jest/RNTL) yukarıdaki Pending (Bekleyen) ekranlar için de aynı şekilde uygulanacaktır.*
