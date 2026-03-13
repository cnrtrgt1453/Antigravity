import pandas as pd
from typing import Dict, Any, Optional

def analyze_cross(df: pd.DataFrame, ticker: str) -> Dict[str, Any]:
    """
    Analyzes the DataFrame for Golden or Dead Cross within the last 7 days.
    """
    if df.empty or len(df) < 200:
        return {
            "ticker": ticker,
            "signal": "NOT_ENOUGH_DATA",
            "message": "En az 200 gunluk veri gerekiyor."
        }
    
    # Calculate SMA50 and SMA200
    df['SMA50'] = df['Close'].rolling(window=50).mean()
    df['SMA200'] = df['Close'].rolling(window=200).mean()
    
    # Drop rows without SMA200
    df = df.dropna(subset=['SMA200'])
    
    # Get the last 7 days
    last_7_days = df.tail(7)
    
    signal = 'NO_SIGNAL'
    message = "Son 7 gün içinde kesişim olmadı."
    color = "GRAY"
    cross_date = None
    
    # Iterate through the last 7 days to find if lines crossed
    for i in range(1, len(last_7_days)):
        prev_day = last_7_days.iloc[i-1]
        curr_day = last_7_days.iloc[i]
        
        # Golden Cross Check: SMA50 was <= SMA200, and is now > SMA200
        if prev_day['SMA50'] <= prev_day['SMA200'] and curr_day['SMA50'] > curr_day['SMA200']:
            signal = 'GOLDEN_CROSS'
            message = "Golden Cross! SMA50, SMA200'ün üstüne çıktı."
            color = "GREEN"
            cross_date = str(curr_day.name.date())
            
        # Dead Cross Check: SMA50 was >= SMA200, and is now < SMA200
        elif prev_day['SMA50'] >= prev_day['SMA200'] and curr_day['SMA50'] < curr_day['SMA200']:
            signal = 'DEAD_CROSS'
            message = "Dead Cross! SMA50, SMA200'ün altına indi."
            color = "RED"
            cross_date = str(curr_day.name.date())

    result = {
        "ticker": ticker,
        "signal": signal,
        "color": color,
        "message": message,
        "cross_date": cross_date,
        "current_price": df['Close'].iloc[-1],
        "sma50": df['SMA50'].iloc[-1],
        "sma200": df['SMA200'].iloc[-1],
        "last_updated": str(df.index[-1].date())
    }
    
    return result
