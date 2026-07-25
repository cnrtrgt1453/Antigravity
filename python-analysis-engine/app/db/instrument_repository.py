from typing import List
from app.db.database import SessionLocal
from app.models.instrument import Instrument

class IInstrumentRepository:
    def get_active_symbols(self) -> List[str]:
        raise NotImplementedError

class SqlAlchemyInstrumentRepository(IInstrumentRepository):
    def get_active_symbols(self) -> List[str]:
        db = SessionLocal()
        try:
            instruments = db.query(Instrument).filter(Instrument.is_active == True).all()
            return [inst.symbol for inst in instruments]
        finally:
            db.close()
