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
            
        # Kapanış fiyatları var mı kontrol et
        if 'Close' not in df.columns:
            return {
                "ticker": ticker,
                "signal": "INVALID_DATA",
                "color": "gray",
                "message": "Geçerli fiyat verisi (Close) bulunamadı.",
                "cross_date": None,
                "current_price": None
            }

        price = df['Close'].iloc[-1]
        
        # Hareketli Ortalamaları Hesapla
        df['SMA_Short'] = df['Close'].rolling(window=self.short_window).mean()
        df['SMA_Long'] = df['Close'].rolling(window=self.long_window).mean()

        # Önceki günün değerleri
        prev_short = df['SMA_Short'].iloc[-2]
        prev_long = df['SMA_Long'].iloc[-2]
        
        # Bugünün değerleri
        curr_short = df['SMA_Short'].iloc[-1]
        curr_long = df['SMA_Long'].iloc[-1]
        
        cross_date = str(df.index[-1].date())

        # Golden Cross (50 günlük ortalama, 200 günlüğü yukarı kestiğinde)
        if prev_short <= prev_long and curr_short > curr_long:
            return {
                "ticker": ticker,
                "signal": "GOLDEN_CROSS",
                "color": "green",
                "message": "🔥 GOLDEN CROSS TESPİT EDİLDİ! Yükseliş trendi başlıyor olabilir. 🔥",
                "cross_date": cross_date,
                "current_price": float(price)
            }
            
        # Dead Cross (50 günlük ortalama, 200 günlüğü aşağı kestiğinde)
        elif prev_short >= prev_long and curr_short < curr_long:
            return {
                "ticker": ticker,
                "signal": "DEAD_CROSS",
                "color": "red",
                "message": "⚠️ DEAD CROSS TESPİT EDİLDİ! Düşüş trendi başlayabilir. ⚠️",
                "cross_date": cross_date,
                "current_price": float(price)
            }

        return {
            "ticker": ticker,
            "signal": "NO_SIGNAL",
            "color": "gray",
            "message": "Şu an için önemli bir sinyal yok.",
            "cross_date": cross_date,
            "current_price": float(price)
        }
