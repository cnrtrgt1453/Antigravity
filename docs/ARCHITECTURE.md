# FinanceUP - Proje Mimarisi

Bu belge, FinanceUP piyasa analizi ve sanal alım-satım platformunun güncel hibrit (Supabase BaaS + Python FastAPI + Android Native) mimarisini özetler.

## Mimari Prensipler
1. **0 TL Maliyet:** Tüm altyapı ücretsiz katmanlar (Supabase Free Tier, Render/Koyeb Free Tier) üzerinde çalışacak şekilde tasarlanmıştır.
2. **Minimum Teknoloji:** 3 bağımsız servis yerine yalnızca 1 istemci (Android) + 1 BaaS (Supabase) + 1 analitik motor (Python FastAPI) yapısına geçilmiştir. Java Spring Boot katmanı kaldırılarak bellek ve sunucu maliyeti sıfırlanmıştır.

---

## Teknoloji Yığını

### 1. BaaS & Veritabanı Katmanı (Supabase / PostgreSQL)
- **Rol:** Kimlik doğrulama, kullanıcı profilleri, portföy & bakiye yönetimi, takip listesi, haberler ve atomik alım-satım motoru.
- **Teknoloji:** PostgreSQL 15, Row Level Security (RLS), Supabase Auth (Google SSO), PL/pgSQL RPC.
- **Atomik İşlemler:** Alım ve satım işlemleri `buy_stock` ve `sell_stock` SQL RPC fonksiyonları içinde transaction garantisiyle yürütülür (%1 komisyon, anlık ortalama maliyet hesaplama, bakiye düşümü).

### 2. Analiz Motoru (Python FastAPI)
- **Rol:** BIST hisselerinin OHLC verilerini çekme, matematiksel teknik göstergeleri (SMA50, SMA200, Golden/Dead Cross) hesaplama ve anlık grafik endpoint'leri sunma.
- **Teknoloji:** Python 3.10+, FastAPI, Pandas, `yfinance`, `supabase-py`, APScheduler.
- **İletişim:** Hesaplanan sinyalleri doğrudan Supabase `market_signals` tablosuna ve hisse son fiyatlarını `stocks` tablosuna aktarır.

### 3. Mobil İstemci (Android Native)
- **Rol:** Kullanıcı arayüzü, canlı grafik gösterimi, takip listesi ve sanal borsa simülasyonu.
- **Teknoloji:** Kotlin, Jetpack Compose, Hilt (Dependency Injection), Ktor Client, Coroutines & Flow.
- **İletişim:** Veritabanı ve Auth işlemleri için doğrudan Supabase PostgREST REST API'si ile; anlık teknik grafik ve taramalar için Python FastAPI ile konuşur.

---

## Temel İş Akışları

```
[Android Native İstemci]
   │
   ├── (Auth & Profil) ──────────> [Supabase Auth]
   ├── (Portföy & Alım/Satım) ────> [Supabase RPC (buy_stock / sell_stock)]
   ├── (Takip Listesi & Hisseler) > [Supabase PostgREST]
   │
   └── (Anlık Grafik & Sinyal) ───> [Python FastAPI (yfinance + TA)]
                                            │
                                            └── (Sinyalleri Kaydet) ──> [Supabase DB]
```
