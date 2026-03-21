package com.antigravity.api.service;

import com.antigravity.api.entity.Portfolio;
import com.antigravity.api.entity.TradeHistory;
import com.antigravity.api.entity.User;

import java.math.BigDecimal;
import java.util.List;

public interface TradingService {
    Portfolio getOrCreatePortfolio(User user);
    Portfolio buyStock(User user, String symbol, Long quantity, BigDecimal price);
    Portfolio sellStock(User user, String symbol, Long quantity, BigDecimal price);
    List<TradeHistory> getTradeHistory(User user);
    
    // Watchlist methods
    void addToWatchlist(User user, String symbol);
    void removeFromWatchlist(User user, String symbol);
    List<String> getWatchlist(User user);
}
