import pytest
import pandas as pd
import numpy as np
from datetime import datetime, timedelta

@pytest.fixture
def sample_dataframe():
    """Genel kullanım için 250 günlük örnek veri seti."""
    dates = [datetime.now() - timedelta(days=i) for i in range(250)]
    dates.reverse()
    
    data = {
        'Close': np.linspace(100, 200, 250) + np.random.normal(0, 2, 250)
    }
    df = pd.DataFrame(data, index=pd.to_datetime(dates))
    return df

@pytest.fixture
def golden_cross_dataframe():
    """Golden Cross (Altın Kesişim) senaryosu için veri seti.
    Kısa vadeli ortalamanın (50) uzun vadeli ortalamayı (200) yukarı kestiği durum.
    """
    dates = [datetime.now() - timedelta(days=i) for i in range(300)]
    dates.reverse()
    
    # İlk 200 gün fiyat düşük (örneğin 100), sonra sert yükseliş (200)
    prices = [100.0] * 200 + [200.0] * 100
    
    df = pd.DataFrame({'Close': prices}, index=pd.to_datetime(dates))
    return df

@pytest.fixture
def dead_cross_dataframe():
    """Dead Cross (Ölüm Kesişimi) senaryosu için veri seti.
    Kısa vadeli ortalamanın uzun vadeyi aşağı kestiği durum.
    """
    dates = [datetime.now() - timedelta(days=i) for i in range(300)]
    dates.reverse()
    
    # İlk 200 gün fiyat yüksek (örneğin 200), sonra sert düşüş (50)
    prices = [200.0] * 200 + [50.0] * 100
    
    df = pd.DataFrame({'Close': prices}, index=pd.to_datetime(dates))
    return df
