# Feature: News Module (Haberler)

The News module provides timely financial information and personalized analysis reports for tracked stocks.

## Architecture

### Scheduling
- **Frequency**: Weekdays at 18:15 (Cron: `0 15 18 * * MON-FRI`).
- **Service**: `NewsScheduler`.
- **Logic**: Incremental updates based on unique news UIDs from providers (e.g., KAP).

### News Personalization
- **Personalized Feed**: Uses SQL JOINs to filter news based on the user's `Watchlist`.
- **Frontend UI**:
    - **Tab Toggle**: Switch between "My Watchlist" and "All News".
    - **Symbol Filters**: Horizontal chips for specific stocks in user watchlist.
    - **Sorting**: Toggle between Newest/Oldest.
    - **Pagination**: Optimized with infinite scroll.

## Weekly Analysis Report
- **Activation**: Monday only.
- **Exception**: First-time users can access it any day.
- **Content**: Summarizes news activity for watchlist stocks over the last 30 days.
- **Persistence**: Tracks `lastReportDate` on the `User` entity to enforce once-per-week rules.
