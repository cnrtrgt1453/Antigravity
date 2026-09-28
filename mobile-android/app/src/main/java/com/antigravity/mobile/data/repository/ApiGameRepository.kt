package com.antigravity.mobile.data.repository

import com.antigravity.mobile.data.config.SupabaseConfig
import com.antigravity.mobile.domain.model.Portfolio
import com.antigravity.mobile.domain.model.PortfolioItem
import com.antigravity.mobile.domain.model.TradeHistory
import com.antigravity.mobile.domain.repository.GameRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
internal data class SupabasePortfolioItemDto(
    val id: Long,
    @SerialName("stock_symbol") val stockSymbol: String,
    val quantity: Long,
    @SerialName("average_cost") val averageCost: Double
)

@Serializable
internal data class SupabasePortfolioDto(
    val id: Long,
    val balance: Double,
    val items: List<SupabasePortfolioItemDto> = emptyList()
)

@Serializable
internal data class SupabaseTradeHistoryDto(
    val id: Long,
    @SerialName("stock_symbol") val stockSymbol: String,
    val type: String,
    val quantity: Long,
    val price: Double,
    val commission: Double,
    @SerialName("total_amount") val totalAmount: Double,
    val timestamp: String
)

@Serializable
internal data class RpcTradeRequest(
    val p_symbol: String,
    val p_quantity: Long,
    val p_price: Double
)

class ApiGameRepository @Inject constructor(
    private val client: HttpClient,
    private val config: SupabaseConfig
) : GameRepository {

    private val restUrl: String
        get() = "${SupabaseConfig.SUPABASE_URL}/rest/v1"

    override suspend fun getPortfolio(): Result<Portfolio> {
        return try {
            val response = client.get("$restUrl/portfolios?select=*,items:portfolio_items(*)") {
                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                header("Authorization", config.getAuthHeader())
            }.body<List<SupabasePortfolioDto>>()

            val dto = response.firstOrNull() ?: SupabasePortfolioDto(
                id = 0,
                balance = 750000.0,
                items = emptyList()
            )

            val domainPortfolio = Portfolio(
                id = dto.id,
                balance = dto.balance,
                items = dto.items.map {
                    PortfolioItem(
                        id = it.id,
                        stockSymbol = it.stockSymbol,
                        quantity = it.quantity,
                        averageCost = it.averageCost
                    )
                }
            )
            Result.success(domainPortfolio)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTradeHistory(): Result<List<TradeHistory>> {
        return try {
            val response = client.get("$restUrl/trade_history?select=*&order=timestamp.desc") {
                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                header("Authorization", config.getAuthHeader())
            }.body<List<SupabaseTradeHistoryDto>>()

            val domainList = response.map {
                TradeHistory(
                    id = it.id,
                    stockSymbol = it.stockSymbol,
                    type = it.type,
                    quantity = it.quantity,
                    price = it.price,
                    commission = it.commission,
                    totalAmount = it.totalAmount,
                    timestamp = it.timestamp
                )
            }
            Result.success(domainList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun buyStock(symbol: String, quantity: Long, price: Double): Result<Portfolio> {
        return try {
            // Supabase Atomik SQL RPC Fonksiyonunu çağır
            client.post("$restUrl/rpc/buy_stock") {
                contentType(ContentType.Application.Json)
                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                header("Authorization", config.getAuthHeader())
                setBody(RpcTradeRequest(symbol, quantity, price))
            }
            // Güncel portföyü çek ve dön
            getPortfolio()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sellStock(symbol: String, quantity: Long, price: Double): Result<Portfolio> {
        return try {
            // Supabase Atomik SQL RPC Fonksiyonunu çağır
            client.post("$restUrl/rpc/sell_stock") {
                contentType(ContentType.Application.Json)
                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                header("Authorization", config.getAuthHeader())
                setBody(RpcTradeRequest(symbol, quantity, price))
            }
            // Güncel portföyü çek ve dön
            getPortfolio()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
