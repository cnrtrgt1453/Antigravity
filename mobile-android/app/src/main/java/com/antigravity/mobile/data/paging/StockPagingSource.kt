package com.antigravity.mobile.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.antigravity.mobile.data.config.SupabaseConfig
import com.antigravity.mobile.domain.model.Stock
import com.antigravity.mobile.domain.model.MarketFilter
import com.antigravity.mobile.domain.util.BistCompanyNames
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StockDto(
    val symbol: String,
    val name: String,
    val category: String? = null,
    @SerialName("last_price") val lastPrice: Double? = null,
    val sma50: Double? = null,
    val sma200: Double? = null,
    val signal: String? = null,
    @SerialName("cross_price") val crossPrice: Double? = null,
    @SerialName("cross_date") val crossDate: String? = null,
    @SerialName("daily_change_percent") val dailyChangePercent: Double? = null,
    @SerialName("cross_diff") val crossDiff: Double? = null,
    @SerialName("cross_diff_percent") val crossDiffPercent: Double? = null
)

@Serializable
internal data class WatchlistSymbolDto(
    @SerialName("stock_symbol") val stockSymbol: String
)

class StockPagingSource(
    private val client: HttpClient,
    private val config: SupabaseConfig,
    private val filter: MarketFilter = MarketFilter.ALL,
    private val searchQuery: String = ""
) : PagingSource<Int, Stock>() {

    private var watchlistCache: Set<String>? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Stock> {
        val page = params.key ?: 0
        return try {
            val restUrl = "${SupabaseConfig.SUPABASE_URL}/rest/v1"

            // 1. İlk sayfada takip listesini hem yerelden hem uzaktan yükle (oturum varsa)
            if (page == 0 || watchlistCache == null) {
                val localWatchlist = config.getLocalWatchlist()
                if (config.isLoggedIn()) {
                    try {
                        val response = client.get("$restUrl/watchlist?select=stock_symbol") {
                            header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                            header("Authorization", config.getAuthHeader())
                        }
                        if (response.status.value in 200..299) {
                            val watchlistItems = response.body<List<WatchlistSymbolDto>>()
                            val remoteWatchlist = watchlistItems.map { it.stockSymbol }.toSet()
                            val merged = localWatchlist + remoteWatchlist
                            config.saveLocalWatchlist(merged)
                            watchlistCache = merged
                        } else {
                            watchlistCache = localWatchlist
                        }
                    } catch (e: Exception) {
                        watchlistCache = localWatchlist
                    }
                } else {
                    watchlistCache = localWatchlist
                }
            }

            // 2. Filtreye göre Supabase sorgu parametresini oluştur
            val filterParam = when (filter) {
                MarketFilter.ALL -> "order=symbol.asc"
                MarketFilter.BIST30 -> "is_bist30=eq.true&order=symbol.asc"
                MarketFilter.BIST50 -> "is_bist50=eq.true&order=symbol.asc"
                MarketFilter.BIST100 -> "is_bist100=eq.true&order=symbol.asc"
                MarketFilter.TOP_GAINERS -> "order=daily_change_percent.desc.nullslast"
                MarketFilter.TOP_LOSERS -> "order=daily_change_percent.asc.nullslast"
                MarketFilter.CROSS_GAINERS -> "order=cross_diff_percent.desc.nullslast"
                MarketFilter.CROSS_LOSERS -> "order=cross_diff_percent.asc.nullslast"
            }

            var queryParams = filterParam
            if (searchQuery.isNotBlank()) {
                val cleanQuery = searchQuery.trim()
                val encodedQuery = cleanQuery.replace(" ", "%20")
                val conditions = mutableListOf<String>()
                conditions.add("symbol.ilike.*$encodedQuery*")
                conditions.add("name.ilike.*$encodedQuery*")
                val matchedSymbols = BistCompanyNames.findSymbolsByNameQuery(cleanQuery)
                if (matchedSymbols.isNotEmpty()) {
                    conditions.add("symbol.in.(${matchedSymbols.joinToString(",")})")
                }
                val searchOr = "or=(${conditions.joinToString(",")})"
                queryParams = "$queryParams&$searchOr"
            }

            val offset = page * params.loadSize
            val selectFields = "symbol,name,category,last_price,sma50,sma200,signal,cross_price,cross_date,daily_change_percent,cross_diff,cross_diff_percent"
            val httpResponse = client.get("$restUrl/stocks?select=$selectFields&$queryParams&limit=${params.loadSize}&offset=$offset") {
                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                header("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
            }

            if (httpResponse.status.value !in 200..299) {
                val errorBody = httpResponse.bodyAsText()
                return LoadResult.Error(Exception("Veri alınamadı (${httpResponse.status.value}): $errorBody"))
            }

            val stocksDtoList = httpResponse.body<List<StockDto>>()

            // 3. Domain Stock modeline dönüştür
            val currentWatched = watchlistCache ?: config.getLocalWatchlist()
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
                    isWatched = currentWatched.contains(dto.symbol) || currentWatched.contains(dto.symbol.substringBefore(".")),
                    dailyChangePercent = dto.dailyChangePercent,
                    crossDiff = dto.crossDiff,
                    crossDiffPercent = dto.crossDiffPercent
                )
            }

            val isLastPage = stocksDtoList.size < params.loadSize
            LoadResult.Page(
                data = stocks,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (isLastPage) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Stock>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
