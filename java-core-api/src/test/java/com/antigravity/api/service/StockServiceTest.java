package com.antigravity.api.service;

import com.antigravity.api.entity.Stock;
import com.antigravity.api.repository.StockRepository;
import com.antigravity.api.service.impl.StockServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private StockServiceImpl stockService;

    private Stock mockStock;

    @BeforeEach
    void setUp() {
        mockStock = new Stock();
        mockStock.setId(1L);
        mockStock.setSymbol("AAPL");
        mockStock.setCategory("TECH");
        mockStock.setIsActive(true);
    }

    @Test
    void getActiveStocksPaginated_ReturnsPageOfStocks() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Stock> page = new PageImpl<>(List.of(mockStock));
        
        when(stockRepository.findByIsActiveTrue(pageable)).thenReturn(page);

        Page<Stock> result = stockService.getActiveStocksPaginated(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("AAPL", result.getContent().get(0).getSymbol());
        verify(stockRepository).findByIsActiveTrue(pageable);
    }

    @Test
    void getStockBySymbol_WhenExists_ReturnsStock() {
        when(stockRepository.findBySymbol("AAPL")).thenReturn(Optional.of(mockStock));

        Stock result = stockService.getStockBySymbol("AAPL");

        assertNotNull(result);
        assertEquals("AAPL", result.getSymbol());
    }

    @Test
    void getStockBySymbol_WhenNotExists_ThrowsException() {
        when(stockRepository.findBySymbol("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> stockService.getStockBySymbol("UNKNOWN"));
    }
}
