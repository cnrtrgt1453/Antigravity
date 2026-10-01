package com.antigravity.mobile.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Stock(
    val symbol: String,
    val name: String,
    val category: String?,
    // Analysis fields
    val signal: String? = "NO_SIGNAL",
    val currentPrice: Double? = null,
    val sma50: Double? = null,
    val sma200: Double? = null,
    val crossPrice: Double? = null,
    val crossDate: String? = null,
    val isWatched: Boolean = false,
    val dailyChangePercent: Double? = null,
    val crossDiff: Double? = null,
    val crossDiffPercent: Double? = null
)

enum class MarketFilter(val title: String) {
    ALL("Tümü"),
    BIST30("BIST 30"),
    BIST50("BIST 50"),
    BIST100("BIST 100"),
    TOP_GAINERS("Günün Yükselenleri"),
    TOP_LOSERS("Günün Düşenleri"),
    CROSS_GAINERS("Kesişim Kazandıranlar"),
    CROSS_LOSERS("Kesişim Kaybettirenler")
}

@Serializable
data class MarketSignal(
    val ticker: String,
    val signal: String, // GOLDEN_CROSS, DEAD_CROSS
    val current_price: Double,
    val cross_price: Double?,
    val cross_date: String?,
    val sma50: Double? = null,
    val sma200: Double? = null,
    val message: String?
)

@Serializable
data class MarketSummary(
    val symbol: String,
    val name: String,
    val price: String,
    val change: String,
    val isUpward: Boolean
)
