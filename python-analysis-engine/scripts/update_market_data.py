import json
import os
import sys
import logging
import urllib.request
import urllib.parse
from concurrent.futures import ThreadPoolExecutor, as_completed
from dotenv import load_dotenv
import yfinance as yf

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger(__name__)

current_dir = os.path.dirname(os.path.abspath(__file__))
parent_dir = os.path.dirname(current_dir)
sys.path.insert(0, parent_dir)

load_dotenv(os.path.join(parent_dir, ".env"))

SUPABASE_URL = os.getenv("SUPABASE_URL")
SUPABASE_KEY = os.getenv("SUPABASE_SERVICE_ROLE_KEY") or os.getenv("SUPABASE_ANON_KEY")

BIST_30_SYMBOLS = {
    "AKBNK.IS", "ALARK.IS", "ARCLK.IS", "ASELS.IS", "ASTOR.IS",
    "BIMAS.IS", "BRSAN.IS", "EKGYO.IS", "ENKAI.IS", "EREGL.IS",
    "FROTO.IS", "GARAN.IS", "GUBRF.IS", "HEKTS.IS", "ISCTR.IS",
    "KCHOL.IS", "KONTR.IS", "KOZAL.IS", "KRDMD.IS", "MGROS.IS",
    "OYAKC.IS", "PETKM.IS", "PGSUS.IS", "SAHOL.IS", "SASA.IS",
    "SISE.IS", "TCELL.IS", "THYAO.IS", "TOASO.IS", "TUPRS.IS", "YKBNK.IS"
}

BIST_50_ADDITIONAL = {
    "AEFES.IS", "AGHOL.IS", "AHGAZ.IS", "AKFYE.IS", "AKSA.IS",
    "AKSEN.IS", "ALFAS.IS", "ANSGR.IS", "CANTE.IS", "CIMSA.IS",
    "CWENE.IS", "DOAS.IS", "DOHOL.IS", "ECILC.IS", "ENJSA.IS",
    "EUPWR.IS", "GENIL.IS", "GESAN.IS", "GOLTS.IS", "ISMEN.IS",
    "KCAER.IS", "KOZAA.IS", "MAVI.IS", "ODAS.IS", "QUAGR.IS",
    "REEDR.IS", "SMRTG.IS", "SOKM.IS", "TABGD.IS", "TAVHL.IS",
    "TKFEN.IS", "TTKOM.IS", "ULKER.IS", "VAKBN.IS", "VESBE.IS"
}
BIST_50_SYMBOLS = BIST_30_SYMBOLS.union(BIST_50_ADDITIONAL)

BIST_100_ADDITIONAL = {
    "ALGYO.IS", "AYDEM.IS", "BAGFS.IS", "BERA.IS", "BOBET.IS",
    "BRYAT.IS", "BTCIM.IS", "BUCIM.IS", "CLEBI.IS", "EGEEN.IS",
    "GLYHO.IS", "GWIND.IS", "HALKB.IS", "ISGYO.IS", "IZMDC.IS",
    "KARSN.IS", "KORDS.IS", "KMPUR.IS", "KONYA.IS", "KUTPO.IS",
    "LOGO.IS", "MIATK.IS", "NETAS.IS", "NTGAZ.IS", "OTKAR.IS",
    "PARSN.IS", "PENTA.IS", "PSGYO.IS", "SDTTR.IS", "SKBNK.IS",
    "TATGD.IS", "TMSN.IS", "TRGYO.IS", "TSKB.IS", "TTRAK.IS",
    "TUKAS.IS", "TURSG.IS", "YEOTK.IS", "ZOREN.IS", "GENKM.IS",
    "EUPWR.IS", "IMASM.IS", "KLSER.IS", "KOTON.IS", "LILAK.IS"
}
BIST_100_SYMBOLS = BIST_50_SYMBOLS.union(BIST_100_ADDITIONAL)

def fetch_daily_changes(tickers):
    logger.info(f"Fetching daily changes for {len(tickers)} tickers via yfinance...")
    daily_changes = {}
    
    # Download in chunks of 100 to avoid request URL limit
    chunk_size = 80
    for i in range(0, len(tickers), chunk_size):
        chunk = tickers[i:i + chunk_size]
        try:
            df = yf.download(chunk, period="5d", progress=False)['Close']
            for ticker in chunk:
                try:
                    if ticker in df.columns:
                        series = df[ticker].dropna()
                    elif isinstance(df, yf.pandas.Series):
                        series = df.dropna()
                    else:
                        continue

                    if len(series) >= 2:
                        curr = float(series.iloc[-1])
                        prev = float(series.iloc[-2])
                        if prev > 0:
                            pct = ((curr - prev) / prev) * 100.0
                            daily_changes[ticker] = (curr, pct)
                except Exception:
                    pass
        except Exception as e:
            logger.warning(f"Error fetching chunk {i}: {e}")

    logger.info(f"Got daily changes for {len(daily_changes)} tickers.")
    return daily_changes

def update_stock(ticker, daily_data, results_map, headers):
    last_price = None
    daily_pct = None

    if ticker in daily_data:
        curr, pct = daily_data[ticker]
        last_price = curr
        daily_pct = pct

    res_item = results_map.get(ticker, {})
    if last_price is None:
        last_price = res_item.get("current_price")

    cross_price = res_item.get("cross_price")
    cross_diff = None
    cross_diff_pct = None
    if last_price is not None and cross_price is not None and cross_price > 0:
        cross_diff = last_price - cross_price
        cross_diff_pct = ((last_price - cross_price) / cross_price) * 100.0

    payload = {
        "is_bist30": ticker in BIST_30_SYMBOLS,
        "is_bist50": ticker in BIST_50_SYMBOLS,
        "is_bist100": ticker in BIST_100_SYMBOLS,
    }

    if last_price is not None:
        payload["last_price"] = round(float(last_price), 4)
    if daily_pct is not None:
        payload["daily_change_percent"] = round(float(daily_pct), 2)
    if cross_diff is not None:
        payload["cross_diff"] = round(float(cross_diff), 4)
    if cross_diff_pct is not None:
        payload["cross_diff_percent"] = round(float(cross_diff_pct), 2)

    url = f"{SUPABASE_URL}/rest/v1/stocks?symbol=eq.{urllib.parse.quote(ticker)}"
    body = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(url, data=body, headers=headers, method="PATCH")

    try:
        with urllib.request.urlopen(req, timeout=10) as response:
            return response.status in (200, 204)
    except Exception:
        return False

def main():
    results_path = os.path.join(parent_dir, "results.json")
    with open(results_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    results_map = {item["ticker"]: item for item in data if "ticker" in item}
    tickers = list(results_map.keys())

    # 1. Fetch daily change percentages
    daily_data = fetch_daily_changes(tickers)

    # 2. Update Supabase
    headers = {
        "apikey": SUPABASE_KEY,
        "Authorization": f"Bearer {SUPABASE_KEY}",
        "Content-Type": "application/json"
    }

    logger.info("Updating Supabase with daily changes and BIST 30/50/100 tags...")
    success = 0
    with ThreadPoolExecutor(max_workers=20) as executor:
        futures = {executor.submit(update_stock, t, daily_data, results_map, headers): t for t in tickers}
        for future in as_completed(futures):
            if future.result():
                success += 1

    logger.info(f"Done! Updated {success}/{len(tickers)} stocks in Supabase.")

if __name__ == "__main__":
    main()
