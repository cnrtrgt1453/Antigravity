package com.antigravity.mobile.domain.usecase

import com.antigravity.mobile.domain.model.Portfolio
import com.antigravity.mobile.domain.repository.GameRepository
import java.math.BigDecimal
import javax.inject.Inject

class BuyStockUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(symbol: String, quantity: Long, price: Double): Result<Portfolio> {
        if (symbol.isBlank()) {
            return Result.failure(IllegalArgumentException("Sembol geçerli olmalıdır."))
        }
        if (quantity <= 0) {
            return Result.failure(IllegalArgumentException("Alım miktarı 0'dan büyük olmalıdır."))
        }
        if (price <= 0.0) {
            return Result.failure(IllegalArgumentException("Fiyat 0'dan büyük olmalıdır."))
        }
        return gameRepository.buyStock(symbol, quantity, price)
    }
}
