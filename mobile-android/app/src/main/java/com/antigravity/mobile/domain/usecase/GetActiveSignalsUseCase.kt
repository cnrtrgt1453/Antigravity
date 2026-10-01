package com.antigravity.mobile.domain.usecase

import com.antigravity.mobile.domain.model.TradingSignal
import com.antigravity.mobile.domain.repository.SignalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveSignalsUseCase @Inject constructor(
    private val repository: SignalRepository
) {
    operator fun invoke(): Flow<Result<List<TradingSignal>>> {
        return repository.getActiveSignals()
    }
}
