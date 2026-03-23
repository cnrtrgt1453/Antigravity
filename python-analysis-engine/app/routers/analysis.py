import os
import json
import time
from datetime import datetime, timedelta
import pandas as pd
import traceback
from fastapi import APIRouter, HTTPException
from app.services.data_provider import YahooFinanceProvider
from app.services.analysis_strategy import GoldenCrossStrategy
from pydantic import BaseModel
from typing import List, Any, Dict

router = APIRouter()

# Get the absolute path to the directory where this file located (app/routers)
# Then go up two levels to the root directory (python-analysis-engine)
BASE_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

COOLDOWN_FILE = os.path.join(BASE_DIR, "last_scan.json")
COOLDOWN_HOURS = 12
RESULTS_FILE = os.path.join(BASE_DIR, "results.json")

class AnalysisResult(BaseModel):
    ticker: str
    signal: str
    color: str
    message: str
    cross_date: str | None
    current_price: float | None
    sma50: float | None
    sma200: float | None
    cross_price: float | None
    last_updated: str | None

def get_last_scan_time():
    if not os.path.exists(COOLDOWN_FILE):
        return 0
    try:
        with open(COOLDOWN_FILE, "r") as f:
            data = json.load(f)
            return data.get("timestamp", 0)
    except:
        return 0

def update_last_scan_time():
    with open(COOLDOWN_FILE, "w") as f:
        json.dump({"timestamp": time.time()}, f)

@router.get("/scan")
def scan_single_instrument(ticker: str):
    """
    Scans a single instrument right now on-demand.
    Example: ?ticker=THYAO.IS OR ?ticker=XAUUSD=X
    """
    provider = YahooFinanceProvider()
    strategy = GoldenCrossStrategy()
    df = provider.fetch_historical_data(ticker, period="2y", interval="1d")
    result = strategy.analyze(df, ticker)
    return result

@router.get("/run_full_scan_now")
def run_full_scan_now():
    """
    Triggers the weekly scheduled job manually for testing.
    Includes a 12-hour cooldown logic.
    """
    last_scan = get_last_scan_time()
    elapsed = time.time() - last_scan
    cooldown_seconds = COOLDOWN_HOURS * 3600

    if elapsed < cooldown_seconds:
        remaining = int(cooldown_seconds - elapsed)
        hours = remaining // 3600
        minutes = (remaining % 3600) // 60
        raise HTTPException(
            status_code=429, 
            detail=f"Tarama için beklemeniz gerekiyor. Kalan süre: {hours} saat {minutes} dakika."
        )

    from app.scheduler.jobs import scan_all_instruments
    # Actually trigger the scan
    scan_all_instruments()
    update_last_scan_time()
    
    return {"message": "Piyasa taraması başarıyla başlatıldı ve tamamlandı."}

@router.get("/cooldown_status")
def get_cooldown_status():
    """
    Returns the remaining cooldown time for the manual button.
    """
    last_scan = get_last_scan_time()
    elapsed = time.time() - last_scan
    cooldown_seconds = COOLDOWN_HOURS * 3600
    
    remaining = max(0, int(cooldown_seconds - elapsed))
    return {
        "can_scan": remaining == 0,
        "remaining_seconds": remaining,
        "last_scan_timestamp": last_scan
    }

@router.get("/latest_signals")
def get_latest_signals():
    """
    Returns results for instruments that had a Golden or Dead Cross in the last 7 days.
    """
    if not os.path.exists(RESULTS_FILE):
        return {"golden_signals": [], "dead_signals": []}
    
    try:
        with open(RESULTS_FILE, "r") as f:
            results = json.load(f)
            
        seven_days_ago = datetime.now() - timedelta(days=7)
        
        filtered_results = []
        for r in results:
            if r.get("cross_date"):
                try:
                    cross_date = datetime.strptime(r["cross_date"], "%Y-%m-%d")
                    if cross_date >= seven_days_ago:
                        filtered_results.append(r)
                except:
                    continue
                    
        golden = [r for r in filtered_results if r.get("signal") == "GOLDEN_CROSS"]
        dead = [r for r in filtered_results if r.get("signal") == "DEAD_CROSS"]
        
        return {
            "golden_signals": golden,
            "dead_signals": dead
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/all_market_data")
def get_all_market_data():
    """
    Returns results for ALL instruments (for Markets screen).
    """
    try:
        if not os.path.exists(RESULTS_FILE):
            print(f"HATA: {RESULTS_FILE} bulunamadı!")
            return []
            
        with open(RESULTS_FILE, "r", encoding="utf-8") as f:
            results = json.load(f)
        return results
    except Exception as e:
        print("--- Python API Hatası ---")
        traceback.print_exc()
        print("-------------------------")
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/ohlc")
def get_ohlc_data(ticker: str, period: str = "1y", interval: str = "1d"):
    """
    Returns historical OHLC data, SMA indicators and Cross markers.
    """
    provider = YahooFinanceProvider()
    df = provider.fetch_historical_data(ticker, period=period, interval=interval)
    
    if df is None or df.empty:
        raise HTTPException(status_code=404, detail=f"No data found for {ticker}")
    
    # Calculate SMAs
    df['SMA50'] = df['Close'].rolling(window=50).mean()
    df['SMA200'] = df['Close'].rolling(window=200).mean()
    
    # Reset index to get date
    df = df.reset_index()
    date_col = 'Date' if 'Date' in df.columns else 'Datetime'
    
    ohlc_data = []
    sma50_data = []
    sma200_data = []
    markers = []
    
    for i in range(len(df)):
        row = df.iloc[i]
        t = int(row[date_col].timestamp())
        
        # OHLC
        ohlc_data.append({
            "time": t,
            "open": float(row["Open"]),
            "high": float(row["High"]),
            "low": float(row["Low"]),
            "close": float(row["Close"])
        })
        
        # SMA50
        if not pd.isna(row['SMA50']):
            sma50_data.append({"time": t, "value": float(row['SMA50'])})
            
        # SMA200
        if not pd.isna(row['SMA200']):
            sma200_data.append({"time": t, "value": float(row['SMA200'])})
            
        # Markers (Cross detector)
        if i > 0:
            prev_row = df.iloc[i-1]
            if not pd.isna(prev_row['SMA50']) and not pd.isna(prev_row['SMA200']) and \
               not pd.isna(row['SMA50']) and not pd.isna(row['SMA200']):
                
                # Golden Cross
                if prev_row['SMA50'] <= prev_row['SMA200'] and row['SMA50'] > row['SMA200']:
                    markers.append({
                        "time": t,
                        "position": "belowBar",
                        "color": "#F6C90E",
                        "shape": "arrowUp",
                        "text": "☀️ GOLDEN"
                    })
                # Dead Cross
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
