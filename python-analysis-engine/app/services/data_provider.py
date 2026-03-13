from typing import Protocol, Optional
import yfinance as yf
import pandas as pd
import logging

logger = logging.getLogger(__name__)

class IMarketDataProvider(Protocol):
    """Veri sağlayıcıları için Interface / Protocol sınıfı."""
    def fetch_historical_data(self, ticker: str, period: str = "2y", interval: str = "1d") -> Optional[pd.DataFrame]:
        ...

class YahooFinanceProvider:
    """IMarketDataProvider uygulayan Yahoo Finance nesnesi."""
    def fetch_historical_data(self, ticker: str, period: str = "2y", interval: str = "1d") -> Optional[pd.DataFrame]:
        try:
            stock = yf.Ticker(ticker)
            df = stock.history(period=period, interval=interval)
            
            if df.empty:
                logger.warning(f"No data found for {ticker}")
                return None
                
            return df
        except Exception as e:
            logger.error(f"Error fetching data for {ticker}: {e}")
            return None
