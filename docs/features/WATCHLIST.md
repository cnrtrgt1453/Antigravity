# Feature: Watchlist (Takip Listesi)

The Watchlist feature allows users to track specific stock symbols and receive personalized results in their news feeds.

## Technical Details

### Backend Structure
- **Entity**: `Watchlist`
- **Relationship**: Many-to-One with `User`.
- **Database**: PostgreSQL table `watchlist`.
- **Optimization**: Unique composite index on `(user_id, stock_symbol)`.

### Core Operations
- `POST /api/v1/watchlist/add`: Adds a symbol.
- `DELETE /api/v1/watchlist/remove`: Removes a symbol.
- `GET /api/v1/watchlist/list`: Lists user symbols.

## SOLID Principles
- **SRP**: Watchlist logic is isolated from the `User` entity and managed via a dedicated `WatchlistService`.
- **Relationship**: Decoupled design allows for future scale (e.g., price alerts).
