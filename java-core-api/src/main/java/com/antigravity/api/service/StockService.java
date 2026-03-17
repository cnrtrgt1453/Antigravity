package com.antigravity.api.service;

import com.antigravity.api.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StockService {
    List<Stock> getAllActiveStocks();
    Page<Stock> getActiveStocksPaginated(Pageable pageable);
    List<Stock> getStocksByCategory(String category);
    Stock getStockBySymbol(String symbol);
    void saveAll(List<Stock> stocks);
    long countStocks();
}
