package com.antigravity.api.service.impl;

import com.antigravity.api.entity.Stock;
import com.antigravity.api.repository.StockRepository;
import com.antigravity.api.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;

    @Override
    public List<Stock> getAllActiveStocks() {
        return stockRepository.findByIsActiveTrue();
    }

    @Override
    public Page<Stock> getActiveStocksPaginated(Pageable pageable) {
        return stockRepository.findByIsActiveTrue(pageable);
    }

    @Override
    public List<Stock> getStocksByCategory(String category) {
        return stockRepository.findByCategory(category);
    }

    @Override
    public Stock getStockBySymbol(String symbol) {
        return stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new RuntimeException("Stock not found: " + symbol));
    }

    @Override
    @Transactional
    public void saveAll(List<Stock> stocks) {
        stockRepository.saveAll(stocks);
    }

    @Override
    public long countStocks() {
        return stockRepository.count();
    }
}
