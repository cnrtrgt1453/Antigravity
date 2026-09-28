# CLAUDE.md

Bu dosya, bu repoda çalışırken AI asistanlarına rehberlik sağlar.

## Proje Genel Bakış

FinanceUP (Borsa Analiz), BIST (Borsa İstanbul) piyasa analizi ve sanal alım-satım/portföy simülasyon platformudur. Mimari, **0 TL sunucu maliyeti** ve **minimum teknoloji karmaşası** prensibiyle sadeleştirilmiştir:

- **`supabase/`** — BaaS (Backend-as-a-Service): PostgreSQL, Row Level Security (RLS), Google SSO (Supabase Auth), otomatik profil/portföy tetikleyicisi ve atomik alım-satım RPC fonksiyonları (`buy_stock`, `sell_stock`).
- **`python-analysis-engine/`** — `yfinance` üzerinden OHLC verisi çeken, SMA50/SMA200 Golden/Dead Cross sinyallerini hesaplayan, sonuçları Supabase'e kaydeden ve anlık analiz endpoint'leri sunan FastAPI servisi.
- **`mobile-android/`** — Native Android istemcisi (Kotlin, Jetpack Compose, Hilt, Ktor). Veritabanı ve Auth işlemleri için doğrudan Supabase PostgREST ile, anlık teknik analiz ve grafikler için Python FastAPI ile konuşur.
- *(Önceki `java-core-api` ve `mobile-legacy` servisleri mimariden tamamen kaldırılmıştır).*

## Servisleri Yerelde Çalıştırma

**Python Analiz Motoru** (port 8000):
```powershell
cd python-analysis-engine
py -m venv .venv; .\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```
Swagger UI: `http://localhost:8000/docs`.

**Mobil (Android)**:
```powershell
cd mobile-android
.\gradlew installDebug
```

## Veritabanı (Supabase) Kurulumu
1. Supabase SQL Editor'de `supabase/schema.sql` dosyasını çalıştırın (tablolar, RLS ve `buy_stock`/`sell_stock` fonksiyonları oluşturulur).
2. `supabase/seed_stocks.sql` dosyasını çalıştırarak 300+ BIST hissesini yükleyin.
3. Proje URL ve anon key değerlerini `python-analysis-engine/.env` ve `mobile-android/app/src/main/java/com/antigravity/mobile/data/config/SupabaseConfig.kt` dosyalarına tanımlayın.

## Testler

**Python** (pytest):
```powershell
pytest
```
**Mobil**:
```powershell
.\gradlew testDebugUnitTest
```
