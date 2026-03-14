package com.antigravity.api.service.impl;

import com.antigravity.api.entity.User;
import com.antigravity.api.entity.Watchlist;
import com.antigravity.api.repository.WatchlistRepository;
import com.antigravity.api.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchlistServiceImpl implements WatchlistService {

    private final WatchlistRepository watchlistRepository;

    @Override
    @Transactional
    public Watchlist addToWatchlist(User user, String stockSymbol) {
        if (watchlistRepository.existsByUserAndStockSymbol(user, stockSymbol)) {
            // Already exists, return the existing one or throw exception?
            // To be idempotent, we return the existing one.
            return watchlistRepository.findByUserAndStockSymbol(user, stockSymbol).orElse(null);
        }

        Watchlist watchlist = Watchlist.builder()
                .user(user)
                .stockSymbol(stockSymbol)
                .build();

        return watchlistRepository.save(watchlist);
    }

    @Override
    @Transactional
    public void removeFromWatchlist(User user, String stockSymbol) {
        watchlistRepository.findByUserAndStockSymbol(user, stockSymbol)
                .ifPresent(watchlistRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Watchlist> getWatchlistByUser(User user) {
        return watchlistRepository.findByUser(user);
    }
}
