from apscheduler.schedulers.background import BackgroundScheduler
from apscheduler.triggers.cron import CronTrigger
import logging
import json
import os
from enum import Enum

from app.services.data_provider import YahooFinanceProvider
from app.services.analysis_strategy import GoldenCrossStrategy
from app.services.result_reporter import JsonResultReporter
from app.services.scanner_engine import ScannerEngine

logger = logging.getLogger(__name__)

RESULTS_FILE = "results.json"

def scan_all_instruments():
    """
    Scans the watchlist fetching data and performing analysis asynchronously.
    This runs periodically (every Monday at 07:00).
    """
    logger.info("Weekly scanner triggered! Initializing Scanner Engine...")
    
    provider = YahooFinanceProvider()
    strategy = GoldenCrossStrategy(short_window=50, long_window=200)
    reporter = JsonResultReporter(file_path=RESULTS_FILE)
    
    engine = ScannerEngine(
        data_provider=provider,
        strategy=strategy,
        reporter=reporter,
        max_workers=10 # 10 thread ile aynı anda hisse analizi yapacak
    )
    
    # Tüm asenkron ve analiz işlemi içeride yürütülecek
    engine.run_scan()

def start_scheduler():
    scheduler = BackgroundScheduler()
    # Runs every Monday at 07:00 AM
    trigger = CronTrigger(day_of_week='mon', hour=7, minute=0)
    scheduler.add_job(scan_all_instruments, trigger=trigger, id='weekly_market_scan')
    scheduler.start()
    
    logger.info("Scheduler started successfully. Next run is Monday at 7:00 AM.")
    return scheduler
