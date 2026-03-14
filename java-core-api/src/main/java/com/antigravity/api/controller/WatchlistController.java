package com.antigravity.api.controller;

import com.antigravity.api.entity.User;
import com.antigravity.api.entity.Watchlist;
import com.antigravity.api.service.WatchlistService;
import com.antigravity.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;
    private final UserService userService;

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // Spring Security puts email or UID in principal
        
        // If authentication is FirebaseAuthenticationToken, we might need a more specific way to get the user.
        // For now, we assume authentication.getName() returns the identifier we can use to find the user.
        // Given the existing UserController, we might need to find by firebaseUid or email.
        // Let's assume finding by email for simplicity, or we can use a dedicated helper.
        return userService.getUserByEmail(email);
    }

    @PostMapping("/add")
    public ResponseEntity<Watchlist> addToWatchlist(@RequestBody Map<String, String> request) {
        String symbol = request.get("symbol");
        User user = getAuthenticatedUser();
        Watchlist watchlist = watchlistService.addToWatchlist(user, symbol);
        return ResponseEntity.ok(watchlist);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<Void> removeFromWatchlist(@RequestBody Map<String, String> request) {
        String symbol = request.get("symbol");
        User user = getAuthenticatedUser();
        watchlistService.removeFromWatchlist(user, symbol);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/list")
    public ResponseEntity<List<String>> getWatchlist() {
        User user = getAuthenticatedUser();
        List<String> symbols = watchlistService.getWatchlistByUser(user)
                .stream()
                .map(Watchlist::getStockSymbol)
                .collect(Collectors.toList());
        return ResponseEntity.ok(symbols);
    }
}
