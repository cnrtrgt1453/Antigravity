package com.antigravity.mobile.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.antigravity.mobile.data.config.SupabaseConfig
import com.antigravity.mobile.data.paging.StockPagingSource
import com.antigravity.mobile.domain.model.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import javax.inject.Inject

@Serializable
internal data class SupabaseNewsDto(
    val id: Int,
    val title: String,
    val content: String? = "",
    @SerialName("published_at") val publishedAt: String,
    @SerialName("stock_symbol") val stockSymbol: String? = null,
    @SerialName("source_url") val sourceUrl: String? = null
)

@Serializable
internal data class WatchlistInsertDto(
    @SerialName("stock_symbol") val stockSymbol: String,
    @SerialName("user_id") val userId: String? = null
)

class ApiMarketRepository @Inject constructor(
    private val client: HttpClient,
    private val config: SupabaseConfig
) : MarketRepository {

    private val restUrl: String
        get() = "${SupabaseConfig.SUPABASE_URL}/rest/v1"

    override fun getStocksPagingData(): Flow<PagingData<Stock>> {
        return Pager(
            config = PagingConfig(pageSize = 20, prefetchDistance = 2),
            pagingSourceFactory = { StockPagingSource(client, config) }
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
            // BIST
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
            val response = client.get("${SupabaseConfig.PYTHON_BASE_URL}/api/v1/analysis/latest_signals").body<Map<String, List<MarketSignal>>>()
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
                val userId = config.getUserId()
                client.post("$restUrl/watchlist") {
                    contentType(ContentType.Application.Json)
                    header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                    header("Authorization", config.getAuthHeader())
                    setBody(WatchlistInsertDto(stockSymbol = symbol, userId = userId))
                }
            } else {
                client.delete("$restUrl/watchlist?stock_symbol=eq.$symbol") {
                    header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                    header("Authorization", config.getAuthHeader())
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getWatchlist(): Result<List<String>> {
        return try {
            val response = client.get("$restUrl/watchlist?select=stock_symbol") {
                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                header("Authorization", config.getAuthHeader())
            }.body<List<WatchlistInsertDto>>()
            Result.success(response.map { it.stockSymbol })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNews(page: Int, watchlistOnly: Boolean, symbol: String?, sort: String): Result<List<News>> {
        return try {
            val offset = page * 10
            var url = "$restUrl/news?select=*&order=published_at.desc&limit=10&offset=$offset"
            if (!symbol.isNullOrEmpty()) {
                url += "&stock_symbol=eq.$symbol"
            }

            val dtoList = client.get(url) {
                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                header("Authorization", config.getAuthHeader())
            }.body<List<SupabaseNewsDto>>()

            val domainList = dtoList.map {
                News(
                    id = it.id,
                    title = it.title,
                    content = it.content ?: "",
                    publishedAt = it.publishedAt,
                    stockSymbol = it.stockSymbol ?: "",
                    sourceUrl = it.sourceUrl ?: ""
                )
            }
            Result.success(domainList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOHLCData(symbol: String): Result<OHLCData> {
        return try {
            val response = client.get("${SupabaseConfig.PYTHON_BASE_URL}/api/v1/analysis/ohlc?ticker=$symbol&period=1mo&interval=1d").body<OHLCData>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun triggerFullScan(): Result<ScanResponse> {
        return try {
            val response = client.get("${SupabaseConfig.PYTHON_BASE_URL}/api/v1/analysis/run_full_scan_now").body<ScanResponse>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCooldownStatus(): Result<CooldownStatus> {
        return try {
            val response = client.get("${SupabaseConfig.PYTHON_BASE_URL}/api/v1/analysis/cooldown_status").body<CooldownStatus>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
