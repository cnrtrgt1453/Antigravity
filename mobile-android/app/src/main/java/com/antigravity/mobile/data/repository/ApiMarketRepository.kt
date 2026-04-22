package com.antigravity.mobile.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.antigravity.mobile.data.paging.StockPagingSource
import com.antigravity.mobile.domain.model.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
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
            val response = client.get("https://finans.truncgil.com/today.json").body<Map<String, JsonElement>>()
            val summary = mutableListOf<MarketSummary>()
            
            val parsePrice = { key: String ->
                val obj = response[key]?.jsonObject
                val priceStr = obj?.get("Alış")?.jsonPrimitive?.content ?: "0"
                priceStr.replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
            }

            val parseChange = { key: String ->
                val obj = response[key]?.jsonObject
                obj?.get("Değişim")?.jsonPrimitive?.content ?: "0"
            }

            // USD
            summary.add(MarketSummary("USD", "Dolar", String.format("%.2f", parsePrice("USD")), parseChange("USD"), !parseChange("USD").startsWith("%-")))
            // EUR
            summary.add(MarketSummary("EUR", "Euro", String.format("%.2f", parsePrice("EUR")), parseChange("EUR"), !parseChange("EUR").startsWith("%-")))
            // Gold
            summary.add(MarketSummary("GA", "Gram Altın", String.format("%.2f", parsePrice("gram-altin")), parseChange("gram-altin"), !parseChange("gram-altin").startsWith("%-")))
            // BIST (Actually truncgil might not have XU100 directly or it's different)
            if (response.containsKey("BIST 100")) {
                summary.add(MarketSummary("XU100", "BIST 100", String.format("%.2f", parsePrice("BIST 100")), parseChange("BIST 100"), !parseChange("BIST 100").startsWith("%-")))
            }

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
        return try {
            if (isWatched) {
                client.post("$javaBaseUrl/api/game/watchlist") {
                    contentType(ContentType.Application.Json)
                    setBody(WatchlistRequest(symbol))
                }
            } else {
                client.delete("$javaBaseUrl/api/game/watchlist/$symbol")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getWatchlist(): Result<List<String>> {
        return try {
            val response = client.get("$javaBaseUrl/api/game/watchlist").body<List<String>>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNews(page: Int, watchlistOnly: Boolean, symbol: String?, sort: String): Result<List<News>> {
        return try {
            var url = "$javaBaseUrl/api/v1/news?page=$page&size=10&watchlistOnly=$watchlistOnly&sort=$sort"
            if (symbol != null) {
                url += "&symbol=$symbol"
            }
            val response = client.get(url).body<NewsPageResponse>()
            Result.success(response.content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOHLCData(symbol: String): Result<OHLCData> {
        return try {
            val response = client.get("$pythonBaseUrl/api/v1/analysis/ohlc?ticker=$symbol&period=1mo&interval=1d").body<OHLCData>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun triggerFullScan(): Result<ScanResponse> {
        return try {
            val response = client.get("$pythonBaseUrl/api/v1/analysis/run_full_scan_now").body<ScanResponse>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCooldownStatus(): Result<CooldownStatus> {
        return try {
            val response = client.get("$pythonBaseUrl/api/v1/analysis/cooldown_status").body<CooldownStatus>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Serializable
data class WatchlistRequest(val symbol: String)

@Serializable
data class NewsPageResponse(
    val content: List<News>,
    val last: Boolean
)
