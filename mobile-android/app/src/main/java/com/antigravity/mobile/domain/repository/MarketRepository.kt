package com.antigravity.mobile.domain.repository

import com.antigravity.mobile.domain.model.MarketSummary
import com.antigravity.mobile.domain.model.MarketSignal
import com.antigravity.mobile.domain.model.Stock
import com.antigravity.mobile.domain.model.News
import com.antigravity.mobile.domain.model.OHLCData
import com.antigravity.mobile.domain.model.CooldownStatus
import com.antigravity.mobile.domain.model.ScanResponse
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface MarketRepository {
    fun getStocksPagingData(): Flow<PagingData<Stock>>
    suspend fun getMarketSummary(): Result<List<MarketSummary>>
    suspend fun getLatestSignals(): Result<List<MarketSignal>>
    suspend fun toggleWatchlist(symbol: String, isWatched: Boolean): Result<Unit>
    suspend fun getNews(page: Int, watchlistOnly: Boolean, symbol: String?, sort: String): Result<List<News>>
    suspend fun getOHLCData(symbol: String): Result<OHLCData>
    suspend fun triggerFullScan(): Result<ScanResponse>
    suspend fun getCooldownStatus(): Result<CooldownStatus>
    suspend fun getWatchlist(): Result<List<String>>
}
