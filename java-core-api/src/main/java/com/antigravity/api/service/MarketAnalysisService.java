package com.antigravity.api.service;

import com.antigravity.api.client.MarketSignalClient;
import com.antigravity.api.entity.MarketSignal;
import com.antigravity.api.repository.MarketSignalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketAnalysisService {

    private final MarketSignalRepository marketSignalRepository;
    private final MarketSignalClient marketSignalClient;

    /**
     * Her Pazartesi saat 07:05'te çalışır.
     */
    @Scheduled(cron = "0 5 7 * * MON")
    public void scheduleWeeklyAnalysis() {
        log.info("Haftalık piyasa tarama sonuçları kaydediliyor...");
        fetchAndSaveSignals();
    }

    public void fetchAndSaveSignals() {
        try {
            List<MarketSignal> signals = marketSignalClient.fetchLatestSignals();
            if (signals != null && !signals.isEmpty()) {
                marketSignalRepository.saveAll(signals);
                log.info("{} adet sinyal veritabanına başarıyla kaydedildi.", signals.size());
            } else {
                log.info("Kaydedilecek yeni sinyal bulunamadı.");
            }
        } catch (Exception e) {
            log.error("Sinyalleri kaydederken hata oluştu: ", e);
        }
    }
}
