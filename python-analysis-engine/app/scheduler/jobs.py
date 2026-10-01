from apscheduler.schedulers.background import BackgroundScheduler
from apscheduler.triggers.cron import CronTrigger
import logging
import json
import os
from enum import Enum

from app.services.data_provider import YahooFinanceProvider
from app.services.analysis_strategy import GoldenCrossStrategy
from app.services.result_reporter import JsonResultReporter, SupabaseResultReporter
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
    reporter = SupabaseResultReporter(file_path=RESULTS_FILE)
    
    engine = ScannerEngine(
        data_provider=provider,
        strategy=strategy,
        reporter=reporter,
        max_workers=10 # 10 thread ile aynı anda hisse analizi yapacak
    )
    
    # Tüm asenkron ve analiz işlemi içeride yürütülecek
    engine.run_scan()

def sync_kap_news_job():
    """
    KAP bildirimlerini çeker ve geçmiş ayların haberlerini temizler.
    Hafta içi borsa açıkken her 2 saatte bir çalışır.
    """
    logger.info("KAP news sync job triggered!")
    try:
        from app.services.kap_news_service import KapNewsService
        service = KapNewsService()
        result = service.sync_news()
        logger.info(f"KAP news sync completed: {result}")
    except Exception as e:
        logger.error(f"Error in KAP news sync job: {e}")

def start_scheduler():
    scheduler = BackgroundScheduler()
    # Runs every Monday at 07:00 AM for market scanner
    trigger = CronTrigger(day_of_week='mon', hour=7, minute=0)
    scheduler.add_job(scan_all_instruments, trigger=trigger, id='weekly_market_scan')
    
    # Runs every 2 hours during weekdays for KAP news
    kap_trigger = CronTrigger(minute=0, hour='8-20/2')
    scheduler.add_job(sync_kap_news_job, trigger=kap_trigger, id='kap_news_sync')
    
    scheduler.start()
    
    logger.info("Scheduler started successfully. Next scan: Mon 7:00 AM, KAP sync every 2 hours.")
    return scheduler
