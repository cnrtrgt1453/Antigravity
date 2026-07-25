package com.antigravity.mobile.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Portfolio(
    val id: Long,
    val balance: Double,
    val items: List<PortfolioItem> = emptyList()
)

@Serializable
data class PortfolioItem(
    val id: Long,
    val stockSymbol: String,
    val quantity: Long,
    val averageCost: Double
)

@Serializable
data class TradeHistory(
    val id: Long,
    val stockSymbol: String,
    val type: String, // BUY, SELL
    val quantity: Long,
    val price: Double,
    val commission: Double,
    val totalAmount: Double,
    val timestamp: String
)
