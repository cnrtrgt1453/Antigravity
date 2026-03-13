from sqlalchemy import Column, Integer, String, Boolean
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()

class Instrument(Base):
    __tablename__ = 'instruments'

    id = Column(Integer, primary_key=True, index=True)
    symbol = Column(String, unique=True, index=True, nullable=False)
    name = Column(String, nullable=True)
    is_active = Column(Boolean, default=True)

    def __repr__(self):
        return f"<Instrument(symbol='{self.symbol}', active={self.is_active})>"
