package com.antigravity.mobile.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.antigravity.mobile.domain.model.Stock
import com.antigravity.mobile.domain.model.MarketSignal
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

@Serializable
data class StockPageResponse(
    val content: List<StockDto>,
    val last: Boolean,
    val number: Int
)

@Serializable
data class StockDto(
    val symbol: String,
    val name: String,
    val category: String?
)

class StockPagingSource(
    private val client: HttpClient,
    private val javaBaseUrl: String,
    private val pythonBaseUrl: String
) : PagingSource<Int, Stock>() {

    private var analysisCache: List<MarketSignal>? = null
    private var watchlistCache: List<String>? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Stock> {
        val page = params.key ?: 0
        return try {
            // 1. Fetch Cache once per first page or if cache is empty
            if (page == 0 || analysisCache == null) {
                analysisCache = client.get("$pythonBaseUrl/api/v1/analysis/all_market_data").body<List<MarketSignal>>()
            }
            if (page == 0 || watchlistCache == null) {
                watchlistCache = client.get("$javaBaseUrl/api/game/watchlist").body<List<String>>()
            }

            // 2. Fetch Stocks from Java
            val response = client.get("$javaBaseUrl/api/v1/stocks?page=$page&size=${params.loadSize}").body<StockPageResponse>()
            
            // 3. Map to Domain Model with Analysis
            val stocks = response.content.map { dto ->
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

            LoadResult.Page(
                data = stocks,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (response.last) null else page + 1
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
