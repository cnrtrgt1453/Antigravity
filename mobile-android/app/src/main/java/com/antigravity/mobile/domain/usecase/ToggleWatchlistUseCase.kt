package com.antigravity.mobile.domain.usecase

import com.antigravity.mobile.domain.repository.MarketRepository
import javax.inject.Inject

class ToggleWatchlistUseCase @Inject constructor(
    private val marketRepository: MarketRepository
) {
    suspend operator fun invoke(symbol: String, add: Boolean): Result<Unit> {
        if (symbol.isBlank()) {
            return Result.failure(IllegalArgumentException("Sembol bilgisi boş olamaz."))
        }
        return marketRepository.toggleWatchlist(symbol, add)
    }
}
