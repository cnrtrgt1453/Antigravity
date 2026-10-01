import os
import logging
from datetime import datetime, timezone
from typing import List, Dict, Any, Optional
import urllib.request
import json
import xml.etree.ElementTree as ET

from app.db.supabase_client import get_supabase_client

logger = logging.getLogger(__name__)

class KapNewsService:
    """
    Kamuyu Aydınlatma Platformu (KAP) bildirimlerini çeken,
    içinde bulunduğumuz ay haricindeki eski ayların verilerini otomatik silen
    ve yeni bildirimleri Supabase 'news' tablosuna senkronize eden servis.
    """

    def __init__(self):
        self.client = get_supabase_client()

    def get_current_month_start_iso(self) -> str:
        """Bulunduğumuz ayın ilk gününün 00:00:00 UTC tarihini ISO formatında döndürür."""
        now = datetime.now(timezone.utc)
        start_of_month = datetime(now.year, now.month, 1, 0, 0, 0, tzinfo=timezone.utc)
        return start_of_month.isoformat()

    def cleanup_previous_months_news(self) -> int:
        """
        Bulunduğumuz aydan önceki tüm KAP haberlerini Supabase veritabanından kalıcı olarak siler.
        """
        if not self.client:
            logger.warning("Supabase istemcisi mevcut değil. Eski haberler silinemedi.")
            return 0

        try:
            start_iso = self.get_current_month_start_iso()
            logger.info(f"Geçmiş aylara ait haberler siliniyor (Tarih < {start_iso})...")
            
            res = self.client.table("news").delete().lt("published_at", start_iso).execute()
            deleted_count = len(res.data) if hasattr(res, "data") and res.data else 0
            logger.info(f"Geçmiş aylara ait {deleted_count} eski haber başarıyla temizlendi.")
            return deleted_count
        except Exception as e:
            logger.error(f"Eski haberler silinirken hata oluştu: {e}")
            return 0

    def fetch_disclosures_from_kap_api(self) -> List[Dict[str, Any]]:
        """
        KAP açık web servisinden güncel bildirimleri çeker.
        """
        url = "https://www.kap.org.tr/tr/api/disclosures"
        headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Accept": "application/json, text/plain, */*"
        }

        try:
            req = urllib.request.Request(url, headers=headers)
            with urllib.request.urlopen(req, timeout=12) as response:
                if response.status == 200:
                    data = json.loads(response.read().decode('utf-8'))
                    if isinstance(data, list):
                        return data
        except Exception as e:
            logger.warning(f"KAP web API'den veri alınırken hata oluştu: {e}")
        
        return []

    def fetch_disclosures_from_rss_feed(self) -> List[Dict[str, Any]]:
        """
        Alternatif olarak Borsa İstanbul KAP haberlerini Google News / Finans RSS akışından çeker.
        """
        rss_url = "https://news.google.com/rss/search?q=KAP+Borsa+when:30d&hl=tr&gl=TR&ceid=TR:tr"
        headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
        }

        items = []
        try:
            req = urllib.request.Request(rss_url, headers=headers)
            with urllib.request.urlopen(req, timeout=10) as response:
                if response.status == 200:
                    xml_content = response.read().decode('utf-8')
                    root = ET.fromstring(xml_content)
                    channel = root.find("channel")
                    if channel is not None:
                        for item in channel.findall("item"):
                            title = item.findtext("title", "")
                            link = item.findtext("link", "")
                            pub_date = item.findtext("pubDate", "")
                            guid = item.findtext("guid", link)
                            
                            # Hisse sembolünü başlıktan yakalamaya çalış (Örn: "THYAO", "GARAN", "ASELS")
                            stock_symbol = None
                            for word in title.replace(":", " ").replace("-", " ").split():
                                clean = word.strip().upper()
                                if clean in ["THYAO", "GARAN", "ASELS", "EREGL", "KCHOL", "TUPRS", "BIMAS", "SISE", "SASA", "AKBNK", "YKBNK", "PETKM", "KOZAL", "FROTO", "TOASO", "ISCTR"]:
                                    stock_symbol = clean
                                    break

                            items.append({
                                "guid": guid,
                                "title": title,
                                "link": link,
                                "pub_date": pub_date,
                                "stock_symbol": stock_symbol
                            })
        except Exception as e:
            logger.warning(f"RSS akışından haber çekilirken hata: {e}")

        return items

    def sync_news(self) -> Dict[str, Any]:
        """
        1. Geçtiğimiz aya ait haberleri siler.
        2. Güncel KAP bildirimlerini çeker.
        3. Bu ayın bildirimlerini Supabase 'news' tablosuna upsert eder.
        """
        # 1. Adım: Eski ayları temizle
        deleted_count = self.cleanup_previous_months_news()

        # 2. Adım: Verileri çek
        raw_disclosures = self.fetch_disclosures_from_kap_api()
        now = datetime.now(timezone.utc)
        current_year = now.year
        current_month = now.month

        news_to_insert: List[Dict[str, Any]] = []

        if raw_disclosures:
            for item in raw_disclosures:
                try:
                    basic = item.get("basic", {}) if isinstance(item, dict) else {}
                    uid = str(basic.get("disclosureIndex") or item.get("disclosureIndex") or item.get("id") or "")
                    if not uid:
                        continue

                    title = basic.get("title") or item.get("title") or "KAP Bildirimi"
                    content = basic.get("summary") or item.get("summary") or title
                    symbol = basic.get("stockCodes") or item.get("stockCodes") or ""
                    if isinstance(symbol, list):
                        symbol = symbol[0] if symbol else ""

                    pub_date_str = basic.get("publishDate") or item.get("publishDate")
                    # Tarih kontrolü: Yalnızca bu ayın bildirimlerini ekle
                    pub_dt = None
                    if pub_date_str:
                        try:
                            # Örn: 2026-09-30T14:30:00 veya formatlı
                            pub_dt = datetime.fromisoformat(pub_date_str.replace("Z", "+00:00"))
                        except Exception:
                            pub_dt = now
                    else:
                        pub_dt = now

                    # Sadece mevcut aya ait olanları kabul et
                    if pub_dt.year != current_year or pub_dt.month != current_month:
                        continue

                    link = f"https://www.kap.org.tr/tr/Bildirim/{uid}"

                    news_to_insert.append({
                        "title": title[:500],
                        "content": content,
                        "published_at": pub_dt.isoformat(),
                        "stock_symbol": symbol.upper() if symbol else None,
                        "external_uid": f"kap_{uid}",
                        "source_url": link
                    })
                except Exception as e:
                    logger.debug(f"Haber parse edilirken hata: {e}")

        # Eğer resmi API'den veri alınamadıysa RSS yedeklemesini kullan
        if not news_to_insert:
            logger.info("Resmi API yanıt vermedi, alternatif haber akışı kullanılıyor...")
            rss_items = self.fetch_disclosures_from_rss_feed()
            for r in rss_items:
                uid = r.get("guid") or r.get("link")
                news_to_insert.append({
                    "title": r.get("title", "")[:500],
                    "content": r.get("title", ""),
                    "published_at": now.isoformat(),
                    "stock_symbol": r.get("stock_symbol"),
                    "external_uid": f"rss_{abs(hash(uid))}",
                    "source_url": r.get("link")
                })

        # 3. Adım: Supabase'e yaz (upsert)
        inserted_count = 0
        if news_to_insert and self.client:
            try:
                res = self.client.table("news").upsert(
                    news_to_insert,
                    on_conflict="external_uid"
                ).execute()
                inserted_count = len(news_to_insert)
                logger.info(f"{inserted_count} adet bu aya ait KAP haberi Supabase 'news' tablosuna senkronize edildi.")
            except Exception as e:
                logger.error(f"Haberler Supabase'e kaydedilirken hata: {e}")

        return {
            "status": "success",
            "deleted_previous_months_count": deleted_count,
            "synced_news_count": inserted_count
        }
