package com.antigravity.api.controller;

import com.antigravity.api.entity.Stock;
import com.antigravity.api.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping
    public ResponseEntity<Page<Stock>> getAllStocks(Pageable pageable) {
        return ResponseEntity.ok(stockService.getActiveStocksPaginated(pageable));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Stock>> getAllStocksList() {
        return ResponseEntity.ok(stockService.getAllActiveStocks());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Stock>> getStocksByCategory(@PathVariable String category) {
        return ResponseEntity.ok(stockService.getStocksByCategory(category));
    }
}
