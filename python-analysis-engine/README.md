# Python Analiz Motoru (Analysis Engine)

Bu modül, `yfinance` kullanarak hisse senedi verilerini (BIST, Altın, Gümüş) çeken ve Hareketli Ortalama (SMA50/200) Golden ve Dead Cross formasyonlarını tespit eden Python tabanlı analiz motorudur.

## Ön Koşullar
- Python 3.9+
- pip

## Hızlı Başlangıç

1. Depoyu klonlayın ve bu dizine gidin.
2. Sanal ortam (virtual environment) oluşturun ve aktifleştirin:
   ```bash
   py -m venv .venv
   .\.venv\Scripts\Activate.ps1
   ```
   *(Eğer Windows'ta script çalıştırma yetkisi hatası alırsanız, PowerShell'i Yönetici olarak açıp `Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser` komutunu çalıştırın veya doğrudan `.venv` içindeki `python.exe`yi kullanın.)*

3. Bağımlılıkları yükleyin:
   ```bash
   pip install -r requirements.txt
   ```

4. FastAPI geliştirme sunucusunu başlatın:
   ```bash
   uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
   ```

5. Uç noktaları (endpoints) test etmek için tarayıcınızda Swagger UI'yı açın:
   - http://localhost:8000/docs
    
## Zamanlanmış Görevler (Scheduled Jobs)
Analiz motoru, piyasayı **her Pazartesi saat 07:00'de** otomatik olarak tarayacak ve BIST hisseleri, Altın ve Gümüş için sonuçları üretecek şekilde yapılandırılmıştır.
