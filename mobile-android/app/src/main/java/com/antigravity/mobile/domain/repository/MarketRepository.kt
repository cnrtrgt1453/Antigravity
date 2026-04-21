package com.antigravity.mobile.domain.repository

import com.antigravity.mobile.domain.model.MarketSummary
import com.antigravity.mobile.domain.model.MarketSignal
import com.antigravity.mobile.domain.model.Stock
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface MarketRepository {
    fun getStocksPagingData(): Flow<PagingData<Stock>>
    suspend fun getMarketSummary(): Result<List<MarketSummary>>
    suspend fun getLatestSignals(): Result<List<MarketSignal>>
    suspend fun toggleWatchlist(symbol: String, isWatched: Boolean): Result<Unit>
}
