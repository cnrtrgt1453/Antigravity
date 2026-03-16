package com.antigravity.api.repository;

import com.antigravity.api.entity.Portfolio;
import com.antigravity.api.entity.PortfolioItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, Long> {
    Optional<PortfolioItem> findByPortfolioAndStockSymbol(Portfolio portfolio, String stockSymbol);
}
