package com.antigravity.api.service.impl;

import com.antigravity.api.entity.News;
import com.antigravity.api.service.NewsIntegrationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MockNewsIntegrationService implements NewsIntegrationService {

    @Override
    public List<News> fetchLatestNews() {
        List<News> newsList = new ArrayList<>();
        
        // Mock data for initial implementation
        String[] tickers = {"THYAO", "EREGL", "ASELS", "SISE", "SASA"};
        String[] titles = {
            " bilançosunu açıkladı: Kâr beklentileri aştı!",
            " yeni yatırım planlarını duyurdu.",
            " ihracat rekoru kırmaya devam ediyor.",
            " hakkında yeni teknik analiz raporu yayınlandı.",
            " piyasalarındaki son durum analizi."
        };

        for (int i = 0; i < tickers.length; i++) {
            newsList.add(News.builder()
                    .title(tickers[i] + titles[i])
                    .content(tickers[i] + " için 2024 yılı stratejik hedefleri ve pazar analizi detayları.")
                    .publishedAt(LocalDateTime.now().minusHours(i * 2))
                    .stockSymbol(tickers[i])
                    .externalUid(UUID.randomUUID().toString()) // Real provider would have a stable ID
                    .sourceUrl("https://kap.org.tr/mock-news/" + i)
                    .build());
        }

        return newsList;
    }
}
