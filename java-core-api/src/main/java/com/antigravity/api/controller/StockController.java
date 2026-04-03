package com.antigravity.api.controller;

import com.antigravity.api.entity.Stock;
import com.antigravity.api.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@Tag(name = "Stock Controller", description = "Piyasalar ve Hisse Senedi API Uç Noktaları")
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping
    @Operation(summary = "Aktif hisseleri sayfalayarak getirir", description = "Veritabanındaki aktif hisse senetlerini (isActive=true) sayfalama ile döndürür.")
    public ResponseEntity<Page<Stock>> getAllStocks(Pageable pageable) {
        return ResponseEntity.ok(stockService.getActiveStocksPaginated(pageable));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Kategoriye göre hisseleri listeler", description = "Belirtilen kategoriye ait hisse senetlerini döndürür.")
    public ResponseEntity<List<Stock>> getStocksByCategory(@PathVariable String category) {
        return ResponseEntity.ok(stockService.getStocksByCategory(category));
    }
}
