package com.antigravity.api.service;

import com.antigravity.api.entity.News;
import java.util.List;

/**
 * Interface for external news providers.
 */
public interface NewsIntegrationService {
    
    /**
     * Fetches top financial news/KAP entries from the provider.
     */
    List<News> fetchLatestNews();
}
