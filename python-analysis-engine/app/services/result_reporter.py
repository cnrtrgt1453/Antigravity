from typing import Protocol, List, Dict, Any
import json
import logging
from datetime import datetime

logger = logging.getLogger(__name__)

class IResultReporter(Protocol):
    """Analiz sonuçlarının nereye yazılacağını belirten arayüz."""
    def report(self, results: List[Dict[str, Any]]) -> None:
        ...

class JsonResultReporter:
    """Sonuçları JSON dosyasına yazan raporlayıcı."""
    
    def __init__(self, file_path: str = "results.json"):
        self.file_path = file_path
        
    def report(self, results: List[Dict[str, Any]]) -> None:
        try:
            with open(self.file_path, "w", encoding="utf-8") as f:
                json.dump(results, f, ensure_ascii=False, indent=4)
            logger.info(f"Results successfully written to {self.file_path}")
        except Exception as e:
            logger.error(f"Failed to write results to JSON: {e}")

class SupabaseResultReporter:
    """Sonuçları hem yerel JSON dosyasına hem de Supabase PostgreSQL tablosuna aktaran raporlayıcı."""
    
    def __init__(self, file_path: str = "results.json"):
        self.json_reporter = JsonResultReporter(file_path=file_path)

    def report(self, results: List[Dict[str, Any]]) -> None:
        # 1. Yerel JSON'a yedekle
        self.json_reporter.report(results)
        
        # 2. Supabase'e yaz
        from app.db.supabase_client import get_supabase_client
        client = get_supabase_client()
        if not client:
            return

        try:
            signals = []
            for r in results:
                sig = r.get("signal")
                ticker = r.get("ticker")
                price = r.get("price") or r.get("current_price")
                cross_date = r.get("cross_date")

                if sig in ["GOLDEN_CROSS", "DEAD_CROSS"] and ticker:
                    signals.append({
                        "ticker": ticker,
                        "signal_type": sig,
                        "price": float(price) if price is not None else None,
                        "cross_date": cross_date
                    })

            if signals:
                client.table("market_signals").insert(signals).execute()
                logger.info(f"{len(signals)} signals saved to Supabase.")
        except Exception as e:
            logger.error(f"Failed to report results to Supabase: {e}")

class DatabaseResultReporter(SupabaseResultReporter):
    """Geriye dönük uyumluluk takma adı (Alias)."""
    pass
