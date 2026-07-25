package com.antigravity.api.service;

import com.antigravity.api.entity.*;
import com.antigravity.api.repository.*;
import com.antigravity.api.service.impl.TradingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradingServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;
    @Mock
    private PortfolioItemRepository portfolioItemRepository;
    @Mock
    private TradeHistoryRepository tradeHistoryRepository;
    @Mock
    private GameWatchlistRepository gameWatchlistRepository;

    @InjectMocks
    private TradingServiceImpl tradingService;

    private User mockUser;
    private Portfolio mockPortfolio;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().id(1L).email("test@example.com").build();
        mockPortfolio = Portfolio.builder()
                .id(1L)
                .user(mockUser)
                .balance(new BigDecimal("1000.0000"))
                .build();
    }

    @Test
    void getOrCreatePortfolio_ShouldCreateNew_WhenNotExists() {
        when(portfolioRepository.findByUser(mockUser)).thenReturn(Optional.empty());
        when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(i -> i.getArgument(0));

        Portfolio result = tradingService.getOrCreatePortfolio(mockUser);

        assertNotNull(result);
        assertEquals(new BigDecimal("750000.0000"), result.getBalance());
        verify(portfolioRepository).save(any(Portfolio.class));
    }

    @Test
    void buyStock_ShouldSucceed_WhenBalanceIsEnough() {
        String symbol = "THYAO";
        Long quantity = 10L;
        BigDecimal price = new BigDecimal("10.0000"); // Total: 100
        // Commission: 100 * 0.01 = 1.00
        // Total deduction: 101.00

        when(portfolioRepository.findByUser(mockUser)).thenReturn(Optional.of(mockPortfolio));
        when(portfolioItemRepository.findByPortfolioAndStockSymbol(mockPortfolio, symbol)).thenReturn(Optional.empty());
        when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(i -> i.getArgument(0));

        Portfolio result = tradingService.buyStock(mockUser, symbol, quantity, price);

        assertEquals(new BigDecimal("899.0000"), result.getBalance());
        verify(portfolioItemRepository).save(any(PortfolioItem.class));
        verify(tradeHistoryRepository).save(any(TradeHistory.class));
    }

    @Test
    void buyStock_ShouldThrowException_WhenBalanceIsInsufficient() {
        String symbol = "THYAO";
        Long quantity = 1000L;
        BigDecimal price = new BigDecimal("10.0000"); // Total: 10000

        when(portfolioRepository.findByUser(mockUser)).thenReturn(Optional.of(mockPortfolio));

        assertThrows(RuntimeException.class, () -> tradingService.buyStock(mockUser, symbol, quantity, price));
    }

    @Test
    void sellStock_ShouldSucceed_WhenQuantityIsEnough() {
        String symbol = "THYAO";
        Long quantity = 5L;
        BigDecimal price = new BigDecimal("20.0000"); // Total: 100
        // Commission: 100 * 0.01 = 1.00
        // Net proceeds: 99.00

        PortfolioItem item = PortfolioItem.builder()
                .portfolio(mockPortfolio)
                .stockSymbol(symbol)
                .quantity(10L)
                .averageCost(new BigDecimal("10.0000"))
                .build();

        when(portfolioRepository.findByUser(mockUser)).thenReturn(Optional.of(mockPortfolio));
        when(portfolioItemRepository.findByPortfolioAndStockSymbol(mockPortfolio, symbol)).thenReturn(Optional.of(item));
        when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(i -> i.getArgument(0));

        Portfolio result = tradingService.sellStock(mockUser, symbol, quantity, price);

        assertEquals(new BigDecimal("1099.0000"), result.getBalance());
        assertEquals(5L, item.getQuantity());
        verify(tradeHistoryRepository).save(any(TradeHistory.class));
    }

    @Test
    void sellStock_ShouldThrowException_WhenNotOwned() {
        when(portfolioRepository.findByUser(mockUser)).thenReturn(Optional.of(mockPortfolio));
        when(portfolioItemRepository.findByPortfolioAndStockSymbol(mockPortfolio, "ANY")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> tradingService.sellStock(mockUser, "ANY", 1L, BigDecimal.ONE));
    }

    @Test
    void addToWatchlist_ShouldSave_WhenNotExists() {
        String symbol = "THYAO";
        when(gameWatchlistRepository.existsByUserAndStockSymbol(mockUser, symbol)).thenReturn(false);

        tradingService.addToWatchlist(mockUser, symbol);

        verify(gameWatchlistRepository).save(any(GameWatchlist.class));
    }

    @Test
    void removeFromWatchlist_ShouldDelete_WhenExists() {
        String symbol = "THYAO";
        GameWatchlist watchlist = GameWatchlist.builder().user(mockUser).stockSymbol(symbol).build();
        when(gameWatchlistRepository.findByUserAndStockSymbol(mockUser, symbol)).thenReturn(Optional.of(watchlist));

        tradingService.removeFromWatchlist(mockUser, symbol);

        verify(gameWatchlistRepository).delete(watchlist);
    }
}
