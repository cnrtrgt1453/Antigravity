import os
import json
import time
from typing import List, Dict, Any
from datetime import datetime, timedelta

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
