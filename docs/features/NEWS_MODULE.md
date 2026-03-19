# Özellik: Haber Modülü (News)

Haber modülü, takip edilen hisseler için zamanında finansal bilgiler ve kişiselleştirilmiş analiz raporları sunar.

## Mimari

### Zamanlama (Scheduling)
- **Sıklık:** Hafta içi saat 18:15 (Cron: `0 15 18 * * MON-FRI`).
- **Servis:** `NewsScheduler`.
- **Mantık:** Haber sağlayıcılardan (örn. KAP) gelen benzersiz haber UID'lerine göre artımlı güncellemeler.

### Haber Kişiselleştirme
- **Kişiselleştirilmiş Akış:** Kullanıcının `Takip Listesi`ne (Watchlist) göre haberleri filtrelemek için SQL JOIN yapılarını kullanır.
- **Frontend Arayüzü:**
    - **Sekme Değiştirme:** "Takip Listem" ve "Tüm Haberler" arasında geçiş.
    - **Sembol Filtreleri:** Kullanıcı listesindeki belirli hisseler için yatay filtreler.
    - **Sıralama:** En Yeni/En Eski arasında geçiş.
    - **Sayfalama:** Sonsuz kaydırma (infinite scroll) ile optimize edilmiştir.

## Haftalık Analiz Raporu
- **Aktivasyon:** Sadece Pazartesi günleri.
- **İstisna:** Yeni kullanıcılar ilk raporlarına herhangi bir gün erişebilir.
- **İçerik:** Takip listesindeki hisselerin son 30 gündeki haber hareketliliğini özetler.
- **Kalıcılık:** Haftada bir kuralını uygulamak için `User` tablosunda `lastReportDate` bilgisini takip eder.
