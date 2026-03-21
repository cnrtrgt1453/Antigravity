package com.antigravity.api.service.impl;

import com.antigravity.api.entity.*;
import com.antigravity.api.repository.*;
import com.antigravity.api.service.TradingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradingServiceImpl implements TradingService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final TradeHistoryRepository tradeHistoryRepository;
    private final GameWatchlistRepository gameWatchlistRepository;

    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.01"); // %1

    @Override
    @Transactional
    public Portfolio getOrCreatePortfolio(User user) {
        return portfolioRepository.findByUser(user)
                .orElseGet(() -> {
                    Portfolio portfolio = Portfolio.builder()
                            .user(user)
                            .balance(new BigDecimal("750000.0000"))
                            .build();
                    return portfolioRepository.save(portfolio);
                });
    }

    @Override
    @Transactional
    public Portfolio buyStock(User user, String symbol, Long quantity, BigDecimal price) {
        Portfolio portfolio = getOrCreatePortfolio(user);
        
        BigDecimal totalVolume = price.multiply(BigDecimal.valueOf(quantity));
        BigDecimal commission = totalVolume.multiply(COMMISSION_RATE).setScale(4, RoundingMode.HALF_UP);
        BigDecimal totalDeduction = totalVolume.add(commission);

        if (portfolio.getBalance().compareTo(totalDeduction) < 0) {
            throw new RuntimeException("Yetersiz bakiye! İşlem masrafları dahil " + totalDeduction + " TL gerekiyor.");
        }

        // 1. Update Balance
        portfolio.setBalance(portfolio.getBalance().subtract(totalDeduction));

        // 2. Update Portfolio Item
        PortfolioItem item = portfolioItemRepository.findByPortfolioAndStockSymbol(portfolio, symbol)
                .orElse(PortfolioItem.builder()
                        .portfolio(portfolio)
                        .stockSymbol(symbol)
                        .quantity(0L)
                        .averageCost(BigDecimal.ZERO)
                        .build());

        BigDecimal currentTotalCost = item.getAverageCost().multiply(BigDecimal.valueOf(item.getQuantity()));
        Long newQuantity = item.getQuantity() + quantity;
        BigDecimal newTotalCost = currentTotalCost.add(totalVolume);
        BigDecimal newAverageCost = newTotalCost.divide(BigDecimal.valueOf(newQuantity), 4, RoundingMode.HALF_UP);

        item.setQuantity(newQuantity);
        item.setAverageCost(newAverageCost);
        portfolioItemRepository.save(item);

        // 3. Save History
        saveHistory(user, symbol, TradeHistory.TradeType.BUY, quantity, price, commission, totalDeduction);

        return portfolioRepository.save(portfolio);
    }

    @Override
    @Transactional
    public Portfolio sellStock(User user, String symbol, Long quantity, BigDecimal price) {
        Portfolio portfolio = getOrCreatePortfolio(user);
        
        PortfolioItem item = portfolioItemRepository.findByPortfolioAndStockSymbol(portfolio, symbol)
                .orElseThrow(() -> new RuntimeException("Bu hisse portföyünüzde bulunmuyor."));

        if (item.getQuantity() < quantity) {
            throw new RuntimeException("Yetersiz hisse miktarı! Elinizde " + item.getQuantity() + " adet var.");
        }

        BigDecimal totalVolume = price.multiply(BigDecimal.valueOf(quantity));
        BigDecimal commission = totalVolume.multiply(COMMISSION_RATE).setScale(4, RoundingMode.HALF_UP);
        BigDecimal netProceeds = totalVolume.subtract(commission);

        // 1. Update Balance
        portfolio.setBalance(portfolio.getBalance().add(netProceeds));

        // 2. Update Portfolio Item
        item.setQuantity(item.getQuantity() - quantity);
        if (item.getQuantity() == 0) {
            portfolioItemRepository.delete(item);
        } else {
            portfolioItemRepository.save(item);
        }

        // 3. Save History
        saveHistory(user, symbol, TradeHistory.TradeType.SELL, quantity, price, commission, netProceeds);

        return portfolioRepository.save(portfolio);
    }

    @Override
    public List<TradeHistory> getTradeHistory(User user) {
        return tradeHistoryRepository.findByUserOrderByTimestampDesc(user);
    }

    @Override
    @Transactional
    public void addToWatchlist(User user, String symbol) {
        if (!gameWatchlistRepository.existsByUserAndStockSymbol(user, symbol)) {
            GameWatchlist watchlist = GameWatchlist.builder()
                    .user(user)
                    .stockSymbol(symbol)
                    .build();
            gameWatchlistRepository.save(watchlist);
        }
    }

    @Override
    @Transactional
    public void removeFromWatchlist(User user, String symbol) {
        gameWatchlistRepository.findByUserAndStockSymbol(user, symbol)
                .ifPresent(gameWatchlistRepository::delete);
    }

    @Override
    public List<String> getWatchlist(User user) {
        return gameWatchlistRepository.findByUser(user).stream()
                .map(GameWatchlist::getStockSymbol)
                .collect(Collectors.toList());
    }

    private void saveHistory(User user, String symbol, TradeHistory.TradeType type, Long quantity, BigDecimal price, BigDecimal commission, BigDecimal totalAmount) {
        TradeHistory history = TradeHistory.builder()
                .user(user)
                .stockSymbol(symbol)
                .type(type)
                .quantity(quantity)
                .price(price)
                .commission(commission)
                .totalAmount(totalAmount)
                .build();
        tradeHistoryRepository.save(history);
    }
}
