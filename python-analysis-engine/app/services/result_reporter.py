from typing import Protocol, List, Dict, Any
import json
import logging
from app.db.database import SessionLocal
from app.models.instrument import Instrument

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

class DatabaseResultReporter:
    """(Opsiyonel Gelecek Planı) Sonuçları doğrudan bir DB tablosuna veya Java API'ye yollayan raporlayıcı."""
    
    def report(self, results: List[Dict[str, Any]]) -> None:
        # Şimdilik metod imzası hazırlandı, Java API ile zaten bir entegrasyonumuz var.
        pass
