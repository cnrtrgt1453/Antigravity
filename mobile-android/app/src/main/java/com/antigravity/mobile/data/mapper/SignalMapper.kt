package com.antigravity.mobile.data.mapper

import com.antigravity.mobile.data.remote.dto.SignalDto
import com.antigravity.mobile.domain.model.SignalDirection
import com.antigravity.mobile.domain.model.SignalMetrics
import com.antigravity.mobile.domain.model.TradingSignal

fun SignalDto.toDomain(): TradingSignal {
    return TradingSignal(
        symbol = this.symbol,
        direction = when (this.signalType.uppercase()) {
            "BUY" -> SignalDirection.BUY
            "SELL" -> SignalDirection.SELL
            else -> SignalDirection.HOLD
        },
        entryPrice = this.entryPrice,
        stopLoss = this.stopLoss,
        takeProfit = this.takeProfit,
        riskRewardRatio = this.riskRewardRatio,
        timestamp = this.timestamp,
        metrics = SignalMetrics(
            ema200 = this.ema200,
            ema21 = this.ema21,
            adx = this.adx,
            stochK = this.stochK,
            stochD = this.stochD,
            volumeRatio = this.volumeRatio,
            atr = this.atr
        )
    )
}
