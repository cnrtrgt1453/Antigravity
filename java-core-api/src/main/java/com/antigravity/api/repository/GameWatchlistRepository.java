package com.antigravity.api.repository;

import com.antigravity.api.entity.GameWatchlist;
import com.antigravity.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameWatchlistRepository extends JpaRepository<GameWatchlist, Long> {
    List<GameWatchlist> findByUser(User user);
    Optional<GameWatchlist> findByUserAndStockSymbol(User user, String stockSymbol);
    boolean existsByUserAndStockSymbol(User user, String stockSymbol);
}
