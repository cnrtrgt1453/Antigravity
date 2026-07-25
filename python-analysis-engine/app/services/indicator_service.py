import pandas as pd
from typing import Dict, Any, List

class TechnicalIndicatorService:
    @staticmethod
    def calculate_ohlc_with_indicators(df: pd.DataFrame) -> Dict[str, Any]:
        """Calculates SMA50, SMA200 and Cross Markers from historical OHLC DataFrame."""
        if df is None or df.empty:
            return {"ohlc": [], "sma50": [], "sma200": [], "markers": []}

        df = df.copy()
        df['SMA50'] = df['Close'].rolling(window=50).mean()
        df['SMA200'] = df['Close'].rolling(window=200).mean()

        df = df.reset_index()
        date_col = 'Date' if 'Date' in df.columns else 'Datetime'

        ohlc_data = []
        sma50_data = []
        sma200_data = []
        markers = []

        for i in range(len(df)):
            row = df.iloc[i]
            t = int(row[date_col].timestamp())

            ohlc_data.append({
                "time": t,
                "open": float(row["Open"]),
                "high": float(row["High"]),
                "low": float(row["Low"]),
                "close": float(row["Close"])
            })

            if not pd.isna(row['SMA50']):
                sma50_data.append({"time": t, "value": float(row['SMA50'])})

            if not pd.isna(row['SMA200']):
                sma200_data.append({"time": t, "value": float(row['SMA200'])})

            if i > 0:
                prev_row = df.iloc[i - 1]
                if (not pd.isna(prev_row['SMA50']) and not pd.isna(prev_row['SMA200']) and
                        not pd.isna(row['SMA50']) and not pd.isna(row['SMA200'])):
                    if prev_row['SMA50'] <= prev_row['SMA200'] and row['SMA50'] > row['SMA200']:
                        markers.append({
                            "time": t,
                            "position": "belowBar",
                            "color": "#F6C90E",
                            "shape": "arrowUp",
                            "text": "☀️ GOLDEN"
                        })
                    elif prev_row['SMA50'] >= prev_row['SMA200'] and row['SMA50'] < row['SMA200']:
                        markers.append({
                            "time": t,
                            "position": "aboveBar",
                            "color": "#ef5350",
                            "shape": "arrowDown",
                            "text": "💀 DEAD"
                        })

        return {
            "ohlc": ohlc_data,
            "sma50": sma50_data,
            "sma200": sma200_data,
            "markers": markers
        }
