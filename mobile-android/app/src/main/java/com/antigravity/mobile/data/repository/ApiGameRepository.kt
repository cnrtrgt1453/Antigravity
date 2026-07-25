package com.antigravity.mobile.data.repository

import com.antigravity.mobile.domain.model.Portfolio
import com.antigravity.mobile.domain.model.TradeHistory
import com.antigravity.mobile.domain.repository.GameRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class TradeRequest(
    val symbol: String,
    val quantity: Long,
    val price: Double
)

class ApiGameRepository @Inject constructor(
    private val client: HttpClient
) : GameRepository {

    private val baseUrl = "http://10.0.2.2:8080/api/game"

    override suspend fun getPortfolio(): Result<Portfolio> {
        return try {
            val response = client.get("$baseUrl/portfolio").body<Portfolio>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTradeHistory(): Result<List<TradeHistory>> {
        return try {
            val response = client.get("$baseUrl/history").body<List<TradeHistory>>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun buyStock(symbol: String, quantity: Long, price: Double): Result<Portfolio> {
        return try {
            val response = client.post("$baseUrl/buy") {
                contentType(ContentType.Application.Json)
                setBody(TradeRequest(symbol, quantity, price))
            }.body<Portfolio>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sellStock(symbol: String, quantity: Long, price: Double): Result<Portfolio> {
        return try {
            val response = client.post("$baseUrl/sell") {
                contentType(ContentType.Application.Json)
                setBody(TradeRequest(symbol, quantity, price))
            }.body<Portfolio>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
