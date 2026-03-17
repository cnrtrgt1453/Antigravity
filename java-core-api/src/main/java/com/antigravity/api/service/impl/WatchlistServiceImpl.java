package com.antigravity.api.service.impl;

import com.antigravity.api.entity.User;
import com.antigravity.api.entity.Watchlist;
import com.antigravity.api.entity.Stock;
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
    public Watchlist addToWatchlist(User user, Stock stock) {
        if (watchlistRepository.existsByUserAndStock(user, stock)) {
            return watchlistRepository.findByUserAndStock(user, stock).orElse(null);
        }

        Watchlist watchlist = Watchlist.builder()
                .user(user)
                .stock(stock)
                .build();

        return watchlistRepository.save(watchlist);
    }

    @Override
    @Transactional
    public void removeFromWatchlist(User user, Stock stock) {
        watchlistRepository.findByUserAndStock(user, stock)
                .ifPresent(watchlistRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Watchlist> getWatchlistByUser(User user) {
        return watchlistRepository.findByUser(user);
    }
}
