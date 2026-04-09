package com.antigravity.api.service;

import com.antigravity.api.entity.MarketSignal;
import com.antigravity.api.repository.MarketSignalRepository;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@WireMockTest(httpPort = 8089)
class MarketAnalysisIntegrationTest {

    @Autowired
    private MarketAnalysisService marketAnalysisService;

    @Autowired
    private MarketSignalRepository marketSignalRepository;

    @Test
    void shouldFetchAndSaveSignalsFromPythonApi() {
        // Mock Python API response
        stubFor(get(urlEqualTo("/latest_signals"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{" +
                    "\"golden_signals\": [{\"ticker\": \"THYAO\", \"current_price\": 250.5, \"cross_date\": \"2024-03-20\"}]," +
                    "\"dead_signals\": [{\"ticker\": \"ASELS\", \"current_price\": 60.2, \"cross_date\": \"2024-03-15\"}]" +
                    "}")));

        // Execute service method
        marketAnalysisService.fetchAndSaveSignals();

        // Verify data in H2 database
        List<MarketSignal> allSignals = marketSignalRepository.findAll();
        assertThat(allSignals).hasSize(2);
        
        MarketSignal golden = allSignals.stream()
            .filter(s -> s.getSignalType().equals("GOLDEN_CROSS"))
            .findFirst().orElseThrow();
        assertThat(golden.getTicker()).isEqualTo("THYAO");
        assertThat(golden.getPrice()).isEqualTo(250.5);

        MarketSignal dead = allSignals.stream()
            .filter(s -> s.getSignalType().equals("DEAD_CROSS"))
            .findFirst().orElseThrow();
        assertThat(dead.getTicker()).isEqualTo("ASELS");
    }
}
