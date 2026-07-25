package com.antigravity.api.client;

import com.antigravity.api.entity.MarketSignal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class HttpMarketSignalClient implements MarketSignalClient {

    private final RestTemplate restTemplate;

    @Value("${external.python-analysis-url}")
    private String pythonServiceUrl;

    @Override
    @SuppressWarnings("unchecked")
    public List<MarketSignal> fetchLatestSignals() {
        List<MarketSignal> signalsList = new ArrayList<>();
        try {
            Map<?, ?> rawResponse = restTemplate.getForObject(pythonServiceUrl + "/latest_signals", Map.class);
            Map<String, List<Map<String, Object>>> response = (Map<String, List<Map<String, Object>>>) rawResponse;

            if (response != null) {
                parseAndAdd(signalsList, response.get("golden_signals"), "GOLDEN_CROSS");
                parseAndAdd(signalsList, response.get("dead_signals"), "DEAD_CROSS");
            }
        } catch (Exception e) {
            log.error("Python servisine erişilirken veya sinyaller işlenirken hata: ", e);
        }
        return signalsList;
    }

    private void parseAndAdd(List<MarketSignal> targetList, List<Map<String, Object>> signals, String type) {
        if (signals == null) return;
        for (Map<String, Object> signalData : signals) {
            MarketSignal signal = MarketSignal.builder()
                    .ticker((String) signalData.get("ticker"))
                    .signalType(type)
                    .price(((Number) signalData.get("current_price")).doubleValue())
                    .crossDate((String) signalData.get("cross_date"))
                    .build();
            targetList.add(signal);
        }
    }
}
