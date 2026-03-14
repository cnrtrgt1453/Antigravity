from fastapi import FastAPI
from contextlib import asynccontextmanager
from app.scheduler.jobs import start_scheduler
from app.routers import analysis
import logging

logging.basicConfig(level=logging.INFO)

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Start the scheduler on app startup
    scheduler = start_scheduler()
    yield
    # Shutdown the scheduler on app shutdown
    if scheduler:
        scheduler.shutdown()

app = FastAPI(title="Python Analysis Engine", version="1.0.0", lifespan=lifespan)

app.include_router(analysis.router, prefix="/api/v1/analysis", tags=["Analysis"])

@app.get("/")
def read_root():
    return {"message": "Welcome to Python Analysis Engine. Visit /docs for API documentation."}
