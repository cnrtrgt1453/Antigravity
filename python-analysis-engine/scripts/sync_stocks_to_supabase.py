import json
import os
import sys
import logging
import urllib.request
import urllib.parse
from concurrent.futures import ThreadPoolExecutor, as_completed
from dotenv import load_dotenv

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger(__name__)

current_dir = os.path.dirname(os.path.abspath(__file__))
parent_dir = os.path.dirname(current_dir)
sys.path.insert(0, parent_dir)

load_dotenv(os.path.join(parent_dir, ".env"))

SUPABASE_URL = os.getenv("SUPABASE_URL")
SUPABASE_KEY = os.getenv("SUPABASE_SERVICE_ROLE_KEY") or os.getenv("SUPABASE_ANON_KEY")

def update_single_stock(item, headers):
    ticker = item.get("ticker")
    if not ticker:
        return False

    last_price = item.get("current_price") or item.get("price")
    sma50 = item.get("sma50")
    sma200 = item.get("sma200")
    signal = item.get("signal") or "NO_SIGNAL"
    cross_price = item.get("cross_price")
    cross_date = item.get("cross_date")

    payload = {
        "last_price": round(float(last_price), 4) if last_price is not None else None,
        "sma50": round(float(sma50), 4) if sma50 is not None else None,
        "sma200": round(float(sma200), 4) if sma200 is not None else None,
        "signal": signal,
        "cross_price": round(float(cross_price), 4) if cross_price is not None else None,
        "cross_date": cross_date
    }

    url = f"{SUPABASE_URL}/rest/v1/stocks?symbol=eq.{urllib.parse.quote(ticker)}"
    body = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(url, data=body, headers=headers, method="PATCH")

    try:
        with urllib.request.urlopen(req, timeout=10) as response:
            return response.status in (200, 204)
    except Exception as e:
        return False

def sync():
    results_path = os.path.join(parent_dir, "results.json")
    if not os.path.exists(results_path):
        logger.error(f"results.json not found at {results_path}")
        return

    with open(results_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    logger.info(f"Loaded {len(data)} items from results.json")

    if not SUPABASE_URL or not SUPABASE_KEY:
        logger.error("Missing SUPABASE_URL or SUPABASE_KEY in .env")
        return

    headers = {
        "apikey": SUPABASE_KEY,
        "Authorization": f"Bearer {SUPABASE_KEY}",
        "Content-Type": "application/json"
    }

    total = len(data)
    success_count = 0

    logger.info(f"Starting parallel sync with 20 workers...")
    with ThreadPoolExecutor(max_workers=20) as executor:
        futures = {executor.submit(update_single_stock, item, headers): item for item in data}
        completed = 0
        for future in as_completed(futures):
            completed += 1
            if future.result():
                success_count += 1
            if completed % 100 == 0 or completed == total:
                logger.info(f"Progress: {completed}/{total} stocks processed ({success_count} succeeded)")

    logger.info(f"Sync complete! Successfully updated {success_count}/{total} stocks in Supabase.")

if __name__ == "__main__":
    sync()
