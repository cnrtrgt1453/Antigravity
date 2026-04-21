package com.antigravity.mobile.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.antigravity.mobile.data.paging.StockPagingSource
import com.antigravity.mobile.domain.model.MarketSignal
import com.antigravity.mobile.domain.model.MarketSummary
import com.antigravity.mobile.domain.model.Stock
import com.antigravity.mobile.domain.repository.MarketRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ApiMarketRepository @Inject constructor(
    private val client: HttpClient
) : MarketRepository {

    private val javaBaseUrl = "http://10.0.2.2:8080"
    private val pythonBaseUrl = "http://10.0.2.2:8000"

    override fun getStocksPagingData(): Flow<PagingData<Stock>> {
        return Pager(
            config = PagingConfig(pageSize = 20, prefetchDistance = 2),
            pagingSourceFactory = { StockPagingSource(client, javaBaseUrl, pythonBaseUrl) }
        ).flow
    }

    override suspend fun getMarketSummary(): Result<List<MarketSummary>> {
        return try {
            // Mocking summary data for now as Truncgil response needs specific mapping
            val summary = listOf(
                MarketSummary("USD", "Dolar", "32.45", "%0.12", true),
                MarketSummary("GA", "Gram Altın", "2450.00", "%0.45", true),
                MarketSummary("XU100", "BIST 100", "9850.00", "%-0.20", false)
            )
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLatestSignals(): Result<List<MarketSignal>> {
        return try {
            val response = client.get("$pythonBaseUrl/api/v1/analysis/latest_signals").body<Map<String, List<MarketSignal>>>()
            val golden = response["golden_signals"] ?: emptyList()
            val dead = response["dead_signals"] ?: emptyList()
            Result.success(golden + dead)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleWatchlist(symbol: String, isWatched: Boolean): Result<Unit> {
        // Implementation for Java API /api/game/watchlist
        return Result.success(Unit)
    }
}
