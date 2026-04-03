package com.antigravity.api.service.impl;

import com.antigravity.api.entity.News;
import com.antigravity.api.entity.User;
import com.antigravity.api.repository.NewsRepository;
import com.antigravity.api.repository.UserRepository;
import com.antigravity.api.repository.WatchlistRepository;
import com.antigravity.api.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final NewsRepository newsRepository;
    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<News> getNews(User user, List<String> symbols, boolean watchlistOnly, Pageable pageable) {
        if (watchlistOnly) {
            if (symbols != null && !symbols.isEmpty()) {
                // Not: Repository'de InAndWatchlist metodu yok, şimdilik sadece ilki için veya
                // logic geliştirilmeli.
                // Basitlik adına tekli hali koruyabiliriz veya IN sorgusu yazabiliriz.
                return newsRepository.findByUserIdWatchlistAndSymbol(user.getId(), symbols.get(0), pageable);
            }
            return newsRepository.findByUserIdWatchlist(user.getId(), pageable);
        } else {
            if (symbols != null && !symbols.isEmpty()) {
                return newsRepository.findAllByStockSymbolIn(symbols, pageable);
            }
            return newsRepository.findAll(pageable);
        }
    }

    @Override
    @Transactional
    public Map<String, Object> generateWeeklyReport(User user) {
        LocalDateTime now = LocalDateTime.now();
        boolean isFirstTime = user.getLastReportDate() == null;

        // 1. Pazartesi kısıtlaması (İlk kez alanlar için esnetildi)
        if (now.getDayOfWeek() != DayOfWeek.MONDAY && !isFirstTime) {
            throw new IllegalStateException("Haftalık analiz raporu sadece Pazartesi günleri alınabilir.");
        }

        // 2. Haftalık kısıtlama (lastReportDate kontrolü)
        if (!isFirstTime) {
            long daysSinceLastReport = ChronoUnit.DAYS.between(user.getLastReportDate(), now);
            if (daysSinceLastReport < 7) {
                throw new IllegalStateException(
                        "Bu haftalık raporu zaten aldınız. Bir sonraki Pazartesi tekrar deneyin.");
            }
        }

        // 3. Watchlist Çekme
        List<String> watchlistSymbols = watchlistRepository.findByUser(user)
                .stream()
                .map(w -> w.getStock().getSymbol())
                .collect(Collectors.toList());

        if (watchlistSymbols.isEmpty()) {
            throw new IllegalStateException("Takip listeniz boş. Rapor oluşturmak için önce hisse eklemelisiniz.");
        }

        // 4. Son 1 Ayın Haberlerini Çekme
        LocalDateTime oneMonthAgo = now.minusMonths(1);
        List<News> monthlyNews = newsRepository.findAllByStockSymbolInAndPublishedAtAfter(watchlistSymbols,
                oneMonthAgo);

        // 5. Rapor Oluşturma (Mock Analiz)
        Map<String, Object> report = new HashMap<>();
        report.put("generationDate", now);
        report.put("watchlistCount", watchlistSymbols.size());
        report.put("newsCount", monthlyNews.size());

        Map<String, Integer> newsPerSymbol = new HashMap<>();
        for (String symbol : watchlistSymbols) {
            long count = monthlyNews.stream().filter(n -> n.getStockSymbol().equals(symbol)).count();
            newsPerSymbol.put(symbol, (int) count);
        }
        report.put("details", newsPerSymbol);
        report.put("summary", "Takip listenizdeki " + watchlistSymbols.size() + " hisse için son 1 ayda toplam "
                + monthlyNews.size() + " haber analiz edildi.");

        // 6. lastReportDate Güncelleme
        user.setLastReportDate(now);
        userRepository.save(user);

        return report;
    }
}
