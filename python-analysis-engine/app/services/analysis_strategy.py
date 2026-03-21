from typing import Protocol, Dict, Any, Optional
import pandas as pd
import numpy as np

class IAnalysisStrategy(Protocol):
    """Analiz stratejileri için ortak Interface."""
    def analyze(self, df: pd.DataFrame, ticker: str) -> Dict[str, Any]:
        ...

class GoldenCrossStrategy:
    """SMA50 ve SMA200 Kesişimi Analiz Stratejisi."""
    
    def __init__(self, short_window: int = 50, long_window: int = 200):
        self.short_window = short_window
        self.long_window = long_window
        
    def analyze(self, df: pd.DataFrame, ticker: str) -> Dict[str, Any]:
        if df is None or len(df) < self.long_window:
            return {
                "ticker": ticker,
                "signal": "NOT_ENOUGH_DATA",
                "color": "gray",
                "message": f"Bu enstrüman için en az {self.long_window} günlük veri bulunamadı.",
                "cross_date": None,
                "current_price": None
            }
            
        if 'Close' not in df.columns:
            return {
                "ticker": ticker,
                "signal": "INVALID_DATA",
                "color": "gray",
                "message": "Geçerli fiyat verisi (Close) bulunamadı.",
                "cross_date": None,
                "current_price": None
            }

        # Hareketli Ortalamaları Hesapla
        df['SMA_Short'] = df['Close'].rolling(window=self.short_window).mean()
        df['SMA_Long'] = df['Close'].rolling(window=self.long_window).mean()

        # Son 7 günü kontrol et (Kesişim bir gün önce olmuş olabilir)
        last_7_days = df.tail(8) # 8 gün alıyoruz çünkü fark (diff) için bir önceki güne de ihtiyacımız var
        
        signal = 'NO_SIGNAL'
        message = "Şu an için önemli bir sinyal yok."
        color = "gray"
        cross_date = None
        
        for i in range(1, len(last_7_days)):
            prev_row = last_7_days.iloc[i-1]
            curr_row = last_7_days.iloc[i]
            
            if pd.isna(prev_row['SMA_Long']) or pd.isna(curr_row['SMA_Long']):
                continue

            # Golden Cross
            if prev_row['SMA_Short'] <= prev_row['SMA_Long'] and curr_row['SMA_Short'] > curr_row['SMA_Long']:
                signal = 'GOLDEN_CROSS'
                message = "🔥 GOLDEN CROSS TESPİT EDİLDİ! Yükseliş trendi başlıyor olabilir. 🔥"
                color = "green"
                cross_date = str(curr_row.name.date())
            
            # Dead Cross
            elif prev_row['SMA_Short'] >= prev_row['SMA_Long'] and curr_row['SMA_Short'] < curr_row['SMA_Long']:
                signal = 'DEAD_CROSS'
                message = "⚠️ DEAD CROSS TESPİT EDİLDİ! Düşüş trendi başlayabilir. ⚠️"
                color = "red"
                cross_date = str(curr_row.name.date())

        # Golden Cross veya Dead Cross olmuşsa o günkü fiyatı da kaydedelim
        cross_price = None
        if cross_date:
             # Kesişim gününün fiyatını bul (Index tarihtir)
             try:
                 cross_price = float(df.loc[cross_date]['Close'])
             except:
                 cross_price = None

        return {
            "ticker": ticker,
            "signal": signal,
            "color": color,
            "message": message,
            "cross_date": cross_date,
            "current_price": float(df['Close'].iloc[-1]),
            "sma50": float(df['SMA_Short'].iloc[-1]) if not pd.isna(df['SMA_Short'].iloc[-1]) else None,
            "sma200": float(df['SMA_Long'].iloc[-1]) if not pd.isna(df['SMA_Long'].iloc[-1]) else None,
            "cross_price": cross_price
        }
