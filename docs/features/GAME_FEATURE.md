# Özellik: Oyun ve Sanal Portföy

Antigravity mobil uygulamasındaki Oyun bölümü, kullanıcıların gerçek zamanlı verilerle sanal borsa deneyimi yaşamasını sağlar. Kullanıcılar başlangıç bakiyesi ile hisse alıp satabilir, portföylerini takip edebilir ve işlem geçmişlerini inceleyebilirler.

## Temel Bileşenler

### 1. Sanal Portföy (Virtual Portfolio)
- **Bakiye (Balance):** Kullanıcının işlem yapabileceği sanal nakit miktarı.
- **Varlıklar (Holdings):** Sahip olunan hisseler, adetleri ve ortalama maliyetleri.
- **Kar/Zarar Takibi:** Anlık piyasa fiyatları ile maliyet arasındaki farkın yüzde ve tutar cinsinden hesaplanması.

### 2. Alım-Satım İşlemleri (Trading)
- **Hisse Al:** İzleme listesindeki veya piyasadaki bir hisseyi anlık fiyattan satın alma.
- **Hisse Sat:** Portföydeki bir hisseyi anlık fiyattan satma.
- **Komisyon:** Her işlemde %1 oranında sanal komisyon uygulanır.

### 3. İşlem Geçmişi (Trade History)
- Yapılan tüm alım ve satım işlemlerinin tarih, miktar, fiyat ve toplam tutar bilgileriyle listelenmesi.

### 4. İzleme Listesi Entegrasyonu
- "Markets" ekranından takibe alınan hisseler, Oyun bölümünde "Hızlı Al/Sat" için listelenir.

## Teknik Detaylar

### Durum Yönetimi (State Management)
Uygulama, `useGameStore` (Zustand) üzerinden aşağıdaki verileri yönetir:
- `portfolio`: Mevcut bakiye ve hisse listesi.
- `history`: Tüm geçmiş işlemler.
- `watchlist`: Takip edilen hisse sembolleri.

### API Etkileşimi
- **Java Backend:** Portföy verilerinin kalıcı olarak saklanması ve işlem doğrulamaları (bakiye kontrolü vb.) için kullanılır.
- **Python Engine:** Anlık fiyat verilerinin çekilmesi ve kar/zarar hesaplamaları için kullanılır.

## Veri Yapısı (Interface)
```typescript
interface PortfolioItem {
  stockSymbol: string;
  quantity: number;
  averageCost: number;
}

interface TradeHistory {
  stockSymbol: string;
  type: 'BUY' | 'SELL';
  quantity: number;
  price: number;
  totalAmount: number;
  timestamp: string;
}
```
