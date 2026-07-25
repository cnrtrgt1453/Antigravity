package com.antigravity.mobile.domain.repository

import com.antigravity.mobile.domain.model.Portfolio
import com.antigravity.mobile.domain.model.TradeHistory

interface GameRepository {
    suspend fun getPortfolio(): Result<Portfolio>
    suspend fun getTradeHistory(): Result<List<TradeHistory>>
    suspend fun buyStock(symbol: String, quantity: Long, price: Double): Result<Portfolio>
    suspend fun sellStock(symbol: String, quantity: Long, price: Double): Result<Portfolio>
}
