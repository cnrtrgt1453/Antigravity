from fastapi import APIRouter
from app.services.kap_news_service import KapNewsService

router = APIRouter()

@router.get("/sync")
@router.post("/sync")
def sync_kap_news():
    """
    KAP haberlerini senkronize eder:
    - Geçtiğimiz aya ait tüm eski haberleri veritabanından siler.
    - Bulunduğumuz ayın güncel KAP bildirimlerini çeker ve veritabanına kaydeder.
    """
    service = KapNewsService()
    result = service.sync_news()
    return result
