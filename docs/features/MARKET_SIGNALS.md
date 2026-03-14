# Feature: Market Signals (Golden/Dead Cross)

Automated technical analysis to identify potential buying or selling opportunities in the BIST market.

## Analysis Logic
- **Indicators**: 50-day and 200-day Simple Moving Averages (SMA).
- **Golden Cross**: 50-day SMA crosses above 200-day SMA.
- **Dead Cross**: 50-day SMA crosses below 200-day SMA.

## System Interaction
1. **Python Engine**: calculates SMA and identifies crossovers using Pandas.
2. **Java Backend**: consumes results and persists them to `MarketSignal` table.
3. **Frontend**: displays signals in the dashboard with price and date info.

## Performance & Optimization
- **Scan Cooldown**: 12-hour wait between manual full scans to reduce server load.
- **Historical Tracking**: Data is archived weekly in PostgreSQL for trend analysis.
