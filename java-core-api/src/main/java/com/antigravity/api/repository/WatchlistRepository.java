package com.antigravity.api.repository;

import com.antigravity.api.entity.Watchlist;
import com.antigravity.api.entity.User;
import com.antigravity.api.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {
    
    List<Watchlist> findByUser(User user);
    
    Optional<Watchlist> findByUserAndStock(User user, Stock stock);
    
    void deleteByUserAndStock(User user, Stock stock);
    
    boolean existsByUserAndStock(User user, Stock stock);
}
