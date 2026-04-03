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

        # SMA'ların boş (NaN) olduğu ilk 200 günü dışarıda bırakıp kalan valid veriler üzerinden işlem yapıyoruz
        valid_df = df.dropna(subset=['SMA_Short', 'SMA_Long'])
        
        signal = 'NO_SIGNAL'
        message = "Son 2 yılda herhangi bir kesişim bulunamadı."
        color = "gray"
        cross_date = None
        cross_price = None

        if not valid_df.empty:
            diff = valid_df['SMA_Short'] - valid_df['SMA_Long']
            
            # Kesişim noktalarını bul:
            # Golden Cross: Diff şu an pozitif ve bir önceki adımda negatif/sıfır ise
            golden_crosses = (diff > 0) & (diff.shift(1) <= 0)
            # Dead Cross: Diff şu an negatif ve bir önceki adımda pozitif/sıfır ise
            dead_crosses = (diff < 0) & (diff.shift(1) >= 0)
            
            # Tüm kesişimleri içeren dataframe
            all_crosses = valid_df[golden_crosses | dead_crosses]
            
            if not all_crosses.empty:
                # Sadece EN SON gerçekleşen kesişime odaklanıyoruz
                last_cross = all_crosses.iloc[-1]
                last_cross_idx = all_crosses.index[-1]
                
                cross_date = str(last_cross_idx.date() if hasattr(last_cross_idx, 'date') else last_cross_idx)
                
                try:
                    cross_price = float(last_cross['Close'])
                except:
                    cross_price = None

                # En son kesişim hangisi?
                if last_cross['SMA_Short'] > last_cross['SMA_Long']:
                    signal = 'GOLDEN_CROSS'
                    message = "🔥 Son 2 yıl içindeki en güncel kesişim: GOLDEN CROSS 🔥"
                    color = "green"
                else:
                    signal = 'DEAD_CROSS'
                    message = "⚠️ Son 2 yıl içindeki en güncel kesişim: DEAD CROSS ⚠️"
                    color = "red"

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
