package com.antigravity.api.repository;

import com.antigravity.api.entity.Stock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class StockRepositoryTest {

    @Autowired
    private StockRepository stockRepository;

    @Test
    void shouldSaveAndFindStockBySymbol() {
        Stock stock = Stock.builder()
                .symbol("THYAO")
                .name("Türk Hava Yolları")
                .isActive(true)
                .category("BIST100")
                .build();

        stockRepository.save(stock);

        Optional<Stock> found = stockRepository.findBySymbol("THYAO");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Türk Hava Yolları");
    }

    @Test
    void shouldReturnEmptyWhenStockNotFound() {
        Optional<Stock> found = stockRepository.findBySymbol("NON_EXISTENT");
        assertThat(found).isEmpty();
    }
}
