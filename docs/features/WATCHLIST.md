# Özellik: Takip Listesi (Watchlist)

Takip Listesi özelliği, kullanıcıların belirli hisse sembollerini takip etmelerine ve haber akışlarında kişiselleştirilmiş sonuçlar almalarına olanak tanır.

## Teknik Detaylar

### Backend Yapısı
- **Varlık (Entity):** `Watchlist`
- **İlişki:** `User` ile Çok-tan-Bire (Many-to-One).
- **Veritabanı:** PostgreSQL `watchlist` tablosu.
- **Optimizasyon:** `(user_id, stock_symbol)` üzerinde benzersiz kompozit indeks.

### Temel İşlemler
- `POST /api/v1/watchlist/add`: Bir sembol ekler.
- `DELETE /api/v1/watchlist/remove`: Bir sembolü siler.
- `GET /api/v1/watchlist/list`: Kullanıcının sembollerini listeler.

## SOLID Prensipleri
- **SRP (Tek Sorumluluk):** Takip listesi mantığı `User` varlığından izole edilmiştir ve özel bir `WatchlistService` üzerinden yönetilir.
- **İlişki:** Ayrılmış (decoupled) tasarım, gelecekteki ölçeklendirmelere (örn. fiyat alarmları) olanak tanır.
