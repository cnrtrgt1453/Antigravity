package com.antigravity.api.controller;

import com.antigravity.api.entity.Stock;
import com.antigravity.api.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = StockController.class)
@AutoConfigureMockMvc(addFilters = false) // Güvenlik katmanını testte pas geçiyoruz
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StockService stockService;

    private Stock mockStock;

    @BeforeEach
    void setUp() {
        mockStock = new Stock();
        mockStock.setSymbol("AAPL");
        mockStock.setCategory("TECH");
        mockStock.setIsActive(true);
    }

    @Test
    @WithMockUser
    void getAllStocks_ReturnsPaginatedStocks() throws Exception {
        Page<Stock> page = new PageImpl<>(List.of(mockStock));
        when(stockService.getActiveStocksPaginated(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/stocks")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].symbol").value("AAPL"));
    }

    @Test
    @WithMockUser
    void getStocksByCategory_ReturnsList() throws Exception {
        when(stockService.getStocksByCategory("TECH")).thenReturn(List.of(mockStock));

        mockMvc.perform(get("/api/v1/stocks/category/TECH")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("AAPL"));
    }
}
