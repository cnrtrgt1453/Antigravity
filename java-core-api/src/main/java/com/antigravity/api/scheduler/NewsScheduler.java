package com.antigravity.api.scheduler;

import com.antigravity.api.entity.News;
import com.antigravity.api.repository.NewsRepository;
import com.antigravity.api.service.NewsIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewsScheduler {

    private final NewsIntegrationService newsIntegrationService;
    private final NewsRepository newsRepository;

    /**
     * Hafta içi her gün borsa kapandığında (18:15) haberleri çeker.
     * Cron: "0 15 18 * * MON-FRI"
     */
    @Scheduled(cron = "0 15 18 * * MON-FRI")
    @Transactional
    public void scheduleNewsSync() {
        log.info("Haber senkronizasyonu başlatıldı (18:15 Pazartesi-Cuma)");
        syncNews();
    }

    public void syncNews() {
        try {
            List<News> latestNews = newsIntegrationService.fetchLatestNews();
            int newRecordsCount = 0;

            for (News news : latestNews) {
                if (!newsRepository.existsByExternalUid(news.getExternalUid())) {
                    newsRepository.save(news);
                    newRecordsCount++;
                }
            }

            log.info("Haber senkronizasyonu tamamlandı. {} yeni haber eklendi.", newRecordsCount);
        } catch (Exception e) {
            log.error("Haber senkronizasyonu sırasında hata oluştu: {}", e.getMessage());
        }
    }
}
