import os
import json
import time
from datetime import datetime, timedelta
from fastapi import APIRouter, HTTPException
from app.services.data_client import fetch_historical_data
from app.services.analyzer import analyze_cross
from pydantic import BaseModel
from typing import List, Any, Dict

router = APIRouter()

COOLDOWN_FILE = "last_scan.json"
COOLDOWN_HOURS = 12
RESULTS_FILE = "results.json"

class AnalysisResult(BaseModel):
    ticker: str
    signal: str
    color: str
    message: str
    cross_date: str | None
    current_price: float | None
    sma50: float | None
    sma200: float | None
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
    df = fetch_historical_data(ticker, period="2y", interval="1d")
    result = analyze_cross(df, ticker)
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
    Returns the latest scan results from results.json.
    """
    if not os.path.exists(RESULTS_FILE):
        return {"golden_signals": [], "dead_signals": []}
    
    try:
        with open(RESULTS_FILE, "r") as f:
            results = json.load(f)
            
        golden = [r for r in results if r.get("signal") == "GOLDEN_CROSS"]
        dead = [r for r in results if r.get("signal") == "DEAD_CROSS"]
        
        return {
            "golden_signals": golden,
            "dead_signals": dead
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
