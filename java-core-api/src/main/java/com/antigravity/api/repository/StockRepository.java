package com.antigravity.api.repository;

import com.antigravity.api.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findBySymbol(String symbol);
    List<Stock> findByIsActiveTrue();
    Page<Stock> findByIsActiveTrue(Pageable pageable);
    List<Stock> findByCategory(String category);
}
