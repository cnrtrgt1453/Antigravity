package com.antigravity.mobile.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.antigravity.mobile.data.config.SupabaseConfig
import com.antigravity.mobile.data.paging.StockDto
import com.antigravity.mobile.data.paging.StockPagingSource
import com.antigravity.mobile.domain.model.*
import com.antigravity.mobile.domain.repository.MarketRepository
import com.antigravity.mobile.domain.util.BistCompanyNames
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

    override fun getStocksPagingData(
        filter: com.antigravity.mobile.domain.model.MarketFilter,
        searchQuery: String
    ): Flow<PagingData<Stock>> {
        return Pager(
            config = PagingConfig(pageSize = 20, prefetchDistance = 2),
            pagingSourceFactory = { StockPagingSource(client, config, filter, searchQuery) }
        ).flow
    }

    override suspend fun getMarketSummary(): Result<List<MarketSummary>> {
        return try {
            val response = client.get("https://finans.truncgil.com/today.json").body<Map<String, JsonElement>>()
            val summary = mutableListOf<MarketSummary>()
            
            val parsePrice = { key: String ->
                val obj = response[key]?.jsonObject
                val priceStr = obj?.get("Alış")?.jsonPrimitive?.content 
                    ?: obj?.get("alis")?.jsonPrimitive?.content 
                    ?: "0"
                priceStr.replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
            }

            val parseChange = { key: String ->
                val obj = response[key]?.jsonObject
                obj?.get("Değişim")?.jsonPrimitive?.content 
                    ?: obj?.get("degisim")?.jsonPrimitive?.content 
                    ?: "0"
            }

            // 1. Dolar (USD)
            if (response.containsKey("USD")) {
                val change = parseChange("USD")
                summary.add(MarketSummary("USD", "Dolar", String.format(java.util.Locale.US, "%.2f ₺", parsePrice("USD")), change, !change.startsWith("%-") && !change.startsWith("-")))
            }

            // 2. Euro (EUR)
            if (response.containsKey("EUR")) {
                val change = parseChange("EUR")
                summary.add(MarketSummary("EUR", "Euro", String.format(java.util.Locale.US, "%.2f ₺", parsePrice("EUR")), change, !change.startsWith("%-") && !change.startsWith("-")))
            }

            // 3. Pound / Sterlin (GBP)
            val gbpKey = when {
                response.containsKey("GBP") -> "GBP"
                response.containsKey("ingiliz-sterlini") -> "ingiliz-sterlini"
                response.containsKey("sterlin") -> "sterlin"
                else -> null
            }
            if (gbpKey != null) {
                val change = parseChange(gbpKey)
                summary.add(MarketSummary("GBP", "Pound", String.format(java.util.Locale.US, "%.2f ₺", parsePrice(gbpKey)), change, !change.startsWith("%-") && !change.startsWith("-")))
            }

            // 4. Gram Altın
            val goldKey = when {
                response.containsKey("gram-altin") -> "gram-altin"
                response.containsKey("GRA") -> "GRA"
                response.containsKey("altin") -> "altin"
                else -> null
            }
            if (goldKey != null) {
                val change = parseChange(goldKey)
                summary.add(MarketSummary("GA", "Gram Altın", String.format(java.util.Locale.US, "%.2f ₺", parsePrice(goldKey)), change, !change.startsWith("%-") && !change.startsWith("-")))
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
        val currentLocal = config.getLocalWatchlist().toMutableSet()
        val cleanSymbol = symbol.trim().uppercase()
        val baseSymbol = cleanSymbol.substringBefore(".")
        if (isWatched) {
            currentLocal.add(cleanSymbol)
        } else {
            currentLocal.remove(cleanSymbol)
            currentLocal.remove(baseSymbol)
            currentLocal.remove("$baseSymbol.IS")
        }
        config.saveLocalWatchlist(currentLocal)

        return try {
            val userId = config.getUserId()
            if (!userId.isNullOrEmpty()) {
                if (isWatched) {
                    client.post("$restUrl/watchlist") {
                        contentType(ContentType.Application.Json)
                        header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                        header("Authorization", config.getAuthHeader())
                        setBody(WatchlistInsertDto(stockSymbol = cleanSymbol, userId = userId))
                    }
                } else {
                    client.delete("$restUrl/watchlist?stock_symbol=eq.$cleanSymbol&user_id=eq.$userId") {
                        header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                        header("Authorization", config.getAuthHeader())
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    override suspend fun getWatchlist(): Result<List<String>> {
        val localList = config.getLocalWatchlist().toList()
        return try {
            val userId = config.getUserId()
            if (!userId.isNullOrEmpty()) {
                val response = client.get("$restUrl/watchlist?select=stock_symbol&user_id=eq.$userId") {
                    header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                    header("Authorization", config.getAuthHeader())
                }.body<List<WatchlistInsertDto>>()
                val remoteSymbols = response.map { it.stockSymbol }.toSet()
                val merged = (localList.toSet() + remoteSymbols)
                config.saveLocalWatchlist(merged)
                Result.success(merged.toList())
            } else {
                Result.success(localList)
            }
        } catch (e: Exception) {
            Result.success(localList)
        }
    }

    override suspend fun getWatchlistStocks(): Result<List<Stock>> {
        return try {
            val watchlistResult = getWatchlist()
            val symbols = watchlistResult.getOrDefault(config.getLocalWatchlist().toList())
            if (symbols.isEmpty()) {
                return Result.success(emptyList())
            }
            val selectFields = "symbol,name,category,last_price,sma50,sma200,signal,cross_price,cross_date,daily_change_percent,cross_diff,cross_diff_percent"
            val symbolsParam = symbols.joinToString(",")
            val httpResponse = client.get("$restUrl/stocks?select=$selectFields&symbol=in.($symbolsParam)&order=symbol.asc") {
                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                header("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
            }
            if (httpResponse.status.value !in 200..299) {
                return Result.failure(Exception("Hisse verisi alınamadı (${httpResponse.status.value})"))
            }
            val stocksDtoList = httpResponse.body<List<StockDto>>()

            val stocks = stocksDtoList.map { dto ->
                Stock(
                    symbol = dto.symbol,
                    name = BistCompanyNames.getCompanyName(dto.symbol, dto.name),
                    category = dto.category,
                    signal = dto.signal ?: "NO_SIGNAL",
                    currentPrice = dto.lastPrice,
                    sma50 = dto.sma50,
                    sma200 = dto.sma200,
                    crossPrice = dto.crossPrice,
                    crossDate = dto.crossDate,
                    isWatched = true,
                    dailyChangePercent = dto.dailyChangePercent,
                    crossDiff = dto.crossDiff,
                    crossDiffPercent = dto.crossDiffPercent
                )
            }
            Result.success(stocks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBist30Stocks(): Result<List<Stock>> {
        return try {
            val selectFields = "symbol,name,category,last_price,sma50,sma200,signal,cross_price,cross_date,daily_change_percent,cross_diff,cross_diff_percent"
            val httpResponse = client.get("$restUrl/stocks?select=$selectFields&is_bist30=eq.true&order=symbol.asc") {
                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                header("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
            }
            if (httpResponse.status.value !in 200..299) {
                return Result.failure(Exception("BIST 30 verisi alınamadı (${httpResponse.status.value})"))
            }
            val stocksDtoList = httpResponse.body<List<StockDto>>()
            val localWatchlist = config.getLocalWatchlist()

            val stocks = stocksDtoList.map { dto ->
                val cleanSymbol = dto.symbol.substringBefore(".")
                val isWatched = localWatchlist.contains(cleanSymbol) || localWatchlist.contains(dto.symbol)
                Stock(
                    symbol = dto.symbol,
                    name = BistCompanyNames.getCompanyName(dto.symbol, dto.name),
                    category = dto.category,
                    signal = dto.signal ?: "NO_SIGNAL",
                    currentPrice = dto.lastPrice,
                    sma50 = dto.sma50,
                    sma200 = dto.sma200,
                    crossPrice = dto.crossPrice,
                    crossDate = dto.crossDate,
                    isWatched = isWatched,
                    dailyChangePercent = dto.dailyChangePercent,
                    crossDiff = dto.crossDiff,
                    crossDiffPercent = dto.crossDiffPercent
                )
            }
            Result.success(stocks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncKapNews(): Result<Unit> {
        return try {
            client.get("${SupabaseConfig.PYTHON_BASE_URL}/api/v1/news/sync")
            Result.success(Unit)
        } catch (e: Exception) {
            // Python servisi o an çalışmıyorsa sessizce geç, Supabase verileri doğrudan okunur
            Result.success(Unit)
        }
    }

    override suspend fun getNews(page: Int, watchlistOnly: Boolean, symbol: String?, sort: String): Result<List<News>> {
        return try {
            val offset = page * 20
            
            // Bulunduğumuz ayın ilk gününü UTC formatında hesapla
            val calendar = java.util.Calendar.getInstance()
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH) + 1
            val monthStr = if (month < 10) "0$month" else "$month"
            val startOfMonth = "$year-$monthStr-01T00:00:00Z"

            val orderParam = if (sort.equals("asc", ignoreCase = true) || sort.endsWith("asc", ignoreCase = true)) {
                "published_at.asc"
            } else {
                "published_at.desc"
            }

            var url = "$restUrl/news?select=*&published_at=gte.$startOfMonth&order=$orderParam&limit=20&offset=$offset"
            if (!symbol.isNullOrBlank()) {
                val cleanSymbol = symbol.trim().uppercase().removeSuffix(".IS")
                url += "&stock_symbol=eq.$cleanSymbol"
            }

            val httpResponse = client.get(url) {
                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                header("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
            }
            if (httpResponse.status.value !in 200..299) {
                return Result.failure(Exception("Haberler alınamadı (${httpResponse.status.value})"))
            }

            val dtoList = httpResponse.body<List<SupabaseNewsDto>>()

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
