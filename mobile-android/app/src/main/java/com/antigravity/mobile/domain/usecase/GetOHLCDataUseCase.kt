package com.antigravity.mobile.domain.usecase

import com.antigravity.mobile.domain.model.OHLCData
import com.antigravity.mobile.domain.repository.MarketRepository
import javax.inject.Inject

class GetOHLCDataUseCase @Inject constructor(
    private val marketRepository: MarketRepository
) {
    suspend operator fun invoke(symbol: String): Result<OHLCData> {
        if (symbol.isBlank()) {
            return Result.failure(IllegalArgumentException("Sembol boş olamaz."))
        }
        return marketRepository.getOHLCData(symbol)
    }
}
