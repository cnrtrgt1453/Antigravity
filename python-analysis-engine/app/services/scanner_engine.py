import logging
import concurrent.futures
from typing import List, Dict, Any, Optional

from app.db.instrument_repository import IInstrumentRepository, SqlAlchemyInstrumentRepository
from app.services.data_provider import IMarketDataProvider
from app.services.analysis_strategy import IAnalysisStrategy
from app.services.result_reporter import IResultReporter

logger = logging.getLogger(__name__)

class ScannerEngine:
    """Tüm analiz sürecini (DB -> Provider -> Strategy -> Reporter) koordine eden asenkron motor."""
    
    def __init__(
        self, 
        data_provider: IMarketDataProvider, 
        strategy: IAnalysisStrategy, 
        reporter: IResultReporter,
        instrument_repository: Optional[IInstrumentRepository] = None,
        max_workers: int = 10
    ):
        self.data_provider = data_provider
        self.strategy = strategy
        self.reporter = reporter
        self.instrument_repository = instrument_repository or SqlAlchemyInstrumentRepository()
        self.max_workers = max_workers

    def get_active_instruments(self) -> List[str]:
        """Veritabanındaki aktif hisseleri getirir."""
        return self.instrument_repository.get_active_symbols()

    def _process_single_instrument(self, ticker: str) -> Dict[str, Any]:
        """Tek bir hisse için veri çekme ve analiz işlemini yapar."""
        df = self.data_provider.fetch_historical_data(ticker=ticker)
        result = self.strategy.analyze(df=df, ticker=ticker)
        
        if result.get("signal") in ["GOLDEN_CROSS", "DEAD_CROSS"]:
            logger.info(f"SIGNAL FOUND: {ticker} -> {result['signal']} on {result['cross_date']}")
            
        return result

    def run_scan(self):
        """Tüm aktif hisseleri ThreadPool kullanarak asenkron tarar."""
        tickers = self.get_active_instruments()
        logger.info(f"Starting async scan for {len(tickers)} instruments using {self.max_workers} threads...")
        
        results = []
        
        with concurrent.futures.ThreadPoolExecutor(max_workers=self.max_workers) as executor:
            future_to_ticker = {executor.submit(self._process_single_instrument, ticker): ticker for ticker in tickers}
            
            for future in concurrent.futures.as_completed(future_to_ticker):
                ticker = future_to_ticker[future]
                try:
                    res = future.result()
                    results.append(res)
                except Exception as exc:
                    logger.error(f"{ticker} generated an exception: {exc}")
                    
        logger.info(f"Scan completed. Total processed: {len(results)}")
        self.reporter.report(results)
