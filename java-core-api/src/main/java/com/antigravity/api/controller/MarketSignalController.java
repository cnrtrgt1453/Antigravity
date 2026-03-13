package com.antigravity.api.controller;

import com.antigravity.api.entity.MarketSignal;
import com.antigravity.api.repository.MarketSignalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/signals")
@RequiredArgsConstructor
public class MarketSignalController {

    private final MarketSignalRepository marketSignalRepository;

    @GetMapping
    public ResponseEntity<Map<String, List<MarketSignal>>> getLatestSignals() {
        // En son eklenen sinyalleri çek
        List<MarketSignal> allSignals = marketSignalRepository.findByOrderByCreatedAtDesc();
        
        Map<String, List<MarketSignal>> response = new HashMap<>();
        response.put("golden_signals", allSignals.stream()
                .filter(s -> "GOLDEN_CROSS".equals(s.getSignalType()))
                .collect(Collectors.toList()));
        
        response.put("dead_signals", allSignals.stream()
                .filter(s -> "DEAD_CROSS".equals(s.getSignalType()))
                .collect(Collectors.toList()));
        
        return ResponseEntity.ok(response);
    }
}
