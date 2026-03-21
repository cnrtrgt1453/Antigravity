package com.antigravity.api.controller;

import com.antigravity.api.entity.Portfolio;
import com.antigravity.api.entity.TradeHistory;
import com.antigravity.api.entity.User;
import com.antigravity.api.service.TradingService;
import com.antigravity.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {

    private final TradingService tradingService;
    private final UserService userService;

    @GetMapping("/portfolio")
    public ResponseEntity<Portfolio> getPortfolio(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(tradingService.getOrCreatePortfolio(user));
    }

    @PostMapping("/buy")
    public ResponseEntity<?> buyStock(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> request) {
        
        User user = userService.getUserByEmail(userDetails.getUsername());
        String symbol = (String) request.get("symbol");
        Long quantity = Long.valueOf(request.get("quantity").toString());
        BigDecimal price = new BigDecimal(request.get("price").toString());

        try {
            Portfolio portfolio = tradingService.buyStock(user, symbol, quantity, price);
            return ResponseEntity.ok(portfolio);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<?> sellStock(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> request) {
        
        User user = userService.getUserByEmail(userDetails.getUsername());
        String symbol = (String) request.get("symbol");
        Long quantity = Long.valueOf(request.get("quantity").toString());
        BigDecimal price = new BigDecimal(request.get("price").toString());

        try {
            Portfolio portfolio = tradingService.sellStock(user, symbol, quantity, price);
            return ResponseEntity.ok(portfolio);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<TradeHistory>> getHistory(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(tradingService.getTradeHistory(user));
    }

    @GetMapping("/watchlist")
    public ResponseEntity<List<String>> getWatchlist(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(tradingService.getWatchlist(user));
    }

    @PostMapping("/watchlist")
    public ResponseEntity<?> addToWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> request) {
        
        User user = userService.getUserByEmail(userDetails.getUsername());
        tradingService.addToWatchlist(user, request.get("symbol"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/watchlist/{symbol}")
    public ResponseEntity<?> removeFromWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String symbol) {
        
        User user = userService.getUserByEmail(userDetails.getUsername());
        tradingService.removeFromWatchlist(user, symbol);
        return ResponseEntity.ok().build();
    }
}
