package com.antigravity.api.service;

import com.antigravity.api.entity.News;
import com.antigravity.api.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

public interface NewsService {

    /**
     * Get paginated and filtered news.
     */
    Page<News> getNews(User user, List<String> symbols, boolean watchlistOnly, Pageable pageable);

    /**
     * Generates a weekly analysis report for the user's watchlist.
     * Only available on Mondays and once per week.
     */
    Map<String, Object> generateWeeklyReport(User user);
}
