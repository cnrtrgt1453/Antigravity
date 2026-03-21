package com.antigravity.api.service;

import com.antigravity.api.entity.User;
import com.antigravity.api.entity.Watchlist;
import com.antigravity.api.entity.Stock;

import java.util.List;

/**
 * Watchlist business logic operations.
 */
public interface WatchlistService {
    
    Watchlist addToWatchlist(User user, Stock stock);
    
    void removeFromWatchlist(User user, Stock stock);
    
    /**
     * Retrieves all watchlist entries for a specific user.
     * @param user The user entity
     * @return List of Watchlist entries
     */
    List<Watchlist> getWatchlistByUser(User user);
}
