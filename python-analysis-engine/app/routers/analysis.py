import os
import time
from datetime import datetime, timedelta
from typing import List, Dict, Any, Optional

from fastapi import APIRouter, HTTPException, Depends

from app.db.signal_repository import ISignalRepository, FileSignalRepository
from app.services.data_provider import IMarketDataProvider, YahooFinanceProvider
from app.services.analysis_strategy import IAnalysisStrategy, GoldenCrossStrategy
from app.services.indicator_service import TechnicalIndicatorService

router = APIRouter()

BASE_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
COOLDOWN_HOURS = 12

def get_signal_repository() -> ISignalRepository:
    return FileSignalRepository(base_dir=BASE_DIR)

def get_market_data_provider() -> IMarketDataProvider:
    return YahooFinanceProvider()

def get_analysis_strategy() -> IAnalysisStrategy:
    return GoldenCrossStrategy()


@router.get("/scan")
def scan_single_instrument(
    ticker: str,
    provider: IMarketDataProvider = Depends(get_market_data_provider),
    strategy: IAnalysisStrategy = Depends(get_analysis_strategy)
):
    """Scans a single instrument right now on-demand."""
    df = provider.fetch_historical_data(ticker, period="2y", interval="1d")
    result = strategy.analyze(df, ticker)
    return result


@router.get("/run_full_scan_now")
def run_full_scan_now(repo: ISignalRepository = Depends(get_signal_repository)):
    """Triggers the weekly scheduled job manually for testing with 12-hour cooldown logic."""
    last_scan = repo.get_last_scan_timestamp()
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
    scan_all_instruments()
    repo.update_last_scan_timestamp()
    
    return {"message": "Piyasa taraması başarıyla başlatıldı ve tamamlandı."}


@router.get("/cooldown_status")
def get_cooldown_status(repo: ISignalRepository = Depends(get_signal_repository)):
    """Returns the remaining cooldown time for the manual button."""
    last_scan = repo.get_last_scan_timestamp()
    elapsed = time.time() - last_scan
    cooldown_seconds = COOLDOWN_HOURS * 3600
    
    remaining = max(0, int(cooldown_seconds - elapsed))
    return {
        "can_scan": remaining == 0,
        "remaining_seconds": remaining,
        "last_scan_timestamp": last_scan
    }


@router.get("/latest_signals")
def get_latest_signals(repo: ISignalRepository = Depends(get_signal_repository)):
    """Returns results for instruments that had a Golden or Dead Cross in the last 7 days."""
    try:
        results = repo.get_results()
        seven_days_ago = datetime.now() - timedelta(days=7)
        
        filtered_results = []
        for r in results:
            if r.get("cross_date"):
                try:
                    cross_date = datetime.strptime(r["cross_date"], "%Y-%m-%d")
                    if cross_date >= seven_days_ago:
                        filtered_results.append(r)
                except Exception:
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
def get_all_market_data(repo: ISignalRepository = Depends(get_signal_repository)):
    """Returns results for ALL instruments (for Markets screen)."""
    try:
        return repo.get_results()
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/ohlc")
def get_ohlc_data(
    ticker: str,
    period: str = "1y",
    interval: str = "1d",
    provider: IMarketDataProvider = Depends(get_market_data_provider)
):
    """Returns historical OHLC data, SMA indicators and Cross markers."""
    df = provider.fetch_historical_data(ticker, period=period, interval=interval)
    if df is None or df.empty:
        raise HTTPException(status_code=404, detail=f"No data found for {ticker}")
    
    return TechnicalIndicatorService.calculate_ohlc_with_indicators(df)
