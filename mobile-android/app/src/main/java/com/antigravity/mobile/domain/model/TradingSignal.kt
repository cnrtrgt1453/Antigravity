package com.antigravity.mobile.domain.model

enum class SignalDirection { BUY, SELL, HOLD }

data class SignalMetrics(
    val ema200: Double? = null,
    val ema21: Double? = null,
    val adx: Double? = null,
    val stochK: Double? = null,
    val stochD: Double? = null,
    val volumeRatio: Double? = null,
    val atr: Double? = null
)

data class TradingSignal(
    val symbol: String,
    val direction: SignalDirection,
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val riskRewardRatio: Double,
    val timestamp: Long,
    val metrics: SignalMetrics = SignalMetrics()
) {
    val potentialProfitPercent: Double
        get() = if (entryPrice > 0) {
            if (direction == SignalDirection.BUY) {
                ((takeProfit - entryPrice) / entryPrice) * 100.0
            } else {
                ((entryPrice - takeProfit) / entryPrice) * 100.0
            }
        } else 0.0

    val riskPercent: Double
        get() = if (entryPrice > 0) {
            if (direction == SignalDirection.BUY) {
                ((entryPrice - stopLoss) / entryPrice) * 100.0
            } else {
                ((stopLoss - entryPrice) / entryPrice) * 100.0
            }
        } else 0.0
}
