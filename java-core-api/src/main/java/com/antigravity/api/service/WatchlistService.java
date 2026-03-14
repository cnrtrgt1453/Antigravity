package com.antigravity.api.service;

import com.antigravity.api.entity.User;
import com.antigravity.api.entity.Watchlist;

import java.util.List;

/**
 * Watchlist business logic operations.
 */
public interface WatchlistService {
    
    /**
     * Adds a stock symbol to the user's watchlist.
     * @param user The user entity
     * @param stockSymbol The symbol of the stock
     * @return The created Watchlist entry
     */
    Watchlist addToWatchlist(User user, String stockSymbol);
    
    /**
     * Removes a stock symbol from the user's watchlist.
     * @param user The user entity
     * @param stockSymbol The symbol of the stock
     */
    void removeFromWatchlist(User user, String stockSymbol);
    
    /**
     * Retrieves all watchlist entries for a specific user.
     * @param user The user entity
     * @return List of Watchlist entries
     */
    List<Watchlist> getWatchlistByUser(User user);
}
