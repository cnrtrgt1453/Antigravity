import yfinance as yf
import pandas as pd
import logging

logger = logging.getLogger(__name__)

def fetch_historical_data(ticker: str, period: str = "1y", interval: str = "1d") -> pd.DataFrame:
    """
    Fetches historical data for a given ticker from yfinance.
    Assumes interval is daily.
    Returns a pandas DataFrame.
    """
    try:
        logger.info(f"Fetching data for {ticker}...")
        stock = yf.Ticker(ticker)
        df = stock.history(period=period, interval=interval)
        return df
    except Exception as e:
        logger.error(f"Error fetching data for {ticker}: {e}")
        return pd.DataFrame()

# Known BIST 100 tickers can be listed here, or dynamically fetched. We will define a small subset for demonstration if needed.
# For BIST stocks, we append '.IS'. For Gold, 'GC=F' or 'XAUUSD=X'. For Silver, 'SI=F' or 'XAGUSD=X'.
