package com.antigravity.mobile.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.antigravity.mobile.data.config.SupabaseConfig
import com.antigravity.mobile.domain.model.Stock
import com.antigravity.mobile.domain.model.MarketSignal
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StockDto(
    val symbol: String,
    val name: String,
    val category: String? = null
)

@Serializable
internal data class WatchlistSymbolDto(
    @SerialName("stock_symbol") val stockSymbol: String
)

class StockPagingSource(
    private val client: HttpClient,
    private val config: SupabaseConfig
) : PagingSource<Int, Stock>() {

    private var analysisCache: List<MarketSignal>? = null
    private var watchlistCache: Set<String>? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Stock> {
        val page = params.key ?: 0
        return try {
            val restUrl = "${SupabaseConfig.SUPABASE_URL}/rest/v1"

            // 1. İlk sayfada önbelleği doldur
            if (page == 0 || analysisCache == null) {
                try {
                    analysisCache = client.get("${SupabaseConfig.PYTHON_BASE_URL}/api/v1/analysis/all_market_data").body<List<MarketSignal>>()
                } catch (e: Exception) {
                    analysisCache = emptyList()
                }
            }

            if (page == 0 || watchlistCache == null) {
                try {
                    val watchlistItems = client.get("$restUrl/watchlist?select=stock_symbol") {
                        header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                        header("Authorization", config.getAuthHeader())
                    }.body<List<WatchlistSymbolDto>>()
                    watchlistCache = watchlistItems.map { it.stockSymbol }.toSet()
                } catch (e: Exception) {
                    watchlistCache = emptySet()
                }
            }

            // 2. Hisseleri Supabase'den sayfalı olarak çek
            val offset = page * params.loadSize
            val stocksDtoList = client.get("$restUrl/stocks?select=symbol,name,category&order=symbol.asc&limit=${params.loadSize}&offset=$offset") {
                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                header("Authorization", config.getAuthHeader())
            }.body<List<StockDto>>()

            // 3. Domain Stock modeline dönüştür
            val stocks = stocksDtoList.map { dto ->
                val analysis = analysisCache?.find { it.ticker == dto.symbol }
                Stock(
                    symbol = dto.symbol,
                    name = dto.name,
                    category = dto.category,
                    signal = analysis?.signal ?: "NO_SIGNAL",
                    currentPrice = analysis?.current_price,
                    sma50 = analysis?.sma50,
                    sma200 = analysis?.sma200,
                    crossPrice = analysis?.cross_price,
                    isWatched = watchlistCache?.contains(dto.symbol) == true
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
