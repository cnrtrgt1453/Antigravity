package com.antigravity.api.repository;

import com.antigravity.api.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    Optional<News> findByExternalUid(String externalUid);

    boolean existsByExternalUid(String externalUid);

    Page<News> findAllByStockSymbol(String stockSymbol, Pageable pageable);

    @Query("SELECT n FROM News n WHERE n.stockSymbol IN " +
           "(SELECT w.stock.symbol FROM Watchlist w WHERE w.user.id = :userId)")
    Page<News> findByUserIdWatchlist(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT n FROM News n WHERE n.stockSymbol = :symbol AND n.stockSymbol IN " +
           "(SELECT w.stock.symbol FROM Watchlist w WHERE w.user.id = :userId)")
    Page<News> findByUserIdWatchlistAndSymbol(@Param("userId") Long userId, @Param("symbol") String symbol, Pageable pageable);

    Page<News> findAllByStockSymbolIn(List<String> symbols, Pageable pageable);

    List<News> findAllByStockSymbolInAndPublishedAtAfter(List<String> symbols, LocalDateTime after);
}
