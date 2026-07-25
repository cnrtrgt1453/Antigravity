package com.antigravity.api.client;

import com.antigravity.api.entity.MarketSignal;
import java.util.List;

public interface MarketSignalClient {
    /**
     * Fetches latest golden cross and dead cross signals from python analysis engine.
     */
    List<MarketSignal> fetchLatestSignals();
}
