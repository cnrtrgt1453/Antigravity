import os
import logging
from typing import Optional
from dotenv import load_dotenv

load_dotenv()

logger = logging.getLogger(__name__)

_supabase_client = None

def get_supabase_client():
    """Returns a singleton instance of the Supabase client."""
    global _supabase_client
    if _supabase_client is not None:
        return _supabase_client

    supabase_url = os.getenv("SUPABASE_URL")
    # For backend operations like scanner writing signals to public tables,
    # service role key is preferred, but anon key works if policies permit.
    supabase_key = os.getenv("SUPABASE_SERVICE_ROLE_KEY") or os.getenv("SUPABASE_ANON_KEY")

    if not supabase_url or not supabase_key:
        logger.warning(
            "SUPABASE_URL or SUPABASE_KEY is missing from environment. "
            "Please configure them in python-analysis-engine/.env"
        )
        return None

    try:
        from supabase import create_client
        _supabase_client = create_client(supabase_url, supabase_key)
        logger.info("Supabase client initialized successfully.")
        return _supabase_client
    except Exception as e:
        logger.error(f"Failed to initialize Supabase client: {e}")
        return None
