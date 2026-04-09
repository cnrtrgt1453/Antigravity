import pytest
import pandas as pd
from app.services.analysis_strategy import GoldenCrossStrategy

class TestGoldenCrossStrategy:
    
    def test_analyze_not_enough_data(self):
        strategy = GoldenCrossStrategy(short_window=50, long_window=200)
        # Sadece 100 günlük veri
        df = pd.DataFrame({'Close': [100.0] * 100})
        
        result = strategy.analyze(df, "TEST")
        
        assert result["signal"] == "NOT_ENOUGH_DATA"
        assert "200 günlük veri bulunamadı" in result["message"]

    def test_analyze_invalid_data(self):
        strategy = GoldenCrossStrategy()
        # 'Close' sütunu yok
        df = pd.DataFrame({'Price': [100.0] * 250})
        
        result = strategy.analyze(df, "TEST")
        
        assert result["signal"] == "INVALID_DATA"
        assert "Close" in result["message"]

    def test_analyze_golden_cross_detection(self, golden_cross_dataframe):
        strategy = GoldenCrossStrategy()
        result = strategy.analyze(golden_cross_dataframe, "THYAO")
        
        assert result["signal"] == "GOLDEN_CROSS"
        assert result["current_price"] == 200.0
        assert result["sma50"] is not None
        assert result["sma200"] is not None
        # Golden cross'ta SMA50 > SMA200 olmalı
        assert result["sma50"] > result["sma200"]

    def test_analyze_dead_cross_detection(self, dead_cross_dataframe):
        strategy = GoldenCrossStrategy()
        result = strategy.analyze(dead_cross_dataframe, "ASELS")
        
        assert result["signal"] == "DEAD_CROSS"
        assert result["current_price"] == 50.0
        assert result["sma50"] < result["sma200"]

    def test_analyze_no_signal(self, sample_dataframe):
        strategy = GoldenCrossStrategy()
        # Sample dataframe np.linspace(100, 200) yani sürekli artış eğiliminde
        # Bu durumda sma50 hep sma200'den büyük olacaktır (başlangıç kesişimi hariç)
        # Ama valid_df (dropna sonrası) sma50 > sma200 ile başlar ve öyle biterse "any kesişim" olmayabilir.
        
        # Test için sabit bir fiyat dizisi verelim (sabit 100)
        df_static = pd.DataFrame({'Close': [100.0] * 300})
        result = strategy.analyze(df_static, "STATIC")
        
        # Sürekli 100 ise fark hep 0'dır. (diff > 0) & (diff.shift(1) <= 0) sağlanmaz.
        assert result["signal"] == "NO_SIGNAL"
