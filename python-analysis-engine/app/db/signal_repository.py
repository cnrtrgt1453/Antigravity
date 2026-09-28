import os
import json
import time
import logging
from typing import List, Dict, Any, Optional
from datetime import datetime, timedelta

from app.db.supabase_client import get_supabase_client

logger = logging.getLogger(__name__)

class ISignalRepository:
    def get_results(self) -> List[Dict[str, Any]]:
        raise NotImplementedError
        
    def save_results(self, results: List[Dict[str, Any]]) -> None:
        raise NotImplementedError

    def get_last_scan_timestamp(self) -> float:
        raise NotImplementedError
        
    def update_last_scan_timestamp(self) -> None:
        raise NotImplementedError


class FileSignalRepository(ISignalRepository):
    def __init__(self, base_dir: str):
        self.results_file = os.path.join(base_dir, "results.json")
        self.cooldown_file = os.path.join(base_dir, "last_scan.json")

    def get_results(self) -> List[Dict[str, Any]]:
        if not os.path.exists(self.results_file):
            return []
        try:
            with open(self.results_file, "r", encoding="utf-8") as f:
                return json.load(f)
        except Exception:
            return []

    def save_results(self, results: List[Dict[str, Any]]) -> None:
        with open(self.results_file, "w", encoding="utf-8") as f:
            json.dump(results, f, indent=2)

    def get_last_scan_timestamp(self) -> float:
        if not os.path.exists(self.cooldown_file):
            return 0.0
        try:
            with open(self.cooldown_file, "r", encoding="utf-8") as f:
                data = json.load(f)
                return data.get("timestamp", 0.0)
        except Exception:
            return 0.0

    def update_last_scan_timestamp(self) -> None:
        with open(self.cooldown_file, "w", encoding="utf-8") as f:
            json.dump({"timestamp": time.time()}, f)


class SupabaseSignalRepository(ISignalRepository):
    """Supabase PostgreSQL tabanlı sinyal ve piyasa verisi deposu.
    Gerektiğinde FileSignalRepository fallback mekanizmasını kullanır.
    """
    def __init__(self, base_dir: str):
        self.fallback = FileSignalRepository(base_dir=base_dir)

    def get_results(self) -> List[Dict[str, Any]]:
        client = get_supabase_client()
        if not client:
            return self.fallback.get_results()

        try:
            # En son piyasa sinyallerini Supabase'den çek
            response = client.table("market_signals").select("*").order("created_at", desc=True).limit(500).execute()
            data = response.data or []
            
            # FastAPI ile uyumlu formata dönüştür
            results = []
            for item in data:
                results.append({
                    "ticker": item.get("ticker"),
                    "signal": item.get("signal_type"),
                    "price": item.get("price"),
                    "cross_date": item.get("cross_date"),
                    "created_at": item.get("created_at")
                })
            return results if results else self.fallback.get_results()
        except Exception as e:
            logger.error(f"Error fetching signals from Supabase, falling back to file: {e}")
            return self.fallback.get_results()

    def save_results(self, results: List[Dict[str, Any]]) -> None:
        # Öncelikle yerel dosyaya yedekle
        self.fallback.save_results(results)

        client = get_supabase_client()
        if not client:
            return

        try:
            # 1. Sinyalleri hazırla (GOLDEN_CROSS veya DEAD_CROSS olanlar)
            signals_to_insert = []
            for r in results:
                sig = r.get("signal")
                ticker = r.get("ticker")
                price = r.get("price") or r.get("current_price")
                cross_date = r.get("cross_date")

                if sig in ["GOLDEN_CROSS", "DEAD_CROSS"] and ticker:
                    signals_to_insert.append({
                        "ticker": ticker,
                        "signal_type": sig,
                        "price": float(price) if price is not None else None,
                        "cross_date": cross_date
                    })

            if signals_to_insert:
                # Toplu ekle
                client.table("market_signals").insert(signals_to_insert).execute()
                logger.info(f"{len(signals_to_insert)} signals successfully inserted into Supabase.")

            # 2. Hisse son fiyatlarını güncelle
            for r in results:
                ticker = r.get("ticker")
                price = r.get("price") or r.get("current_price")
                if ticker and price:
                    try:
                        client.table("stocks").update({
                            "last_price": float(price),
                            "updated_at": datetime.utcnow().isoformat()
                        }).eq("symbol", ticker).execute()
                    except Exception:
                        pass
        except Exception as e:
            logger.error(f"Error saving results to Supabase: {e}")

    def get_last_scan_timestamp(self) -> float:
        return self.fallback.get_last_scan_timestamp()

    def update_last_scan_timestamp(self) -> None:
        self.fallback.update_last_scan_timestamp()
