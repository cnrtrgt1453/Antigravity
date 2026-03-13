from apscheduler.schedulers.background import BackgroundScheduler
from apscheduler.triggers.cron import CronTrigger
import logging
import json
import os

from app.services.data_client import fetch_historical_data
from app.services.analyzer import analyze_cross

logger = logging.getLogger(__name__)

# Watchlist for BIST100 and major instruments.
# This list covers major BIST stocks, Gold, and Silver.
WATCHLIST = [
    "XAUUSD=X", "XAGUSD=X", # Gold & Silver
    "AEFES.IS", "AGHOL.IS", "AKBNK.IS", "AKSA.IS", "AKSEN.IS", "ALARK.IS", "ALBRK.IS", "ARCLK.IS",
    "ASELS.IS", "ASTOR.IS", "ASUZU.IS", "AYDEM.IS", "BAGFS.IS", "BERA.IS", "BIMAS.IS", "BRISA.IS",
    "BRYAT.IS", "BUCIM.IS", "CANTE.IS", "CCOLA.IS", "CEMTS.IS", "CIMSA.IS", "CWENE.IS", "DOAS.IS",
    "DOHOL.IS", "EGEEN.IS", "EKGYO.IS", "ENJSA.IS", "ENKAI.IS", "EREGL.IS", "EUPWR.IS", "FROTO.IS",
    "GARAN.IS", "GENIL.IS", "GESAN.IS", "GLYHO.IS", "GSDHO.IS", "GUBRF.IS", "GWIND.IS", "HALKB.IS",
    "HETSH.IS", "IPEKE.IS", "ISCTR.IS", "ISDMR.IS", "ISGYO.IS", "ISMEN.IS", "IZMDC.IS", "KARDM.IS",
    "KCHOL.IS", "KENT.IS", "KONTR.IS", "KORDS.IS", "KOZAA.IS", "KOZAL.IS", "KRDMD.IS", "MAVI.IS",
    "MGROS.IS", "MIATK.IS", "ODAS.IS", "OTKAR.IS", "OYAKC.IS", "PENTA.IS", "PETKM.IS", "PGSUS.IS",
    "QUAGR.IS", "SAHOL.IS", "SASA.IS", "SAYAS.IS", "SISE.IS", "SKBNK.IS", "SMRTG.IS", "SOKM.IS",
    "TARKN.IS", "TAVHL.IS", "TCELL.IS", "THYAO.IS", "TKFEN.IS", "TMSN.IS", "TOASO.IS", "TSKB.IS",
    "TTKOM.IS", "TTRAK.IS", "TUPRS.IS", "TURSG.IS", "ULKER.IS", "VAKBN.IS", "VESBE.IS", "VESTL.IS",
    "YEOTK.IS", "YKBNK.IS", "YYLGD.IS", "ZOREN.IS"
]

RESULTS_FILE = "results.json"

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
    
    # Save results to JSON file
    try:
        with open(RESULTS_FILE, "w") as f:
            json.dump(results, f)
        logger.info(f"Results saved to {RESULTS_FILE}")
    except Exception as e:
        logger.error(f"Error saving results: {e}")

def start_scheduler():
    scheduler = BackgroundScheduler()
    # Runs every Monday at 07:00 AM
    trigger = CronTrigger(day_of_week='mon', hour=7, minute=0)
    scheduler.add_job(scan_all_instruments, trigger=trigger, id='weekly_market_scan')
    scheduler.start()
    
    logger.info("Scheduler started successfully. Next run is Monday at 7:00 AM.")
    return scheduler
