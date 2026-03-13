from apscheduler.schedulers.background import BackgroundScheduler
from apscheduler.triggers.cron import CronTrigger
import logging

from app.services.data_client import fetch_historical_data
from app.services.analyzer import analyze_cross

logger = logging.getLogger(__name__)

# A small subset of BIST30 or standard watchlist.
# Can be loaded from DB.
WATCHLIST = [
    "XAUUSD=X", # Gold
    "XAGUSD=X", # Silver
    "THYAO.IS", # BIST example
    "GARAN.IS",
    "AKBNK.IS",
    "EREGL.IS",
    "TUPRS.IS"
]

def scan_all_instruments():
    """
    Scans the watchlist, fetches data, performs analysis.
    This runs periodically (every Monday at 07:00).
    """
    logger.info("Weekly scanner triggered! Scanning instruments...")
    results = []
    for ticker in WATCHLIST:
        df = fetch_historical_data(ticker, period="2y", interval="1d")
        result = analyze_cross(df, ticker)
        results.append(result)
        
        # If Golden/Dead Cross found, we can send a Push Notification, save to DB, etc.
        if result['signal'] in ['GOLDEN_CROSS', 'DEAD_CROSS']:
            logger.info(f"SIGNAL FOUND: {ticker} -> {result['signal']} ({result['color']}) on {result['cross_date']}")
        else:
            logger.info(f"{ticker} -> {result['signal']}")
    
    logger.info("Weekly scan completed!")
    # TODO: In the future, save `results` to PostgreSQL so Java/Mobile can read it instantly.

def start_scheduler():
    scheduler = BackgroundScheduler()
    # Runs every Monday at 07:00 AM
    trigger = CronTrigger(day_of_week='mon', hour=7, minute=0)
    scheduler.add_job(scan_all_instruments, trigger=trigger, id='weekly_market_scan')
    scheduler.start()
    
    logger.info("Scheduler started successfully. Next run is Monday at 7:00 AM.")
    return scheduler
