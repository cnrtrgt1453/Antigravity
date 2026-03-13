# Python Analysis Engine

This is the Python-based analysis engine designed to fetch stock data (BIST, Gold, Silver) using `yfinance` and perform Moving Average (SMA50/200) Golden and Dead Cross pattern recognitions.

## Prerequisites
- Python 3.9+
- pip

## Quickstart

1. Clone the repository and navigate to this folder.
2. Create and activate a virtual environment:
   ```bash
   py -m venv .venv
   .\.venv\Scripts\Activate.ps1
   ```
   *(If you get a script execution policy error on Windows, run `Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser` as Administrator or run the `python.exe` inside the `.venv` directly.)*

3. Install requirements:
   ```bash
   pip install -r requirements.txt
   ```

4. Start the FastAPI development server:
   ```bash
   uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
   ```

5. Open your browser and navigate to the Swagger UI to test the endpoints:
   - http://localhost:8000/docs
   
## Scheduled Jobs
The engine is configured to automatically scan the market **every Monday at 07:00 AM** and output the results for BIST stocks, Gold, and Silver.
