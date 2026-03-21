package com.antigravity.api.service;

import com.antigravity.api.entity.MarketSignal;
import com.antigravity.api.repository.MarketSignalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketAnalysisService {

    private final MarketSignalRepository marketSignalRepository;
    private final RestTemplate restTemplate;

    @Value("${external.python-analysis-url}")
    private String pythonServiceUrl;

    /**
     * Her Pazartesi saat 07:05'te çalışır.
     * (Python motorunun 07:00'deki taramasının bitmesi için 5 dakika beklendi)
     */
    @Scheduled(cron = "0 5 7 * * MON")
    public void scheduleWeeklyAnalysis() {
        log.info("Haftalık piyasa tarama sonuçları kaydediliyor...");
        fetchAndSaveSignals();
    }

    public void fetchAndSaveSignals() {
        try {
            // Önce Python tarafında taramayı tetikle (cooldown nedeniyle gerekmeyebilir ama garanti olsun)
            // restTemplate.getForObject(PYTHON_SERVICE_URL + "/run_full_scan_now", Map.class);
            
            // En son sinyalleri getir
            Map<String, List<Map<String, Object>>> response = restTemplate.getForObject(
                pythonServiceUrl + "/latest_signals", 
                Map.class
            );

            if (response != null) {
                saveSignals(response.get("golden_signals"), "GOLDEN_CROSS");
                saveSignals(response.get("dead_signals"), "DEAD_CROSS");
                log.info("Sinyaller başarıyla veritabanına kaydedildi.");
            }
        } catch (Exception e) {
            log.error("Sinyalleri kaydederken hata oluştu: ", e);
        }
    }

    private void saveSignals(List<Map<String, Object>> signals, String type) {
        if (signals == null) return;
        
        for (Map<String, Object> signalData : signals) {
            MarketSignal signal = MarketSignal.builder()
                .ticker((String) signalData.get("ticker"))
                .signalType(type)
                .price(((Number) signalData.get("current_price")).doubleValue())
                .crossDate((String) signalData.get("cross_date"))
                .build();
            
            marketSignalRepository.save(signal);
        }
    }
}
