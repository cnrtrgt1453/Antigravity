package com.antigravity.mobile.domain.repository

import com.antigravity.mobile.domain.model.TradingSignal
import kotlinx.coroutines.flow.Flow

interface SignalRepository {
    fun getActiveSignals(): Flow<Result<List<TradingSignal>>>
}
