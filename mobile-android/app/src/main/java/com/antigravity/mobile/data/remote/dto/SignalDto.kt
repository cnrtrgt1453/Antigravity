package com.antigravity.mobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignalDto(
    @SerialName("symbol") val symbol: String,
    @SerialName("signal_type") val signalType: String,
    @SerialName("entry_price") val entryPrice: Double,
    @SerialName("stop_loss") val stopLoss: Double,
    @SerialName("take_profit") val takeProfit: Double,
    @SerialName("risk_reward_ratio") val riskRewardRatio: Double,
    @SerialName("timestamp") val timestamp: Long,
    @SerialName("ema200") val ema200: Double? = null,
    @SerialName("ema21") val ema21: Double? = null,
    @SerialName("adx") val adx: Double? = null,
    @SerialName("stoch_k") val stochK: Double? = null,
    @SerialName("stoch_d") val stochD: Double? = null,
    @SerialName("volume_ratio") val volumeRatio: Double? = null,
    @SerialName("atr") val atr: Double? = null
)
